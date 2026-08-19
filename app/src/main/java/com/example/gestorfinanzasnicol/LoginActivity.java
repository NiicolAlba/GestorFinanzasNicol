package com.example.gestorfinanzasnicol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvIrRegistro;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etLoginCorreo);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrRegistro = findViewById(R.id.tvIrRegistro);
        progressBar = findViewById(R.id.progressBarLogin);

        tvIrRegistro.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
        });

        btnLogin.setOnClickListener(v -> iniciarSesion());
    }

    private void iniciarSesion() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("El correo es obligatorio");
            etEmail.requestFocus();
            mostrarMensaje("⚠️ Ingresa tu correo electrónico");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("La contraseña es obligatoria");
            etPassword.requestFocus();
            mostrarMensaje("⚠️ Ingresa tu contraseña");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        mostrarMensaje("✅ Bienvenido " + user.getEmail());
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Exception exception = task.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "Error desconocido";

                        android.util.Log.e("LOGIN_ERROR", "❌ Error: " + errorMessage);

                        String mensajeAmigable = obtenerMensajeAmigable(errorMessage);
                        mostrarMensaje(mensajeAmigable);
                    }
                });
    }

    private void mostrarMensaje(String mensaje) {
        View view = findViewById(android.R.id.content);
        Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG).show();
    }

    private String obtenerMensajeAmigable(String error) {
        if (error == null) return "❌ Error desconocido. Intenta de nuevo.";

        // 🔹 CONVERTIR A MINÚSCULAS PARA FACILITAR LA COMPARACIÓN
        String errorLower = error.toLowerCase();

        // 🔹 TRADUCCIONES MEJORADAS DE ERRORES DE LOGIN
        if (errorLower.contains("there is no user record") ||
                errorLower.contains("user not found")) {
            return "❌ No existe una cuenta con ese correo electrónico. ¿Quieres registrarte?";
        }
        else if (errorLower.contains("the password is invalid") ||
                errorLower.contains("does not have a password") ||
                errorLower.contains("invalid password")) {
            return "❌ Contraseña incorrecta. Intenta de nuevo.";
        }
        else if (errorLower.contains("the email address is badly formatted") ||
                errorLower.contains("invalid email")) {
            return "❌ Correo electrónico inválido. Verifica el formato.";
        }
        else if (errorLower.contains("too many attempts") ||
                errorLower.contains("too many requests")) {
            return "⏳ Demasiados intentos. Espera un momento y vuelve a intentar.";
        }
        else if (errorLower.contains("network error") ||
                errorLower.contains("network")) {
            return "📶 Sin conexión a Internet. Verifica tu red.";
        }
        else if (errorLower.contains("the supplied auth credential is incorrect") ||
                errorLower.contains("malformed") ||
                errorLower.contains("has expired")) {
            return "❌ Credenciales incorrectas. Verifica tu correo y contraseña.";
        }
        else {
            return "❌ Error: " + error;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}