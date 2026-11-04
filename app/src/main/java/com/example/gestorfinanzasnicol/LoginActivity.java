package com.example.gestorfinanzasnicol;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvIrRegistro;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etLoginCorreo);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrRegistro = findViewById(R.id.tvIrRegistro);
        progressBar = findViewById(R.id.progressBarLogin);

        // Auto-Login: Si ya hay sesión, saltar a MainActivity
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        tvIrRegistro.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegistroActivity.class)));

        btnLogin.setOnClickListener(v -> iniciarSesion());
    }

    private void iniciarSesion() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // Buscar el rol en Firestore
                        db.collection("usuarios").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String nombre = documentSnapshot.getString("nombre");
                                        String rol = documentSnapshot.getString("rol");

                                        // Guardar en SharedPreferences (Caché local)
                                        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("nombre", nombre);
                                        editor.putString("rol", rol);
                                        editor.apply();

                                        progressBar.setVisibility(View.GONE);
                                        btnLogin.setEnabled(true);
                                        Toast.makeText(LoginActivity.this, "✅ Bienvenido " + nombre, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        btnLogin.setEnabled(true);
                                        Toast.makeText(LoginActivity.this, "No se encontró el perfil", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnLogin.setEnabled(true);
                                    Toast.makeText(LoginActivity.this, "Error al leer perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "❌ Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}