package com.plantillagen.model;

public class LineaProduccion {

    private int id;
    private int posicion;
    private String nombre;
    private boolean activo;
    private String color = "#A0A0A0";
    private String categoria = "";

    public LineaProduccion() {}

    public LineaProduccion(int id, int posicion, String nombre, boolean activo) {
        this(id, posicion, nombre, activo, "#A0A0A0", "");
    }

    public LineaProduccion(int id, int posicion, String nombre, boolean activo, String color) {
        this(id, posicion, nombre, activo, color, "");
    }

    public LineaProduccion(int id, int posicion, String nombre, boolean activo, String color, String categoria) {
        this.id = id;
        this.posicion = posicion;
        this.nombre = nombre;
        this.activo = activo;
        this.color = color;
        this.categoria = categoria;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    @Override
    public String toString() {
        return posicion + " - " + nombre;
    }
}
