package com.example.gestorfinanzasnicol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private EditText etId, etConcepto, etMonto, etTipo;
    private TextInputLayout tilConcepto, tilMonto;
    private Button btnAgregar, btnBuscar, btnEditar, btnBorrar, btnVerTodos, btnCerrarSesion;
    private RecyclerView rvTransacciones;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private AdaptadorTransaccion adaptador;
    private List<Transaccion> listaTransacciones;
    private com.google.firebase.firestore.ListenerRegistration listenerFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "⚠️ Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
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
        rvTransacciones = findViewById(R.id.rvTransacciones);
        progressBar = findViewById(R.id.progressBarMain);

        // Configurar RecyclerView
        rvTransacciones.setLayoutManager(new LinearLayoutManager(this));
        rvTransacciones.setNestedScrollingEnabled(true);

        // Inicializar adaptador
        listaTransacciones = new ArrayList<>();
        adaptador = new AdaptadorTransaccion(listaTransacciones, transaccion -> {
            etId.setText(transaccion.getId());
            etConcepto.setText(transaccion.getConcepto());
            etMonto.setText(String.valueOf(transaccion.getMonto()));
            etTipo.setText(transaccion.getTipo());
            Toast.makeText(this, "📝 Cargado para editar", Toast.LENGTH_SHORT).show();
        });
        rvTransacciones.setAdapter(adaptador);

        // 🔹 VALIDACIONES EN TIEMPO REAL (TextWatcher)
        configurarValidaciones();

        // Configurar eventos
        btnAgregar.setOnClickListener(v -> agregarTransaccion());
        btnBuscar.setOnClickListener(v -> buscarTransaccion());
        btnEditar.setOnClickListener(v -> actualizarTransaccion());
        btnBorrar.setOnClickListener(v -> eliminarTransaccion());

        btnVerTodos.setOnClickListener(v -> {
            cargarTransaccionesTiempoReal();
            Toast.makeText(this, "🔄 Lista actualizada", Toast.LENGTH_SHORT).show();
        });

        btnCerrarSesion.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "👋 Sesión cerrada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        // Cargar transacciones al iniciar
        cargarTransaccionesTiempoReal();
    }

    // 🔹 CONFIGURAR VALIDACIONES EN TIEMPO REAL (TEXTWATCHER)
    private void configurarValidaciones() {
        // Validar concepto (mínimo 3 caracteres)
        etConcepto.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 3 && s.length() > 0) {
                    tilConcepto.setError("⚠️ El concepto debe tener al menos 3 caracteres");
                } else {
                    tilConcepto.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Validar monto (debe ser mayor a 0)
        etMonto.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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

    // 🔹 MÉTODO: Agregar transacción con ID personalizado
    private void agregarTransaccion() {
        String id = etId.getText().toString().trim();
        String concepto = etConcepto.getText().toString().trim();
        String montoStr = etMonto.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();

        // Validar ID
        if (id.isEmpty()) {
            Toast.makeText(this, "❗ Ingresa un ID para la transacción", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar concepto (mínimo 3 caracteres)
        if (concepto.isEmpty()) {
            Toast.makeText(this, "❗ El concepto no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        if (concepto.length() < 3) {
            Toast.makeText(this, "❗ El concepto debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar monto
        if (montoStr.isEmpty()) {
            Toast.makeText(this, "❗ El monto no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
            if (monto <= 0) {
                Toast.makeText(this, "❗ El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "❗ El monto debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar tipo
        if (tipo.isEmpty()) {
            Toast.makeText(this, "❗ El tipo no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!tipo.equals("Ingreso") && !tipo.equals("Gasto")) {
            Toast.makeText(this, "❗ El tipo debe ser 'Ingreso' o 'Gasto'", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnAgregar.setEnabled(false);

        // Verificar si el ID ya existe en Firestore
        db.collection("transacciones").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        progressBar.setVisibility(View.GONE);
                        btnAgregar.setEnabled(true);
                        Toast.makeText(this, "⚠️ Ya existe una transacción con ese ID", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Si no existe, guardar la transacción
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
                                Toast.makeText(MainActivity.this, "✅ Transacción agregada con ID: " + id, Toast.LENGTH_SHORT).show();
                                limpiarCampos();
                                cargarTransaccionesTiempoReal();
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
                    Toast.makeText(MainActivity.this, "❌ Error al verificar ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 🔹 MÉTODO: Buscar transacción por ID
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
                        String concepto = documentSnapshot.getString("concepto");
                        Double monto = documentSnapshot.getDouble("monto");
                        String tipo = documentSnapshot.getString("tipo");

                        etConcepto.setText(concepto);
                        etMonto.setText(String.valueOf(monto));
                        etTipo.setText(tipo);

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

    // 🔹 MÉTODO: Actualizar transacción
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
                    cargarTransaccionesTiempoReal();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnEditar.setEnabled(true);
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 🔹 MÉTODO: Eliminar transacción
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
                    cargarTransaccionesTiempoReal();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnBorrar.setEnabled(true);
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 🔹 MÉTODO: Cargar transacciones en tiempo real (LISTENER)
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
                        Toast.makeText(this, "❌ Error al cargar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        listaTransacciones.clear();
                        for (QueryDocumentSnapshot documento : value) {
                            Transaccion transaccion = documento.toObject(Transaccion.class);
                            listaTransacciones.add(transaccion);
                        }
                        adaptador.notifyDataSetChanged();

                        if (listaTransacciones.isEmpty()) {
                            Toast.makeText(this, "📭 No hay transacciones", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // 🔹 MÉTODO: Limpiar campos
    private void limpiarCampos() {
        etId.setText("");
        etConcepto.setText("");
        etMonto.setText("");
        etTipo.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerFirestore != null) {
            listenerFirestore.remove();
        }
    }
}