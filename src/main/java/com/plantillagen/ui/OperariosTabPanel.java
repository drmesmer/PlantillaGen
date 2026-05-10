package com.plantillagen.ui;

import com.plantillagen.db.OperarioDAO;
import com.plantillagen.model.Operario;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OperariosTabPanel extends JPanel {

    private static final int POOL_PHOTO_SIZE = 24;

    private List<Operario> operarios;
    private Operario selectedOperario;

    private JPanel listPanel;
    private JTextField filterField;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JCheckBox chkActivo;
    private JLabel photoLabel;
    private JLabel countLabel;

    private RadarChartPanel radarChart;
    private JSlider sldEfi, sldCal, sldSeg, sldIni, sldPol;
    private JLabel lblEfi, lblCal, lblSeg, lblIni, lblPol;

    public OperariosTabPanel() {
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(6000);

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

    public void actionNuevo() {
        selectedOperario = null;
        txtCodigo.setText("");
        txtNombre.setText("");
        chkActivo.setSelected(true);
        photoLabel.setIcon(null);
        clearSliders();
    }

    public void actionGuardar() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        if (codigo.isEmpty() || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "C\u00f3digo y nombre son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            OperarioDAO dao = new OperarioDAO();
            if (selectedOperario != null) {
                selectedOperario.setCodigo(codigo);
                selectedOperario.setNombre(nombre);
                selectedOperario.setActivo(chkActivo.isSelected());
                selectedOperario.setEfi(sldEfi.getValue());
                selectedOperario.setCal(sldCal.getValue());
                selectedOperario.setSeg(sldSeg.getValue());
                selectedOperario.setIni(sldIni.getValue());
                selectedOperario.setPol(sldPol.getValue());
                dao.update(selectedOperario);
            } else {
                Operario nuevo = new Operario(codigo, nombre,
                    ImageUtil.createPlaceholder(codigo));
                nuevo.setEfi(sldEfi.getValue());
                nuevo.setCal(sldCal.getValue());
                nuevo.setSeg(sldSeg.getValue());
                nuevo.setIni(sldIni.getValue());
                nuevo.setPol(sldPol.getValue());
                dao.insert(nuevo);
            }
            int savedId = selectedOperario != null ? selectedOperario.getId() : -1;
            loadFromDB();
            if (savedId > 0) {
                selectOperarioById(savedId);
            } else {
                actionNuevo();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void actionEliminar() {
        if (selectedOperario == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "\u00bfEliminar DEFINITIVAMENTE al operario " + selectedOperario.getCodigo()
                + " - " + selectedOperario.getNombre() + "?\n\n"
                + "Esta acci\u00f3n NO se puede deshacer.",
            "Confirmar eliminaci\u00f3n", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            OperarioDAO dao = new OperarioDAO();
            dao.delete(selectedOperario.getId());
            loadFromDB();
            actionNuevo();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al eliminar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "OPERARIOS DISPONIBLES",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Color.DARK_GRAY),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        filterField = new JTextField();
        filterField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterField.putClientProperty("JTextField.placeholderText",
            "Filtrar por c\u00f3digo o nombre...");
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        filterPanel.add(filterField, BorderLayout.CENTER);
        panel.add(filterPanel, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setIgnoreRepaint(false);

        listPanel.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) { updateCount(); }
            @Override
            public void componentRemoved(ContainerEvent e) { updateCount(); }
        });

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);
        panel.add(scroll, BorderLayout.CENTER);

        countLabel = new JLabel("Cargando...", SwingConstants.CENTER);
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        countLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 2, 0));
        panel.add(countLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "DETALLE DE OPERARIO",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Color.DARK_GRAY),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        int photoSize = 130;
        photoLabel = new JLabel();
        photoLabel.setPreferredSize(new Dimension(photoSize + 16, photoSize + 16));
        photoLabel.setMinimumSize(new Dimension(photoSize + 8, photoSize + 8));
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        radarChart = new RadarChartPanel();

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 4, 6);
        gc.fill = GridBagConstraints.BOTH;

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0; gc.weighty = 0;
        gc.gridheight = 1;
        gc.insets = new Insets(0, 0, 0, 6);
        JPanel photoWrapper = new JPanel(new GridBagLayout());
        photoWrapper.setOpaque(false);
        GridBagConstraints pgc = new GridBagConstraints();
        pgc.insets = new Insets(6, 0, 0, 0);
        photoWrapper.add(photoLabel, pgc);
        grid.add(photoWrapper, gc);

        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1; gc.weighty = 0;
        gc.insets = new Insets(0, 0, 4, 0);
        grid.add(createBasicInfoBox(), gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; gc.weighty = 1;
        grid.add(radarChart, gc);

        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1; gc.weighty = 1;
        grid.add(createValoracionesBox(), gc);

        panel.add(grid, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createBasicInfoBox() {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Info b\u00e1sica", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11), Color.DARK_GRAY),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        box.setOpaque(false);

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        gbc.gridy = 0; gbc.weightx = 0;
        inner.add(makeLabel("C\u00f3digo:", null), gbc);
        gbc.gridy = 1; gbc.weightx = 1;
        txtCodigo = new JTextField(8); txtCodigo.setFont(fieldFont);
        inner.add(txtCodigo, gbc);

        gbc.gridy = 2; gbc.weightx = 0;
        inner.add(makeLabel("Nombre:", null), gbc);
        gbc.gridy = 3; gbc.weightx = 1;
        txtNombre = new JTextField(22); txtNombre.setFont(fieldFont);
        inner.add(txtNombre, gbc);

        gbc.gridy = 4;
        chkActivo = new JCheckBox("Operario activo", true);
        chkActivo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkActivo.setOpaque(false);
        inner.add(chkActivo, gbc);

        box.add(inner, BorderLayout.CENTER);
        return box;
    }

    private JPanel createValoracionesBox() {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Valoraciones", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11), Color.DARK_GRAY),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        box.setOpaque(false);

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setOpaque(false);

        String[] ids = {"EFI", "CAL", "SEG", "INI", "POL"};
        String[] fullNames = {
            "Eficiencia", "Calidad", "Seguridad y Rigor", "Iniciativa", "Polivalencia"
        };
        String[] tooltips = {
            "<html><b>1. Eficiencia (EFI)</b><br>Mide la productividad y velocidad. Es la capacidad<br>de mantener el ritmo de la l\u00ednea de producci\u00f3n,<br>cumplir con los tiempos de ciclo y alcanzar los<br>objetivos de unidades diarias sin retrasar al resto<br>del equipo.</html>",
            "<html><b>2. Calidad (CAL)</b><br>Representa la precisi\u00f3n y atenci\u00f3n al detalle.<br>Un valor alto aqu\u00ed significa que el operario tiene<br>un porcentaje de errores o \"scrap\" (desperdicio)<br>casi nulo y que sigue estrictamente las<br>especificaciones t\u00e9cnicas del producto.</html>",
            "<html><b>3. Seguridad y Rigor (SEG)</b><br>Eval\u00faa el cumplimiento de normas y protocolos.<br>Es la disciplina para usar los EPIs, respetar las<br>se\u00f1ales de seguridad y mantener el orden (5S) en<br>su puesto de trabajo. Es el pilar de la fiabilidad.</html>",
            "<html><b>4. Iniciativa (INI)</b><br>Sustituye al \"Liderazgo\". Mide la capacidad de<br>resoluci\u00f3n y proactividad. Un operario con alta<br>iniciativa detecta aver\u00edas antes de que ocurran,<br>propone mejoras en el proceso y no necesita<br>supervisi\u00f3n constante para actuar ante un<br>imprevisto.</html>",
            "<html><b>5. Polivalencia (POL)</b><br>Mide la adaptabilidad y trabajo en equipo. Es la<br>capacidad de rotar por diferentes puestos de la<br>f\u00e1brica, aprender r\u00e1pido a usar nuevas m\u00e1quinas<br>y colaborar con los compa\u00f1eros para que el flujo<br>de trabajo no se detenga.</html>"
        };

        sldEfi = createSlider(); sldCal = createSlider();
        sldSeg = createSlider(); sldIni = createSlider();
        sldPol = createSlider();
        JSlider[] sliders = {sldEfi, sldCal, sldSeg, sldIni, sldPol};

        lblEfi = createValueLabel(); lblCal = createValueLabel();
        lblSeg = createValueLabel(); lblIni = createValueLabel();
        lblPol = createValueLabel();
        JLabel[] valLabels = {lblEfi, lblCal, lblSeg, lblIni, lblPol};

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < 5; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel nameLbl = makeLabel(ids[i] + " \u2014 " + fullNames[i], tooltips[i]);
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            nameLbl.setForeground(new Color(60, 70, 85));
            nameLbl.setPreferredSize(new Dimension(180, 24));
            inner.add(nameLbl, gbc);

            gbc.gridx = 1; gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            inner.add(sliders[i], gbc);

            gbc.gridx = 2; gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            valLabels[i].setPreferredSize(new Dimension(30, 24));
            inner.add(valLabels[i], gbc);

            final int idx = i;
            sliders[i].addChangeListener((ChangeEvent e) -> {
                int val = sliders[idx].getValue();
                valLabels[idx].setText(String.valueOf(val));
                updateRadarChart();
                updatePhotoOverlay();
            });
        }

        box.add(inner, BorderLayout.CENTER);
        return box;
    }

    private JLabel makeLabel(String text, String tooltip) {
        JLabel lbl = new JLabel(text);
        if (tooltip != null) lbl.setToolTipText(tooltip);
        return lbl;
    }

    private JSlider createSlider() {
        JSlider slider = new JSlider(1, 99, 50);
        slider.setOpaque(false);
        slider.setPreferredSize(new Dimension(150, 24));
        return slider;
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("50", SwingConstants.RIGHT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(40, 50, 65));
        return label;
    }

    private void updateRadarChart() {
        if (radarChart != null) {
            radarChart.setValues(
                sldEfi.getValue(), sldCal.getValue(),
                sldSeg.getValue(), sldIni.getValue(), sldPol.getValue());
        }
    }

    private void updatePhotoOverlay() {
        if (selectedOperario == null) return;
        int avg = (sldEfi.getValue() + sldCal.getValue()
            + sldSeg.getValue() + sldIni.getValue() + sldPol.getValue()) / 5;
        updatePhotoWithAvg(selectedOperario, avg);
    }

    private void loadSliders(Operario op) {
        sldEfi.setValue(op.getEfi());   lblEfi.setText(String.valueOf(op.getEfi()));
        sldCal.setValue(op.getCal());   lblCal.setText(String.valueOf(op.getCal()));
        sldSeg.setValue(op.getSeg());   lblSeg.setText(String.valueOf(op.getSeg()));
        sldIni.setValue(op.getIni());   lblIni.setText(String.valueOf(op.getIni()));
        sldPol.setValue(op.getPol());   lblPol.setText(String.valueOf(op.getPol()));
        updateRadarChart();
    }

    private void clearSliders() {
        sldEfi.setValue(50); lblEfi.setText("50");
        sldCal.setValue(50); lblCal.setText("50");
        sldSeg.setValue(50); lblSeg.setText("50");
        sldIni.setValue(50); lblIni.setText("50");
        sldPol.setValue(50); lblPol.setText("50");
        updateRadarChart();
    }

    private void loadFromDB() {
        try {
            OperarioDAO dao = new OperarioDAO();
            operarios = dao.findAllIncludeInactive();
            listPanel.removeAll();

            for (Operario op : operarios) {
                loadPhoto(op);
                FichaOperarioRow ficha = new FichaOperarioRow(op, POOL_PHOTO_SIZE);
                ficha.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            selectOperarioByRef(op);
                        }
                    }
                });
                listPanel.add(ficha);
            }

            listPanel.revalidate();
            listPanel.repaint();
            updateCount();
        } catch (Exception e) {
            e.printStackTrace();
            operarios = new ArrayList<>();
        }
    }

    private void loadPhoto(Operario op) {
        Image foto = loadImage("fotos/" + op.getCodigo() + ".gif");
        if (foto == null) {
            foto = loadImage("fotos/" + op.getCodigo() + ".jpg");
        }
        if (foto == null) {
            foto = ImageUtil.createPlaceholder(op.getCodigo());
        }
        op.setFoto(foto);
    }

    private Image loadImage(String filename) {
        BufferedImage bi = null;
        try (InputStream is = getClass().getResourceAsStream("/" + filename)) {
            if (is != null) bi = ImageIO.read(is);
        } catch (IOException ignored) {}
        if (bi == null) {
            try {
                Path path = Paths.get("src/main/resources", filename);
                if (Files.exists(path)) bi = ImageIO.read(path.toFile());
            } catch (IOException ignored) {}
        }
        if (bi == null) return null;
        if (bi.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage argb = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = argb.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(bi, 0, 0, null);
            g2.dispose();
            return argb;
        }
        return bi;
    }

    private void updateCount() {
        int count = 0;
        for (Component c : listPanel.getComponents()) {
            if (c instanceof FichaOperarioRow) count++;
        }
        long active = operarios.stream().filter(Operario::isActivo).count();
        countLabel.setText(count + " disponibles (" + active + " activos)");
    }

    private void selectOperarioByRef(Operario op) {
        selectedOperario = op;
        txtCodigo.setText(op.getCodigo());
        txtNombre.setText(op.getNombre());
        chkActivo.setSelected(op.isActivo());
        updatePhoto(op);
        loadSliders(op);
    }

    public void selectOperarioById(int id) {
        for (Operario op : operarios) {
            if (op.getId() == id) {
                selectOperarioByRef(op);
                return;
            }
        }
    }

    private void updatePhoto(Operario op) {
        int avg = (op.getEfi() + op.getCal() + op.getSeg() + op.getIni() + op.getPol()) / 5;
        updatePhotoWithAvg(op, avg);
    }

    private void updatePhotoWithAvg(Operario op, int avg) {
        Image foto = op.getFoto();
        if (foto == null) foto = ImageUtil.createPlaceholder(op.getCodigo());

        int imgSize = 130;
        int shadowLen = 10;
        int arc = 20;
        int pad = shadowLen + 4;

        Color avgColor;
        if (avg > 80) avgColor = new Color(80, 220, 60);
        else if (avg > 60) avgColor = new Color(255, 160, 30);
        else avgColor = new Color(240, 70, 50);

        BufferedImage result = new BufferedImage(imgSize + pad, imgSize + pad, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double angle = Math.toRadians(30);
        int dx = (int) (shadowLen * Math.sin(angle));
        int dy = (int) (shadowLen * Math.cos(angle));

        for (int i = shadowLen; i >= 0; i--) {
            int alpha = 3 + (shadowLen - i) * 2;
            g2.setColor(new Color(0, 0, 0, Math.min(alpha, 22)));
            int sx = 4 + i * dx / shadowLen;
            int sy = 4 + i * dy / shadowLen;
            g2.fill(new java.awt.geom.RoundRectangle2D.Float(sx, sy, imgSize, imgSize, arc, arc));
        }

        g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, imgSize, imgSize, arc, arc));
        g2.drawImage(foto.getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH), 0, 0, null);
        g2.setClip(null);

        g2.setColor(avgColor);
        g2.setStroke(new java.awt.BasicStroke(3f));
        g2.draw(new java.awt.geom.RoundRectangle2D.Float(1.5f, 1.5f, imgSize - 3, imgSize - 3, arc - 2, arc - 2));

        Font avgFont = new Font("Segoe UI", Font.BOLD, 26);
        String avgStr = String.valueOf(avg);
        g2.setFont(avgFont);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(avgStr);
        int ax = imgSize - tw - 10;
        int ay = imgSize - 6;

        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(avgStr, ax + 1, ay + 1);
        g2.drawString(avgStr, ax + 1, ay - 1);
        g2.drawString(avgStr, ax - 1, ay + 1);
        g2.drawString(avgStr, ax - 1, ay - 1);

        g2.setColor(avgColor);
        g2.drawString(avgStr, ax, ay);

        g2.dispose();
        photoLabel.setIcon(new ImageIcon(result));
    }

    private void applyFilter() {
        String filter = filterField.getText().toLowerCase().trim();
        for (Component c : listPanel.getComponents()) {
            if (c instanceof FichaOperarioRow) {
                FichaOperarioRow row = (FichaOperarioRow) c;
                Operario op = row.getOperario();
                boolean visible = filter.isEmpty()
                    || op.getCodigo().toLowerCase().contains(filter)
                    || op.getNombre().toLowerCase().contains(filter);
                row.setVisible(visible);
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}
