package com.plantillagen.ui;

import com.plantillagen.model.Operario;

import javax.swing.table.AbstractTableModel;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public class OperarioTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Foto", "C\u00f3digo", "Nombre"};
    private final List<Operario> operarios;

    public OperarioTableModel() {
        this.operarios = new ArrayList<>();
    }

    public void addOperario(Operario op) {
        operarios.add(op);
        fireTableRowsInserted(operarios.size() - 1, operarios.size() - 1);
    }

    public void insertOperarioAt(int index, Operario op) {
        if (index < 0 || index > operarios.size()) {
            index = operarios.size();
        }
        operarios.add(index, op);
        fireTableRowsInserted(index, index);
    }

    public Operario removeOperario(int row) {
        if (row < 0 || row >= operarios.size()) return null;
        Operario op = operarios.remove(row);
        fireTableRowsDeleted(row, row);
        return op;
    }

    public Operario getOperarioAt(int row) {
        if (row < 0 || row >= operarios.size()) return null;
        return operarios.get(row);
    }

    public void removeOperario(Operario op) {
        int idx = operarios.indexOf(op);
        if (idx >= 0) {
            removeOperario(idx);
        }
    }

    public boolean contains(Operario op) {
        return operarios.contains(op);
    }

    public int getOperarioCount() {
        return operarios.size();
    }

    @Override
    public int getRowCount() {
        return operarios.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return col == 0 ? Image.class : String.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Operario op = operarios.get(row);
        switch (col) {
            case 0: return op.getFoto();
            case 1: return op.getCodigo();
            case 2: return op.getNombre();
        }
        return null;
    }
}
