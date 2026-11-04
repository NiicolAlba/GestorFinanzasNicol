package com.example.gestorfinanzasnicol;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String CANAL_ID = "canal_alertas_finanzas";

    private EditText etId, etConcepto, etMonto, etTipo;
    private TextInputLayout tilConcepto, tilMonto;

    // 🔹 AGREGAMOS btnAbrirUbicacion A LA LISTA
    private Button btnAgregar, btnBuscar, btnEditar, btnBorrar, btnVerTodos, btnCerrarSesion, btnAdjuntarFoto, btnAbrirMapa, btnAbrirUbicacion;

    private RecyclerView rvTransacciones;
    private ProgressBar progressBar;
    private ImageView ivFotoTransaccion;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private AdaptadorTransaccion adaptador;
    private List<Transaccion> listaTransacciones;
    private com.google.firebase.firestore.ListenerRegistration listenerFirestore;

    private boolean limpiandoCampos = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        crearCanalDeNotificaciones();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "⚠️ Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etId = findViewById(R.id.etId);
        etConcepto = findViewById(R.id.etConcepto);
        etMonto = findViewById(R.id.etMonto);
        etTipo = findViewById(R.id.etTipo);
        tilConcepto = findViewById(R.id.tilConcepto);
        tilMonto = findViewById(R.id.tilMonto);
        btnAgregar = findViewById(R.id.btnAgregar);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnEditar = findViewById(R.id.btnEditar);
        btnBorrar = findViewById(R.id.btnBorrar);
        btnVerTodos = findViewById(R.id.btnVerTodos);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnAdjuntarFoto = findViewById(R.id.btnAdjuntarFoto);
        btnAbrirMapa = findViewById(R.id.btnAbrirMapa);

        // 🔹 INICIALIZAMOS EL BOTÓN DEL GPS
        btnAbrirUbicacion = findViewById(R.id.btnAbrirUbicacion);

        rvTransacciones = findViewById(R.id.rvTransacciones);
        progressBar = findViewById(R.id.progressBarMain);
        ivFotoTransaccion = findViewById(R.id.ivFotoTransaccion);

        rvTransacciones.setLayoutManager(new LinearLayoutManager(this));
        rvTransacciones.setNestedScrollingEnabled(true);

        listaTransacciones = new ArrayList<>();
        adaptador = new AdaptadorTransaccion(listaTransacciones, transaccion -> {
            etId.setText(transaccion.getId());
            etConcepto.setText(transaccion.getConcepto());
            etMonto.setText(String.valueOf(transaccion.getMonto()));
            etTipo.setText(transaccion.getTipo());

            if (transaccion.getUrlFoto() != null && !transaccion.getUrlFoto().isEmpty()) {
                ivFotoTransaccion.setVisibility(View.VISIBLE);
                Glide.with(this).load(transaccion.getUrlFoto()).into(ivFotoTransaccion);
            } else {
                ivFotoTransaccion.setVisibility(View.GONE);
            }
            Toast.makeText(this, "📝 Cargado para editar", Toast.LENGTH_SHORT).show();
        });
        rvTransacciones.setAdapter(adaptador);

        configurarValidaciones();

        btnAgregar.setOnClickListener(v -> agregarTransaccion());
        btnBuscar.setOnClickListener(v -> buscarTransaccion());
        btnEditar.setOnClickListener(v -> actualizarTransaccion());
        btnBorrar.setOnClickListener(v -> eliminarTransaccion());

        btnVerTodos.setOnClickListener(v -> cargarTransaccionesTiempoReal());

        btnCerrarSesion.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "👋 Sesión cerrada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        btnAdjuntarFoto.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            String concepto = etConcepto.getText().toString().trim();
            String montoStr = etMonto.getText().toString().trim();
            String tipo = etTipo.getText().toString().trim();

            if (id.isEmpty() || concepto.isEmpty() || montoStr.isEmpty() || tipo.isEmpty()) {
                Toast.makeText(this, "⚠️ Para guardar con foto, primero llena todos los datos del formulario.", Toast.LENGTH_LONG).show();
                return;
            }

            double monto;
            try {
                monto = Double.parseDouble(montoStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "⚠️ El monto debe ser un número válido.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(MainActivity.this, SubirFotoActivity.class);
            intent.putExtra("idTransaccion", id);
            intent.putExtra("concepto", concepto);
            intent.putExtra("monto", monto);
            intent.putExtra("tipo", tipo);
            startActivity(intent);
        });

        btnAbrirMapa.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapaActivity.class);
            startActivity(intent);
        });

        // 🔹 EVENTO DEL BOTÓN DE UBICACIÓN GPS
        btnAbrirUbicacion.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UbicacionActivity.class);
            startActivity(intent);
        });

        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        String rolCached = prefs.getString("rol", "Vendedor");
        aplicarPermisosDeRol(rolCached);

        cargarTransaccionesTiempoReal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarTransaccionesTiempoReal();
    }

    private void aplicarPermisosDeRol(String rol) {
        if (rol.equals("Vendedor")) {
            btnBorrar.setVisibility(View.GONE);
        } else if (rol.equals("Administrador")) {
            btnBorrar.setVisibility(View.VISIBLE);
            btnEditar.setVisibility(View.VISIBLE);
        }
    }

    private void configurarValidaciones() {
        etConcepto.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (limpiandoCampos) return;
                if (s.length() < 3 && s.length() > 0) {
                    tilConcepto.setError("⚠️ El concepto debe tener al menos 3 caracteres");
                } else {
                    tilConcepto.setErrorEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        etMonto.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (limpiandoCampos) return;
                String texto = s.toString();
                if (!texto.isEmpty()) {
                    try {
                        double monto = Double.parseDouble(texto);
                        if (monto <= 0) {
                            tilMonto.setError("⚠️ El monto debe ser mayor a 0");
                        } else {
                            tilMonto.setErrorEnabled(false);
                        }
                    } catch (NumberFormatException e) {
                        tilMonto.setError("⚠️ Ingresa un número válido");
                    }
                } else {
                    tilMonto.setError("💰 El monto no puede estar vacío");
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void agregarTransaccion() {
        String id = etId.getText().toString().trim();
        String concepto = etConcepto.getText().toString().trim();
        String montoStr = etMonto.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();

        if (id.isEmpty() || concepto.isEmpty() || montoStr.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(this, "❗ Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "❗ El monto debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnAgregar.setEnabled(false);

        db.collection("transacciones").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        progressBar.setVisibility(View.GONE);
                        btnAgregar.setEnabled(true);
                        Toast.makeText(this, "⚠️ Ya existe una transacción con ese ID. Usa otro.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Map<String, Object> transaccionMap = new HashMap<>();
                    transaccionMap.put("id", id);
                    transaccionMap.put("concepto", concepto);
                    transaccionMap.put("monto", monto);
                    transaccionMap.put("tipo", tipo);
                    transaccionMap.put("userId", currentUser.getUid());
                    transaccionMap.put("fecha", System.currentTimeMillis());

                    db.collection("transacciones").document(id)
                            .set(transaccionMap)
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                btnAgregar.setEnabled(true);
                                Toast.makeText(MainActivity.this, "✅ Transacción agregada", Toast.LENGTH_SHORT).show();

                                lanzarNotificacionLocal("¡Transacción Registrada!", "Se agregó '" + concepto + "' por $" + monto);

                                limpiarCampos();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnAgregar.setEnabled(true);
                                Toast.makeText(MainActivity.this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnAgregar.setEnabled(true);
                    Toast.makeText(MainActivity.this, "❌ Error al verificar ID", Toast.LENGTH_SHORT).show();
                });
    }

    private void buscarTransaccion() {
        String id = etId.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(this, "✏️ Ingresa un ID para buscar", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        db.collection("transacciones").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        etConcepto.setText(documentSnapshot.getString("concepto"));
                        etMonto.setText(String.valueOf(documentSnapshot.getDouble("monto")));
                        etTipo.setText(documentSnapshot.getString("tipo"));

                        String urlFoto = documentSnapshot.getString("urlFoto");
                        if (urlFoto != null && !urlFoto.isEmpty()) {
                            ivFotoTransaccion.setVisibility(View.VISIBLE);
                            Glide.with(this).load(urlFoto).into(ivFotoTransaccion);
                        } else {
                            ivFotoTransaccion.setVisibility(View.GONE);
                        }

                        Toast.makeText(this, "🔍 Transacción encontrada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "😅 No se encontró la transacción con ID: " + id, Toast.LENGTH_SHORT).show();
                        limpiarCampos();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "❌ Error de conexión", Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarTransaccion() {
        String id = etId.getText().toString().trim();
        String concepto = etConcepto.getText().toString().trim();
        String montoStr = etMonto.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();

        if (id.isEmpty() || concepto.isEmpty() || montoStr.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(this, "❗ Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "❗ El monto debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnEditar.setEnabled(false);

        db.collection("transacciones").document(id)
                .update("concepto", concepto, "monto", monto, "tipo", tipo)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnEditar.setEnabled(true);
                    Toast.makeText(this, "✅ Transacción actualizada", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnEditar.setEnabled(true);
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void eliminarTransaccion() {
        String id = etId.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(this, "✏️ Ingresa un ID para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        btnBorrar.setEnabled(false);
        db.collection("transacciones").document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnBorrar.setEnabled(true);
                    Toast.makeText(this, "🗑️ Transacción eliminada", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnBorrar.setEnabled(true);
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarTransaccionesTiempoReal() {
        if (listenerFirestore != null) {
            listenerFirestore.remove();
        }
        progressBar.setVisibility(View.VISIBLE);
        listenerFirestore = db.collection("transacciones")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        listaTransacciones.clear();
                        for (QueryDocumentSnapshot documento : value) {
                            Transaccion transaccion = documento.toObject(Transaccion.class);
                            listaTransacciones.add(transaccion);
                        }
                        adaptador.notifyDataSetChanged();
                    }
                });
    }

    private void limpiarCampos() {
        limpiandoCampos = true;

        etId.setText("");
        etConcepto.setText("");
        etMonto.setText("");
        etTipo.setText("");
        ivFotoTransaccion.setVisibility(View.GONE);

        tilMonto.setErrorEnabled(false);
        tilConcepto.setErrorEnabled(false);

        etId.clearFocus();
        etConcepto.clearFocus();
        etMonto.clearFocus();
        etTipo.clearFocus();

        limpiandoCampos = false;
    }

    private void crearCanalDeNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence nombreCanal = "Alertas de Finanzas";
            String descripcion = "Notificaciones de nuevas transacciones";
            int importancia = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel canal = new NotificationChannel(CANAL_ID, nombreCanal, importancia);
            canal.setDescription(descripcion);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    private void lanzarNotificacionLocal(String titulo, String mensaje) {
        Intent destinoIntent = new Intent(this, MainActivity.class);
        destinoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                destinoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CANAL_ID)
                .setSmallIcon(R.drawable.ic_caja)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                managerCompat.notify((int) System.currentTimeMillis(), builder.build());
            }
        } else {
            managerCompat.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerFirestore != null) {
            listenerFirestore.remove();
        }
    }
}