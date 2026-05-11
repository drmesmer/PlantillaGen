package com.plantillagen.model;

import java.sql.Timestamp;

public class Usuario {

    private int id;
    private String codigo;
    private String password;
    private Timestamp ultimaSesion;

    public Usuario() {}

    public Usuario(String codigo, String password) {
        this.codigo = codigo;
        this.password = password;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Timestamp getUltimaSesion() { return ultimaSesion; }
    public void setUltimaSesion(Timestamp ultimaSesion) { this.ultimaSesion = ultimaSesion; }
}
