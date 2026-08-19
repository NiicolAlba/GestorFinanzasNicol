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

public class RegistroActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnRegistrarme;
    private TextView tvVolverLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();

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
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 🔹 VALIDACIONES EN ESPAÑOL
        if (email.isEmpty()) {
            etEmail.setError("El correo es obligatorio");
            etEmail.requestFocus();
            mostrarMensaje("⚠️ Ingresa tu correo electrónico");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("La contraseña es obligatoria");
            etPassword.requestFocus();
            mostrarMensaje("⚠️ Ingresa una contraseña");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            etPassword.requestFocus();
            mostrarMensaje("🔒 La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirma tu contraseña");
            etConfirmPassword.requestFocus();
            mostrarMensaje("⚠️ Confirma tu contraseña");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            mostrarMensaje("⚠️ Las contraseñas no coinciden");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegistrarme.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegistrarme.setEnabled(true);

                    if (task.isSuccessful()) {
                        mostrarMensaje("✅ Usuario registrado correctamente. Inicia sesión.");
                        startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        // 🔹 CAPTURAR EL ERROR REAL
                        Exception exception = task.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "Error desconocido";

                        // 🔹 MOSTRAR EN LOGCAT (PARA DIAGNÓSTICO)
                        android.util.Log.e("REGISTER_ERROR", "❌ Error: " + errorMessage);

                        // 🔹 MOSTRAR MENSAJE TRADUCIDO
                        String mensajeAmigable = obtenerMensajeAmigable(errorMessage);
                        mostrarMensaje(mensajeAmigable);
                    }
                });
    }

    // 🔹 MÉTODO PARA MOSTRAR MENSAJES CON SNACKBAR
    private void mostrarMensaje(String mensaje) {
        View view = findViewById(android.R.id.content);
        Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG).show();
    }

    // 🔹 MÉTODO PARA TRADUCIR ERRORES DE FIREBASE EN REGISTRO
    private String obtenerMensajeAmigable(String error) {
        if (error == null) return "❌ Error desconocido. Intenta de nuevo.";

        // 🔹 CONVERTIR A MINÚSCULAS PARA FACILITAR LA COMPARACIÓN
        String errorLower = error.toLowerCase();

        // 🔹 TRADUCCIONES PARA REGISTRO
        if (errorLower.contains("email already in use") ||
                errorLower.contains("already registered") ||
                errorLower.contains("error_email_already_in_use") ||
                errorLower.contains("the email address is already in use")) {
            return "📧 Este correo ya está registrado. Usa otro o inicia sesión.";
        }
        else if (errorLower.contains("the email address is badly formatted") ||
                errorLower.contains("invalid email") ||
                errorLower.contains("error_invalid_email") ||
                errorLower.contains("badly formatted")) {
            return "❌ Correo electrónico inválido. Verifica el formato (ejemplo: correo@dominio.com).";
        }
        else if (errorLower.contains("weak password") ||
                errorLower.contains("password must be at least 6 characters") ||
                errorLower.contains("error_weak_password") ||
                errorLower.contains("at least 6 characters")) {
            return "🔒 La contraseña debe tener al menos 6 caracteres.";
        }
        else if (errorLower.contains("network error") ||
                errorLower.contains("network") ||
                errorLower.contains("network request failed")) {
            return "📶 Sin conexión a Internet. Verifica tu red y vuelve a intentar.";
        }
        else if (errorLower.contains("there is no user record") ||
                errorLower.contains("user not found")) {
            return "❌ No existe una cuenta con ese correo electrónico. ¿Quieres registrarte?";
        }
        else if (errorLower.contains("the password is invalid") ||
                errorLower.contains("does not have a password") ||
                errorLower.contains("invalid password")) {
            return "❌ Contraseña incorrecta. Intenta de nuevo.";
        }
        else if (errorLower.contains("too many attempts") ||
                errorLower.contains("too many requests")) {
            return "⏳ Demasiados intentos. Espera un momento y vuelve a intentar.";
        }
        else if (errorLower.contains("the supplied auth credential is incorrect") ||
                errorLower.contains("malformed") ||
                errorLower.contains("has expired") ||
                errorLower.contains("credential")) {
            return "❌ Credenciales incorrectas. Verifica tu correo y contraseña.";
        }
        else {
            // SI NO CONOCEMOS EL ERROR, MOSTRAMOS EL ORIGINAL CON UN MENSAJE CLARO
            return "❌ Error al registrar: " + error;
        }
    }
}