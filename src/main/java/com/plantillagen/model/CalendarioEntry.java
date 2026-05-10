package com.plantillagen.model;

import java.time.LocalDate;

public class CalendarioEntry {

    private int id;
    private int lineaId;
    private int turnoId;
    private LocalDate fecha;
    private boolean activo;
    private String lineaNombre;
    private String turnoNombre;

    public CalendarioEntry() {}

    public CalendarioEntry(int lineaId, int turnoId, LocalDate fecha, boolean activo) {
        this.lineaId = lineaId;
        this.turnoId = turnoId;
        this.fecha = fecha;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLineaId() { return lineaId; }
    public void setLineaId(int lineaId) { this.lineaId = lineaId; }

    public int getTurnoId() { return turnoId; }
    public void setTurnoId(int turnoId) { this.turnoId = turnoId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getLineaNombre() { return lineaNombre; }
    public void setLineaNombre(String lineaNombre) { this.lineaNombre = lineaNombre; }

    public String getTurnoNombre() { return turnoNombre; }
    public void setTurnoNombre(String turnoNombre) { this.turnoNombre = turnoNombre; }
}
