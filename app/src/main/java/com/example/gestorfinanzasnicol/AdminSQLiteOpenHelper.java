package com.example.gestorfinanzasnicol;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    public AdminSQLiteOpenHelper(@Nullable Context context
            , @Nullable String name
            , @Nullable SQLiteDatabase.CursorFactory factory
            , int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla de transacciones
        String query = "CREATE TABLE transacciones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "concepto TEXT, " +
                "monto REAL, " +
                "tipo TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS transacciones");
        onCreate(db);
    }
}