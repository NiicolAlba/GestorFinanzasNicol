package com.example.gestorfinanzasnicol;

public class Transaccion {
    private String id;
    private String concepto;
    private double monto;
    private String tipo;
    private String userId;
    private long fecha;

    // Constructor vacío (necesario para Firestore)
    public Transaccion() {}

    // Constructor completo
    public Transaccion(String id, String concepto, double monto, String tipo, String userId, long fecha) {
        this.id = id;
        this.concepto = concepto;
        this.monto = monto;
        this.tipo = tipo;
        this.userId = userId;
        this.fecha = fecha;
    }

    // Getters
    public String getId() { return id; }
    public String getConcepto() { return concepto; }
    public double getMonto() { return monto; }
    public String getTipo() { return tipo; }
    public String getUserId() { return userId; }
    public long getFecha() { return fecha; }

    // Setters (necesarios para Firestore)
    public void setId(String id) { this.id = id; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public void setMonto(double monto) { this.monto = monto; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setFecha(long fecha) { this.fecha = fecha; }
}