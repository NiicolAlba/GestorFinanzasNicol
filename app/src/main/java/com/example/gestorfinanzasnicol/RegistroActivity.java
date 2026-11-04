package com.example.gestorfinanzasnicol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombreRegistro, etEmail, etPassword, etConfirmPassword;
    private Button btnRegistrarme;
    private TextView tvVolverLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etNombreRegistro = findViewById(R.id.etNombreRegistro);
        etEmail = findViewById(R.id.etRegistroCorreo);
        etPassword = findViewById(R.id.etRegistroPassword);
        etConfirmPassword = findViewById(R.id.etRegistroConfirmacion);
        btnRegistrarme = findViewById(R.id.btnRegistrarme);
        tvVolverLogin = findViewById(R.id.tvVolverLogin);
        progressBar = findViewById(R.id.progressBarRegistro);

        tvVolverLogin.setOnClickListener(v -> finish());

        btnRegistrarme.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = etNombreRegistro.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegistrarme.setEnabled(false);

        // 1. Crear el usuario en Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // 2. Guardar el perfil en Firestore (Colección "usuarios")
                        Map<String, Object> perfil = new HashMap<>();
                        perfil.put("nombre", nombre);
                        perfil.put("correo", email);
                        perfil.put("rol", "Vendedor"); // Rol por defecto

                        db.collection("usuarios").document(uid).set(perfil)
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnRegistrarme.setEnabled(true);
                                    Toast.makeText(RegistroActivity.this, "✅ Usuario y perfil creados", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnRegistrarme.setEnabled(true);
                                    Toast.makeText(RegistroActivity.this, "Error al guardar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnRegistrarme.setEnabled(true);
                        Toast.makeText(RegistroActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}