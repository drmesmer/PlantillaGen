package com.plantillagen.model;

public class PlantillaEntry {

    private int id;
    private int lineaId;
    private int operarioId;
    private String operarioCodigo;
    private String operarioNombre;
    private boolean esLider;
    private boolean tieneFormacion;
    private int orden;
    private int turnoId = 1;

    public PlantillaEntry() {}

    public PlantillaEntry(int lineaId, int operarioId, boolean esLider, boolean tieneFormacion, int orden) {
        this.lineaId = lineaId;
        this.operarioId = operarioId;
        this.esLider = esLider;
        this.tieneFormacion = tieneFormacion;
        this.orden = orden;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLineaId() { return lineaId; }
    public void setLineaId(int lineaId) { this.lineaId = lineaId; }

    public int getOperarioId() { return operarioId; }
    public void setOperarioId(int operarioId) { this.operarioId = operarioId; }

    public String getOperarioCodigo() { return operarioCodigo; }
    public void setOperarioCodigo(String operarioCodigo) { this.operarioCodigo = operarioCodigo; }

    public String getOperarioNombre() { return operarioNombre; }
    public void setOperarioNombre(String operarioNombre) { this.operarioNombre = operarioNombre; }

    public boolean isEsLider() { return esLider; }
    public void setEsLider(boolean esLider) { this.esLider = esLider; }

    public boolean isTieneFormacion() { return tieneFormacion; }
    public void setTieneFormacion(boolean tieneFormacion) { this.tieneFormacion = tieneFormacion; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public int getTurnoId() { return turnoId; }
    public void setTurnoId(int turnoId) { this.turnoId = turnoId; }
}
