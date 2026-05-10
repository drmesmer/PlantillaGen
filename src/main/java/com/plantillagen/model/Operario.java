package com.plantillagen.model;

import java.awt.Image;
import java.io.Serializable;

public class Operario implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String codigo;
    private String nombre;
    private boolean activo = true;
    private int efi = 50;
    private int cal = 50;
    private int seg = 50;
    private int ini = 50;
    private int pol = 50;
    private transient Image foto;

    public Operario() {}

    public Operario(int id, String codigo, String nombre) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Operario(String codigo, String nombre, Image foto) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.foto = foto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getEfi() { return efi; }
    public void setEfi(int efi) { this.efi = efi; }
    public int getCal() { return cal; }
    public void setCal(int cal) { this.cal = cal; }
    public int getSeg() { return seg; }
    public void setSeg(int seg) { this.seg = seg; }
    public int getIni() { return ini; }
    public void setIni(int ini) { this.ini = ini; }
    public int getPol() { return pol; }
    public void setPol(int pol) { this.pol = pol; }

    public Image getFoto() {
        return foto;
    }

    public void setFoto(Image foto) {
        this.foto = foto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Operario)) return false;
        Operario op = (Operario) o;
        return codigo.equals(op.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
