package com.example.gestorfinanzasnicol;

public class Transaccion {
    private int id;
    private String concepto;
    private double monto;
    private String tipo;

    public Transaccion(int id, String concepto, double monto, String tipo) {
        this.id = id;
        this.concepto = concepto;
        this.monto = monto;
        this.tipo = tipo;
    }

    public int getId() { return id; }
    public String getConcepto() { return concepto; }
    public double getMonto() { return monto; }
    public String getTipo() { return tipo; }
}