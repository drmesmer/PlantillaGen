package com.plantillagen.model;

import java.time.LocalDate;

public class CalendarioEntry {

    private int id;
    private int plantillaId;
    private LocalDate fecha;
    private boolean activo;
    private String plantillaNombre;

    public CalendarioEntry() {}

    public CalendarioEntry(int plantillaId, LocalDate fecha, boolean activo) {
        this.plantillaId = plantillaId;
        this.fecha = fecha;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPlantillaId() { return plantillaId; }
    public void setPlantillaId(int plantillaId) { this.plantillaId = plantillaId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getPlantillaNombre() { return plantillaNombre; }
    public void setPlantillaNombre(String plantillaNombre) { this.plantillaNombre = plantillaNombre; }
}
