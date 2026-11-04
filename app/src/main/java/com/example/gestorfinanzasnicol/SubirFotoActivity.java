package com.example.gestorfinanzasnicol;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SubirFotoActivity extends AppCompatActivity {

    private ImageView ivPrevia;
    private Button btnElegirGaleria, btnTomarFoto, btnSubir, btnVolver;
    private ProgressBar progressBar;

    private Uri uriGaleria = null;
    private Bitmap bitmapCamara = null;

    // Datos que vienen del formulario
    private String idTransaccion;
    private String concepto;
    private double monto;
    private String tipo;

    private FirebaseStorage storage;
    private FirebaseFirestore db;

    private ActivityResultLauncher<String> abrirGaleriaLauncher;
    private ActivityResultLauncher<Void> abrirCamaraLauncher;
    private ActivityResultLauncher<String> solicitarPermisoCamaraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subir_foto);

        // 🔹 Recibimos TODOS los datos desde MainActivity
        Intent intent = getIntent();
        idTransaccion = intent.getStringExtra("idTransaccion");
        concepto = intent.getStringExtra("concepto");
        monto = intent.getDoubleExtra("monto", 0.0);
        tipo = intent.getStringExtra("tipo");

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        ivPrevia = findViewById(R.id.ivPrevia);
        btnElegirGaleria = findViewById(R.id.btnElegirGaleria);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnSubir = findViewById(R.id.btnSubir);
        btnVolver = findViewById(R.id.btnVolver);
        progressBar = findViewById(R.id.progressBar);

        btnVolver.setOnClickListener(v -> finish());

        abrirGaleriaLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uriGaleria = uri;
                        bitmapCamara = null;
                        ivPrevia.setImageURI(uri);
                    }
                }
        );

        abrirCamaraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        bitmapCamara = bitmap;
                        uriGaleria = null;
                        ivPrevia.setImageBitmap(bitmap);
                        Toast.makeText(this, "Foto capturada. Lista para subir.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        solicitarPermisoCamaraLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        abrirCamaraLauncher.launch(null);
                    } else {
                        Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnElegirGaleria.setOnClickListener(v -> abrirGaleriaLauncher.launch("image/*"));

        btnTomarFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                abrirCamaraLauncher.launch(null);
            } else {
                solicitarPermisoCamaraLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnSubir.setOnClickListener(v -> {
            if (uriGaleria == null && bitmapCamara == null) {
                Toast.makeText(this, "Primero selecciona o toma una foto", Toast.LENGTH_SHORT).show();
                return;
            }
            subirImagenYGuardarTransaccion();
        });
    }

    private void subirImagenYGuardarTransaccion() {
        progressBar.setVisibility(View.VISIBLE);
        btnSubir.setEnabled(false);

        String nombreArchivo = "recibo_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storage.getReference().child("recibos/" + nombreArchivo);

        UploadTask uploadTask;

        if (uriGaleria != null) {
            uploadTask = ref.putFile(uriGaleria);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmapCamara.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] data = baos.toByteArray();
            uploadTask = ref.putBytes(data);
        }

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                String urlFinal = downloadUri.toString();
                guardarTransaccionEnFirestore(urlFinal);
            } else {
                mostrarError(task.getException());
            }
        });
    }

    private void guardarTransaccionEnFirestore(String url) {
        // Creamos la transacción completa (Datos + Foto)
        Map<String, Object> transaccionMap = new HashMap<>();
        transaccionMap.put("id", idTransaccion);
        transaccionMap.put("concepto", concepto);
        transaccionMap.put("monto", monto);
        transaccionMap.put("tipo", tipo);
        transaccionMap.put("urlFoto", url);
        transaccionMap.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        transaccionMap.put("fecha", System.currentTimeMillis());

        // Usamos set() para crearla o sobreescribirla si el ID ya existía
        db.collection("transacciones").document(idTransaccion)
                .set(transaccionMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Foto y transacción guardadas con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(this::mostrarError);
    }

    private void mostrarError(Exception e) {
        progressBar.setVisibility(View.GONE);
        btnSubir.setEnabled(true);
        String mensaje = (e != null) ? e.getMessage() : "Error desconocido";
        Toast.makeText(this, "Error: " + mensaje, Toast.LENGTH_LONG).show();
    }
}