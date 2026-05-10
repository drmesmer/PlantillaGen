package com.plantillagen.ui;

import com.plantillagen.db.LineaDAO;
import com.plantillagen.model.LineaProduccion;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class LineasTabPanel extends JPanel {

    private List<LineaProduccion> lineas;
    private LineaProduccion selectedLinea;
    private int selectedIndex = -1;

    private JPanel listPanel;
    private JTextField filterField;
    private JTextField txtNumero;
    private JTextField txtNombre;
    private JTextField txtCategoria;
    private JCheckBox chkActivo;
    private JButton btnColor;

    private Color selectedColor = Color.GRAY;
    private Runnable onDataChanged;

    private static final Color ROW_EVEN = new Color(255, 255, 255);
    private static final Color ROW_ODD = new Color(247, 249, 252);
    private static final Color ROW_SELECTED_BORDER = new Color(120, 150, 210);
    private static final Color COLOR_ACTIVO = new Color(34, 170, 34);
    private static final Color COLOR_INACTIVO = new Color(200, 50, 50);
    private static final Color SEPARATOR = new Color(228, 231, 237);
    private static final DataFlavor INDEX_FLAVOR =
        new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.Integer", "LineIndex");

    public LineasTabPanel() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.22);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setLeftComponent(createLeftPanel());
        splitPane.setRightComponent(createRightPanel());
        splitPane.addHierarchyListener(e -> {
            if (splitPane.isDisplayable()) {
                splitPane.setDividerLocation(0.22);
            }
        });
        add(splitPane, BorderLayout.CENTER);
    }

    public void initData() {
        loadFromDB();
    }

    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }

    public void actionNuevo() {
        clearForm();
    }

    public void actionEliminar() {
        deleteLine();
    }

    public void actionGuardar() {
        saveLine();
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "L\u00cdNEAS",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Color.DARK_GRAY),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        filterField = new JTextField();
        filterField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterField.putClientProperty("JTextField.placeholderText",
            "Filtrar...");
        filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        panel.add(filterField, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setDropTarget(new DropTarget(listPanel,
            DnDConstants.ACTION_MOVE, new LineDropListener(), true));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "DETALLE DE L\u00cdNEA",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Color.DARK_GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel lblNum = new JLabel("Posici\u00f3n:");
        lblNum.setFont(labelFont);
        formPanel.add(lblNum, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        txtNumero = new JTextField(5);
        txtNumero.setFont(fieldFont);
        txtNumero.setEnabled(false);
        txtNumero.setToolTipText("La posici\u00f3n se gestiona arrastrando las filas");
        formPanel.add(txtNumero, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setFont(labelFont);
        formPanel.add(lblNombre, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        txtNombre = new JTextField(25);
        txtNombre.setFont(fieldFont);
        formPanel.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblCategoria = new JLabel("Categor\u00eda:");
        lblCategoria.setFont(labelFont);
        formPanel.add(lblCategoria, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        txtCategoria = new JTextField(10);
        txtCategoria.setFont(fieldFont);
        formPanel.add(txtCategoria, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel lblColor = new JLabel("Color:");
        lblColor.setFont(labelFont);
        formPanel.add(lblColor, gbc);

        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        colorPanel.setOpaque(false);

        btnColor = new JButton("  ");
        btnColor.setPreferredSize(new Dimension(32, 24));
        btnColor.setBackground(selectedColor);
        btnColor.setFocusPainted(false);
        btnColor.addActionListener(e -> pickColor());
        colorPanel.add(btnColor);

        JLabel lblColorHex = new JLabel("#A0A0A0");
        lblColorHex.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblColorHex.setName("lblColorHex");
        colorPanel.add(lblColorHex);

        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(colorPanel, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        chkActivo = new JCheckBox("L\u00ednea activa", true);
        chkActivo.setFont(labelFont);
        chkActivo.setOpaque(false);
        formPanel.add(chkActivo, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        return panel;
    }

    private void pickColor() {
        Color newColor = JColorChooser.showDialog(this, "Color de l\u00ednea", selectedColor);
        if (newColor != null) {
            selectedColor = newColor;
            btnColor.setBackground(selectedColor);
            updateColorLabel();
        }
    }

    private void updateColorLabel() {
        String hex = String.format("#%02X%02X%02X",
            selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue());
        for (Component c : ((JPanel) btnColor.getParent()).getComponents()) {
            if (c instanceof JLabel && "lblColorHex".equals(c.getName())) {
                ((JLabel) c).setText(hex);
            }
        }
    }

    private void loadFromDB() {
        try {
            LineaDAO dao = new LineaDAO();
            lineas = dao.findAllIncludeInactive();
            refreshList();
        } catch (Exception e) {
            e.printStackTrace();
            lineas = new ArrayList<>();
        }
    }

    private void refreshList() {
        listPanel.removeAll();
        for (int i = 0; i < lineas.size(); i++) {
            listPanel.add(createLineRow(lineas.get(i), i));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createLineRow(LineaProduccion lp, int index) {
        final int idx = index;
        JPanel row = new JPanel(new BorderLayout(4, 0)) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                Color lineCol = parseColor(lp.getColor());
                Color tint = lightTint(lineCol);
                if (idx == selectedIndex) {
                    g.setColor(tint.darker());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(lineCol);
                    g.fillRect(0, 0, 4, getHeight());
                    g.fillRect(getWidth() - 4, 0, 4, getHeight());
                } else {
                    g.setColor(tint);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                g.setColor(SEPARATOR);
                g.drawLine(10, getHeight() - 1, getWidth() - 10, getHeight() - 1);
            }
        };
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setPreferredSize(new Dimension(0, 30));
        row.setOpaque(false);

        JLabel lblIndex = new JLabel(String.valueOf(index + 1), SwingConstants.CENTER);
        lblIndex.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblIndex.setForeground(new Color(150, 155, 165));
        lblIndex.setPreferredSize(new Dimension(20, 30));
        row.add(lblIndex, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new BorderLayout(4, 0));
        infoPanel.setOpaque(false);

        JLabel lblName = new JLabel(lp.getNombre() + (lp.getCategoria().isEmpty() ? "" : " [" + lp.getCategoria() + "]"));
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoPanel.add(lblName, BorderLayout.CENTER);

        JLabel lblActivo = new JLabel(lp.isActivo() ? "\u25CF" : "\u25CB");
        lblActivo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblActivo.setForeground(lp.isActivo() ? COLOR_ACTIVO : COLOR_INACTIVO);
        lblActivo.setPreferredSize(new Dimension(18, 30));
        lblActivo.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(lblActivo, BorderLayout.EAST);

        row.add(infoPanel, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectLine(idx);
            }
        });
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
            row, DnDConstants.ACTION_MOVE, new DragGestureListener() {
                public void dragGestureRecognized(DragGestureEvent dge) {
                    if (idx < 0 || idx >= lineas.size()) return;
                    selectLineNoRefresh(idx);
                    Transferable transferable = new Transferable() {
                        public DataFlavor[] getTransferDataFlavors() {
                            return new DataFlavor[]{INDEX_FLAVOR};
                        }
                        public boolean isDataFlavorSupported(DataFlavor f) {
                            return INDEX_FLAVOR.equals(f);
                        }
                        public Object getTransferData(DataFlavor f)
                                throws UnsupportedFlavorException {
                            if (!isDataFlavorSupported(f))
                                throw new UnsupportedFlavorException(f);
                            return idx;
                        }
                    };
                    DragSource.getDefaultDragSource().startDrag(
                        dge, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR),
                        null, new Point(0, 0), transferable, null);
                }
            });

        return row;
    }

    private void selectLine(int index) {
        selectedIndex = index;
        if (index >= 0 && index < lineas.size()) {
            selectedLinea = lineas.get(index);
            txtNumero.setText(String.valueOf(selectedLinea.getPosicion()));
            txtNombre.setText(selectedLinea.getNombre());
            txtCategoria.setText(selectedLinea.getCategoria());
            chkActivo.setSelected(selectedLinea.isActivo());
            setColorFromHex(selectedLinea.getColor());
        }
        refreshList();
    }

    private void selectLineNoRefresh(int index) {
        selectedIndex = index;
        if (index >= 0 && index < lineas.size()) {
            selectedLinea = lineas.get(index);
            txtNumero.setText(String.valueOf(selectedLinea.getPosicion()));
            txtNombre.setText(selectedLinea.getNombre());
            txtCategoria.setText(selectedLinea.getCategoria());
            chkActivo.setSelected(selectedLinea.isActivo());
            setColorFromHex(selectedLinea.getColor());
        }
    }

    private void setColorFromHex(String hex) {
        try {
            selectedColor = Color.decode(hex);
        } catch (Exception e) {
            selectedColor = Color.GRAY;
        }
        btnColor.setBackground(selectedColor);
        updateColorLabel();
    }

    private void clearForm() {
        selectedIndex = -1;
        selectedLinea = null;
        txtNumero.setText("");
        txtNombre.setText("");
        txtCategoria.setText("");
        chkActivo.setSelected(true);
        selectedColor = Color.GRAY;
        btnColor.setBackground(selectedColor);
        updateColorLabel();
        refreshList();
    }

    private String colorToHex() {
        return String.format("#%02X%02X%02X",
            selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue());
    }

    private void saveLine() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LineaDAO dao = new LineaDAO();
            if (selectedLinea != null) {
                selectedLinea.setNombre(nombre);
                selectedLinea.setActivo(chkActivo.isSelected());
                selectedLinea.setColor(colorToHex());
                selectedLinea.setCategoria(txtCategoria.getText().trim());
                dao.updateDetails(selectedLinea);
            } else {
                int pos = lineas.size() + 1;
                LineaProduccion nueva = new LineaProduccion(
                    0, pos, nombre, chkActivo.isSelected(), colorToHex(),
                    txtCategoria.getText().trim());
                dao.insert(nueva);
            }
            loadFromDB();
            clearForm();
            if (onDataChanged != null) onDataChanged.run();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void deleteLine() {
        if (selectedLinea == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Eliminar la l\u00ednea " + selectedLinea.getPosicion() + " - " + selectedLinea.getNombre() + "?",
            "Confirmar eliminaci\u00f3n", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            LineaDAO dao = new LineaDAO();
            dao.delete(selectedLinea.getId());
            loadFromDB();
            clearForm();
            if (onDataChanged != null) onDataChanged.run();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void applyFilter() {
        String filter = filterField.getText().toLowerCase().trim();
        Component[] rows = listPanel.getComponents();
        for (int i = 0; i < rows.length && i < lineas.size(); i++) {
            LineaProduccion lp = lineas.get(i);
            boolean show = filter.isEmpty()
                || String.valueOf(lp.getPosicion()).contains(filter)
                || lp.getNombre().toLowerCase().contains(filter);
            rows[i].setVisible(show);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private Color parseColor(String hex) {
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return Color.GRAY;
        }
    }

    private Color lightTint(Color c) {
        float factor = 0.12f;
        int r = (int) (c.getRed() * (1 - factor) + 255 * factor);
        int g = (int) (c.getGreen() * (1 - factor) + 255 * factor);
        int b = (int) (c.getBlue() * (1 - factor) + 255 * factor);
        return new Color(r, g, b);
    }

    private class LineDropListener extends DropTargetAdapter {
        public void drop(DropTargetDropEvent dtde) {
            try {
                Transferable t = dtde.getTransferable();
                if (!t.isDataFlavorSupported(INDEX_FLAVOR)) {
                    dtde.rejectDrop();
                    return;
                }
                dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                int sourceIdx = (Integer) t.getTransferData(INDEX_FLAVOR);
                Point loc = dtde.getLocation();
                int targetIdx = getTargetIndex(loc.y);

                if (targetIdx >= 0 && targetIdx < lineas.size()
                        && sourceIdx != targetIdx) {
                    LineaProduccion moved = lineas.remove(sourceIdx);
                    if (targetIdx > sourceIdx) targetIdx--;
                    lineas.add(targetIdx, moved);
                    selectedIndex = targetIdx;
                    selectedLinea = lineas.get(targetIdx);
                    repositionLines();
                    refreshList();
                    saveLineNumbers();
                    refreshDetailForm();
                    if (onDataChanged != null) onDataChanged.run();
                }
                dtde.dropComplete(true);
            } catch (Exception e) {
                dtde.rejectDrop();
                e.printStackTrace();
            }
        }

        private int getTargetIndex(int y) {
            Component[] rows = listPanel.getComponents();
            int cumulative = 0;
            for (int i = 0; i < rows.length; i++) {
                if (rows[i].isVisible()) {
                    cumulative += rows[i].getHeight();
                    if (y < cumulative) return i;
                }
            }
            return rows.length - 1;
        }
    }

    private void refreshDetailForm() {
        if (selectedLinea != null) {
            txtNumero.setText(String.valueOf(selectedLinea.getPosicion()));
            txtNombre.setText(selectedLinea.getNombre());
            txtCategoria.setText(selectedLinea.getCategoria());
            chkActivo.setSelected(selectedLinea.isActivo());
            setColorFromHex(selectedLinea.getColor());
        }
    }

    private void repositionLines() {
        for (int i = 0; i < lineas.size(); i++) {
            lineas.get(i).setPosicion(i + 1);
        }
    }

    private void saveLineNumbers() {
        try {
            LineaDAO dao = new LineaDAO();
            dao.bulkUpdatePosiciones(lineas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
