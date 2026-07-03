package com.example.gestorfinanzasnicol;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnAgregar, btnBuscar, btnEditar, btnBorrar, btnVerTodos;
    private EditText etId, etConcepto, etMonto, etTipo;
    private RecyclerView rvTransacciones;
    private AdaptadorTransaccion adaptador;
    private List<Transaccion> listaTransacciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        etId = findViewById(R.id.etId);
        etConcepto = findViewById(R.id.etConcepto);
        etMonto = findViewById(R.id.etMonto);
        etTipo = findViewById(R.id.etTipo);
        btnAgregar = findViewById(R.id.btnAgregar);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnEditar = findViewById(R.id.btnEditar);
        btnBorrar = findViewById(R.id.btnBorrar);
        btnVerTodos = findViewById(R.id.btnVerTodos);
        rvTransacciones = findViewById(R.id.rvTransacciones);

        rvTransacciones.setLayoutManager(new LinearLayoutManager(this));

        // Configurar eventos
        btnVerTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarListaTransacciones();
            }
        });

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarTransaccion();
            }
        });

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarTransaccion();
            }
        });

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editarTransaccion();
            }
        });

        btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarTransaccion();
            }
        });

        cargarListaTransacciones();
    }

    // Método para registrar una nueva transacción
    private void registrarTransaccion() {
        String concepto = etConcepto.getText().toString();
        String montoStr = etMonto.getText().toString();
        String tipo = etTipo.getText().toString();

        if (!concepto.isEmpty() && !montoStr.isEmpty() && !tipo.isEmpty()) {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "finanzas.db", null, 1);
            SQLiteDatabase db = admin.getWritableDatabase();

            ContentValues registro = new ContentValues();
            registro.put("concepto", concepto);
            registro.put("monto", montoStr);
            registro.put("tipo", tipo);

            db.insert("transacciones", null, registro);
            db.close();

            limpiarCampos();
            Toast.makeText(this, "🎉 ¡Transacción registrada!", Toast.LENGTH_SHORT).show();
            cargarListaTransacciones();
        } else {
            Toast.makeText(this, "❗ Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para buscar una transacción por ID
    private void buscarTransaccion() {
        String idStr = etId.getText().toString();

        if (!idStr.isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "finanzas.db", null, 1);
                SQLiteDatabase db = admin.getReadableDatabase();

                android.database.Cursor fila = db.rawQuery(
                        "SELECT concepto, monto, tipo FROM transacciones WHERE id = " + id, null);

                if (fila.moveToFirst()) {
                    etConcepto.setText(fila.getString(0));
                    etMonto.setText(String.valueOf(fila.getDouble(1)));
                    etTipo.setText(fila.getString(2));
                    Toast.makeText(this, "🔍 ¡Transacción encontrada! (ID: " + id + ")", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "😅 No encontré la transacción con ID: " + id, Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                    mostrarIdsDisponibles();
                }

                db.close();
                fila.close();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "❗ El ID debe ser un número", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "✏️ Ingresa un ID para buscar", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para editar una transacción
    private void editarTransaccion() {
        String idStr = etId.getText().toString();
        String concepto = etConcepto.getText().toString();
        String montoStr = etMonto.getText().toString();
        String tipo = etTipo.getText().toString();

        if (!idStr.isEmpty() && !concepto.isEmpty() && !montoStr.isEmpty() && !tipo.isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);

                AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "finanzas.db", null, 1);
                SQLiteDatabase db = admin.getWritableDatabase();

                ContentValues registroNuevo = new ContentValues();
                registroNuevo.put("concepto", concepto);
                registroNuevo.put("monto", montoStr);
                registroNuevo.put("tipo", tipo);

                int cantidadActualizadas = db.update("transacciones", registroNuevo, "id=" + id, null);
                db.close();

                if (cantidadActualizadas == 1) {
                    Toast.makeText(this, "✅ ¡Transacción actualizada!", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                    cargarListaTransacciones();
                } else {
                    Toast.makeText(this, "😅 No encontré esa transacción", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "❗ El ID debe ser un número", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "❗ Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
        }
    }

    // ️ Método para eliminar una transacción
    private void borrarTransaccion() {
        String idStr = etId.getText().toString();

        if (!idStr.isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "finanzas.db", null, 1);
                SQLiteDatabase db = admin.getWritableDatabase();

                int cantidadBorrados = db.delete("transacciones", "id=" + id, null);
                db.close();

                limpiarCampos();

                if (cantidadBorrados == 1) {
                    Toast.makeText(this, "🗑️ ¡Transacción eliminada!", Toast.LENGTH_SHORT).show();
                    cargarListaTransacciones();
                } else {
                    Toast.makeText(this, "😅 No encontré esa transacción", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "❗ El ID debe ser un número", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "✏️ Ingresa un ID para eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para cargar todas las transacciones en el RecyclerView
    private void cargarListaTransacciones() {
        listaTransacciones = new ArrayList<>();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "finanzas.db", null, 1);
        SQLiteDatabase db = admin.getReadableDatabase();

        android.database.Cursor fila = db.rawQuery(
                "SELECT id, concepto, monto, tipo FROM transacciones ORDER BY id DESC", null);

        while (fila.moveToNext()) {
            int id = fila.getInt(0);
            String concepto = fila.getString(1);
            double monto = fila.getDouble(2);
            String tipo = fila.getString(3);

            listaTransacciones.add(new Transaccion(id, concepto, monto, tipo));
        }

        db.close();
        fila.close();

        adaptador = new AdaptadorTransaccion(listaTransacciones);
        rvTransacciones.setAdapter(adaptador);

        if (listaTransacciones.isEmpty()) {
            Toast.makeText(this, "📭 No hay transacciones aún. ¡Agrega una!", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para limpiar todos los campos
    private void limpiarCampos() {
        etId.setText("");
        etConcepto.setText("");
        etMonto.setText("");
        etTipo.setText("");
    }

    // Método auxiliar para mostrar los ID disponibles
    private void mostrarIdsDisponibles() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "finanzas.db", null, 1);
        SQLiteDatabase db = admin.getReadableDatabase();

        android.database.Cursor fila = db.rawQuery("SELECT id FROM transacciones", null);

        StringBuilder ids = new StringBuilder("📋 ID disponibles: ");
        while (fila.moveToNext()) {
            ids.append(fila.getInt(0)).append(" ");
        }

        db.close();
        fila.close();

        if (ids.toString().equals("📋 ID disponibles: ")) {
            Toast.makeText(this, "📭 No hay transacciones registradas", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, ids.toString(), Toast.LENGTH_LONG).show();
        }
    }
}