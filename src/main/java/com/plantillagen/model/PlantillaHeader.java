package com.plantillagen.model;

import java.sql.Timestamp;

public class PlantillaHeader {

    private int id;
    private String nombre;
    private String estado;
    private String color = "#4A90D9";
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public PlantillaHeader() {}

    public PlantillaHeader(String nombre, String estado) {
        this.nombre = nombre;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "[" + estado + "] " + nombre;
    }
}
