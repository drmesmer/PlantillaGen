package com.plantillagen.ui;

import com.plantillagen.model.Operario;
import com.plantillagen.db.OperarioDAO;
import com.plantillagen.db.PlantillaDetalleTmpDAO;
import com.plantillagen.db.PlantillaHeaderDAO;
import com.plantillagen.db.LineaDAO;
import com.plantillagen.model.LineaProduccion;
import com.plantillagen.model.PlantillaEntry;
import com.plantillagen.model.PlantillaHeader;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int POOL_PHOTO_SIZE = 24;
    private static final int MIN_LINEA_PHOTO = 30;
    private static final int MAX_LINEA_PHOTO = 120;
    private static final int DEFAULT_LINEA_PHOTO = 65;

    private int currentLineaPhotoSize = DEFAULT_LINEA_PHOTO;

    private JLabel titleLabel;
    private JSplitPane mainSplitPane;
    private JPanel poolPanel;
    private JPanel poolRowsPanel;
    private JLabel poolCountLabel;
    private JPanel linesContainer;
    private JScrollPane linesScrollPane;
    private JLabel statusLabel;
    private JTabbedPane tabbedPane;
    private JSlider sizeSlider;
    private JLabel sliderValueLabel;
    private JTextField filterField;
    private JComboBox<String> turnoCombo;
    private JComboBox<String> categoriaCombo;
    private JComboBox<String> estadoCombo;

    private List<JPanel> lineaCardsPanels;
    private List<JPanel> lineaLeaderPanels;
    private List<JLabel> lineCountLabels;
    private List<Integer> panelTurnoIds;
    private List<String> panelCategorias;

    private Map<Integer, Integer> indexToDbId;
    private Map<Integer, Integer> dbIdToIndex;
    private PlantillaHeader currentPlantilla;
    private JLabel plantillaLabel;
    private LineasTabPanel lineasTabPanel;
    private CalendarioTabPanel calendarioTabPanel;
    private OperariosTabPanel operariosTabPanel;
    private JPanel statusCardPlantilla;
    private JPanel statusCardLineas;
    private JPanel statusCardOperarios;
    private JPanel statusPanel;
    private Component statusSpacer;
    private int lastStatusMargin = 310;
    private int currentTurno = 0;
    private String currentCategoria = "TODAS";
    private final Map<Integer, Operario> allOperarios = new HashMap<>();
    private final Runnable onStateChanged = () -> saveAllToPlantillaTmp();

    public MainFrame() {
        lineaCardsPanels = new ArrayList<>();
        lineaLeaderPanels = new ArrayList<>();
        lineCountLabels = new ArrayList<>();
        panelTurnoIds = new ArrayList<>();
        panelCategorias = new ArrayList<>();
        indexToDbId = new HashMap<>();
        dbIdToIndex = new HashMap<>();
        initComponents();
    }

    private void initComponents() {
        Font titleFont = new Font("Segoe UI", Font.BOLD, 18);

        setTitle("PlantillaGen - Gesti\u00f3n de Producci\u00f3n");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1366, 768);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(12, 10, 12, 10)
        ));
        northPanel.setBackground(Color.WHITE);

        titleLabel = new JLabel("GESTOR DE PLANTILLAS",
                SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        northPanel.add(titleLabel, BorderLayout.CENTER);

        Image logoImage = loadLogoImage();
        if (logoImage != null) {
            Image scaledLogo = logoImage.getScaledInstance(-1, 36, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new javax.swing.ImageIcon(scaledLogo));
            logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            northPanel.add(logoLabel, BorderLayout.EAST);
        }

        add(northPanel, BorderLayout.NORTH);

        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.22);
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainSplitPane.addHierarchyListener(e -> {
            if (mainSplitPane.isDisplayable()) {
                mainSplitPane.setDividerLocation(0.22);
            }
        });
        mainSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
            e -> updateStatusSpacer());

        initPoolPanel();

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "L\u00cdNEAS DE PRODUCCI\u00d3N",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Color.DARK_GRAY),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        initLinesContainer();

        JPanel turnoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        turnoPanel.setOpaque(false);
        turnoPanel.add(new JLabel("Turno:"));
        turnoCombo = new JComboBox<>(
            new String[]{"TODOS", "MA\u00d1ANA", "TARDE", "NOCHE"});
        turnoCombo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        turnoCombo.setSelectedIndex(0);
        turnoCombo.addActionListener(e -> {
            currentTurno = turnoCombo.getSelectedIndex();
            applyFilters();
        });
        turnoPanel.add(turnoCombo);

        turnoPanel.add(new JLabel("  Categor\u00eda:"));
        categoriaCombo = new JComboBox<>();
        categoriaCombo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        categoriaCombo.addItem("TODAS");
        categoriaCombo.setSelectedItem("TODAS");
        categoriaCombo.addActionListener(e -> {
            Object sel = categoriaCombo.getSelectedItem();
            currentCategoria = sel != null ? sel.toString() : "TODAS";
            applyFilters();
        });
        turnoPanel.add(categoriaCombo);

        turnoPanel.add(new JLabel("  Estado:"));
        estadoCombo = new JComboBox<>(new String[]{"BORRADOR", "ACTIVA"});
        estadoCombo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        estadoCombo.addActionListener(e -> {
            if (currentPlantilla != null) {
                currentPlantilla.setEstado((String) estadoCombo.getSelectedItem());
                plantillaLabel.setText(currentPlantilla.getNombre()
                    + " [" + currentPlantilla.getEstado() + "]");
            }
        });
        turnoPanel.add(estadoCombo);
        rightPanel.add(turnoPanel, BorderLayout.NORTH);

        linesScrollPane = new JScrollPane(linesContainer);
        linesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        linesScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        rightPanel.add(linesScrollPane, BorderLayout.CENTER);

        mainSplitPane.setLeftComponent(poolPanel);
        mainSplitPane.setRightComponent(rightPanel);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        UIManager.put("TabbedPane.tabAreaAlignment", "center");
        UIManager.put("TabbedPane.tabInsets",
            new java.awt.Insets(8, 20, 6, 20));
        tabbedPane.addTab("  PLANTILLA  ", mainSplitPane);
        lineasTabPanel = new LineasTabPanel();
        lineasTabPanel.setOnDataChanged(this::reloadLinePanels);
        tabbedPane.addTab("  L\u00cdNEAS  ", lineasTabPanel);
        operariosTabPanel = new OperariosTabPanel();
        tabbedPane.addTab("  OPERARIOS  ", operariosTabPanel);
        calendarioTabPanel = new CalendarioTabPanel();
        tabbedPane.addTab("  CALENDARIO  ", calendarioTabPanel);

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            CardLayout cl = (CardLayout) statusPanel.getLayout();
            if (idx == 0) cl.show(statusPanel, "PLANTILLA");
            else if (idx == 1) cl.show(statusPanel, "LINEAS");
            else if (idx == 2) cl.show(statusPanel, "OPERARIOS");
            else cl.show(statusPanel, "PLANTILLA");
            if (idx == 0) reloadLinePanels();
            updateStatusSpacer();
        });

        add(tabbedPane, BorderLayout.CENTER);

        statusPanel = new JPanel(new CardLayout());
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        statusLabel = new JLabel(
                "Arrastre las fichas hacia las l\u00edneas de producci\u00f3n, o entre l\u00edneas.");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        statusCardPlantilla = createPlantillaStatusBar();
        statusCardLineas = createLineasStatusBar();
        statusCardOperarios = createOperariosStatusBar();

        statusPanel.add(statusCardPlantilla, "PLANTILLA");
        statusPanel.add(statusCardLineas, "LINEAS");
        statusPanel.add(statusCardOperarios, "OPERARIOS");

        JPanel statusWrapper = new JPanel(new BorderLayout());
        statusWrapper.setOpaque(false);
        statusSpacer = Box.createHorizontalStrut(310);
        statusWrapper.add(statusSpacer, BorderLayout.WEST);
        statusWrapper.add(statusPanel, BorderLayout.CENTER);
        add(statusWrapper, BorderLayout.SOUTH);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateStatusSpacer();
            }
        });
    }

    private JPanel createPlantillaStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        west.setOpaque(false);

        JButton btnNueva = new JButton("Nueva");
        btnNueva.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnNueva.setFocusPainted(false);
        btnNueva.addActionListener(e -> nuevaPlantilla());

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> guardarPlantilla());

        JButton btnCargar = new JButton("Cargar");
        btnCargar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnCargar.setFocusPainted(false);
        btnCargar.addActionListener(e -> cargarPlantilla());

        plantillaLabel = new JLabel("");
        plantillaLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));

        west.add(btnNueva);
        west.add(btnGuardar);
        west.add(btnCargar);
        west.add(Box.createHorizontalStrut(6));
        west.add(plantillaLabel);
        west.add(Box.createHorizontalStrut(6));
        west.add(statusLabel);
        panel.add(west, BorderLayout.WEST);

        JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        zoomPanel.setOpaque(false);
        zoomPanel.add(new JLabel("Zoom:"));
        sizeSlider = new JSlider(JSlider.HORIZONTAL, MIN_LINEA_PHOTO,
            MAX_LINEA_PHOTO, DEFAULT_LINEA_PHOTO);
        sizeSlider.setPreferredSize(new Dimension(120, 20));
        sizeSlider.setOpaque(false);
        zoomPanel.add(sizeSlider);
        sliderValueLabel = new JLabel(DEFAULT_LINEA_PHOTO + "px");
        sliderValueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sliderValueLabel.setPreferredSize(new Dimension(40, 20));
        zoomPanel.add(sliderValueLabel);
        sizeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                currentLineaPhotoSize = sizeSlider.getValue();
                sliderValueLabel.setText(currentLineaPhotoSize + "px");
                updateAllLineaCardSizes(currentLineaPhotoSize);
            }
        });
        panel.add(zoomPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createLineasStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        west.setOpaque(false);

        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnNuevo.setFocusPainted(false);
        btnNuevo.addActionListener(e -> lineasTabPanel.actionNuevo());

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(e -> lineasTabPanel.actionEliminar());

        JButton btnGuardarLinea = new JButton("Guardar cambios");
        btnGuardarLinea.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGuardarLinea.setFocusPainted(false);
        btnGuardarLinea.addActionListener(e -> lineasTabPanel.actionGuardar());

        JLabel lineasStatusLabel = new JLabel("Gesti\u00f3n de l\u00edneas de producci\u00f3n");
        lineasStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        west.add(btnNuevo);
        west.add(btnEliminar);
        west.add(Box.createHorizontalStrut(4));
        west.add(btnGuardarLinea);
        west.add(Box.createHorizontalStrut(10));
        west.add(lineasStatusLabel);
        panel.add(west, BorderLayout.WEST);

        return panel;
    }

    private JPanel createOperariosStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        west.setOpaque(false);

        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnNuevo.setFocusPainted(false);
        btnNuevo.addActionListener(e -> operariosTabPanel.actionNuevo());

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(e -> operariosTabPanel.actionEliminar());

        JButton btnGuardar = new JButton("Guardar cambios");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> operariosTabPanel.actionGuardar());

        JLabel operariosStatusLabel = new JLabel("Gesti\u00f3n de operarios");
        operariosStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        west.add(btnNuevo);
        west.add(btnEliminar);
        west.add(Box.createHorizontalStrut(4));
        west.add(btnGuardar);
        west.add(Box.createHorizontalStrut(10));
        west.add(operariosStatusLabel);
        panel.add(west, BorderLayout.WEST);

        return panel;
    }

    private void updateStatusSpacer() {
        int w = tabbedPane.getWidth();
        if (w <= 0) w = getContentPane().getWidth();
        if (w <= 0) return;
        int margin = (int) (w * 0.22) + 14;
        if (statusSpacer != null) {
            statusSpacer.setPreferredSize(new Dimension(margin, 0));
            statusSpacer.setMinimumSize(new Dimension(margin, 0));
            statusSpacer.setMaximumSize(new Dimension(margin, 0));
            statusSpacer.revalidate();
        }
    }

    private JPanel createPlaceholderTab(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        label.setForeground(new Color(160, 160, 160));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void initPoolPanel() {
        poolPanel = new JPanel(new BorderLayout(0, 4));
        poolPanel.setBorder(BorderFactory.createCompoundBorder(
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
        filterPanel.add(filterField, BorderLayout.CENTER);
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        poolPanel.add(filterPanel, BorderLayout.NORTH);

        poolRowsPanel = new JPanel();
        poolRowsPanel.setLayout(new BoxLayout(poolRowsPanel, BoxLayout.Y_AXIS));
        poolRowsPanel.setBackground(Color.WHITE);
        poolRowsPanel.setIgnoreRepaint(false);
        poolRowsPanel.setTransferHandler(
            new ContainerDropHandler(poolRowsPanel, true));

        poolRowsPanel.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                updateAllCounts();
            }
            @Override
            public void componentRemoved(ContainerEvent e) {
                updateAllCounts();
            }
        });

        JScrollPane poolScroll = new JScrollPane(poolRowsPanel);
        poolScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        poolScroll.getVerticalScrollBar().setUnitIncrement(20);
        poolScroll.getViewport().setOpaque(true);
        poolScroll.getViewport().setBackground(Color.WHITE);
        poolScroll.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);
        poolPanel.add(poolScroll, BorderLayout.CENTER);

        poolCountLabel = new JLabel("Cargando...", SwingConstants.CENTER);
        poolCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        poolCountLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 2, 0));
        poolPanel.add(poolCountLabel, BorderLayout.SOUTH);

        poolPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateStatusSpacer();
            }
        });
    }

    private void initLinesContainer() {
        linesContainer = new JPanel();
        linesContainer.setLayout(new BoxLayout(linesContainer, BoxLayout.Y_AXIS));

        try {
            LineaDAO lineaDAO = new LineaDAO();
            List<LineaProduccion> lineas = lineaDAO.findAll();
            for (int i = 0; i < lineas.size(); i++) {
                LineaProduccion lp = lineas.get(i);
                createLineaTurnoPanels(lp, i, lineas.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            for (int i = 1; i <= 10; i++) {
                createLineaTurnoPanels(
                    new LineaProduccion(i, i, "Linea " + i, true, "#A0A0A0"),
                    i - 1, 10);
            }
        }
    }

    private void createLineaTurnoPanels(LineaProduccion lp, int lineIndex, int totalLines) {
        String[] turnosLabel = {"", "MA\u00d1ANA", "TARDE", "NOCHE"};
        String cat = lp.getCategoria() != null ? lp.getCategoria() : "";
        for (int t = 1; t <= 3; t++) {
            JPanel panel = createLineaPanel(lp, t, turnosLabel[t]);
            linesContainer.add(panel);
            int globalIdx = lineIndex * 3 + (t - 1);
            indexToDbId.put(globalIdx, lp.getId());
            panelCategorias.add(cat);
        }
        dbIdToIndex.put(lp.getId(), lineIndex * 3);
        if (lineIndex < totalLines - 1)
            linesContainer.add(Box.createVerticalStrut(6));
    }

    private JPanel createLineaPanel(LineaProduccion lp, int turno, String turnoLabel) {
        Color lineColor = parseColor(lp.getColor());

        JPanel outer = new JPanel(new BorderLayout(0, 2));
        outer.setBackground(lightTint(lineColor));
        String title = lp.getNombre().toUpperCase()
                + (lp.getCategoria().isEmpty() ? "" : " \u00b7 " + lp.getCategoria().toUpperCase());
        if (turno > 0) {
            title += " (" + turnoLabel + ")";
        }
        outer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(lineColor, 2),
            title,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12), Color.BLACK));

        int leaderW = 280;

        JPanel leaderPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 4, 4));
        leaderPanel.setOpaque(false);
        leaderPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(4, 4, 4, 4),
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lineColor.brighter(), 1),
                "L\u00cdDER", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11), Color.BLACK)));
        leaderPanel.setTransferHandler(
            new ContainerDropHandler(leaderPanel, false, 2, turno));
        leaderPanel.setPreferredSize(new Dimension(leaderW, 80));
        leaderPanel.setMaximumSize(new Dimension(leaderW, Integer.MAX_VALUE));
        leaderPanel.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                updateLeaderSize(leaderPanel, leaderW);
                updateAllCounts();
            }
            @Override
            public void componentRemoved(ContainerEvent e) {
                updateLeaderSize(leaderPanel, leaderW);
                updateAllCounts();
            }
        });
        lineaLeaderPanels.add(leaderPanel);

        JPanel cardsPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 4, 4));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(4, 4, 4, 4),
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lineColor.brighter(), 1),
                "OPERARIOS", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11), Color.BLACK)));
        lineaCardsPanels.add(cardsPanel);

        outer.setTransferHandler(
            new ContainerDropHandler(cardsPanel, false, -1, turno));
        cardsPanel.setTransferHandler(
            new ContainerDropHandler(cardsPanel, false, -1, turno));
        cardsPanel.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) { updateAllCounts(); }
            @Override
            public void componentRemoved(ContainerEvent e) { updateAllCounts(); }
        });

        outer.add(leaderPanel, BorderLayout.WEST);
        outer.add(cardsPanel, BorderLayout.CENTER);

        JLabel countLabel = new JLabel("0", SwingConstants.RIGHT);
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        countLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 6));
        lineCountLabels.add(countLabel);
        outer.add(countLabel, BorderLayout.SOUTH);

        panelTurnoIds.add(turno);

        return outer;
    }


    public void initData() {
        try {
            new PlantillaDetalleTmpDAO().deleteAll();
            OperarioDAO dao = new OperarioDAO();
            List<Operario> operarios = dao.findAll();
            allOperarios.clear();
            for (Operario op : operarios) {
                allOperarios.put(op.getId(), op);
                Image foto = loadImage("fotos/" + op.getCodigo() + ".gif");
                if (foto == null) {
                    foto = loadImage("fotos/" + op.getCodigo() + ".jpg");
                }
                if (foto == null) {
                    foto = ImageUtil.createPlaceholder(op.getCodigo());
                }
                op.setFoto(foto);
                FichaOperarioRow ficha = new FichaOperarioRow(op, POOL_PHOTO_SIZE);
                poolRowsPanel.add(ficha);
            }
            poolRowsPanel.revalidate();
            updateAllCounts();

            lineasTabPanel.initData();
            calendarioTabPanel.initData();
            operariosTabPanel.initData();

            statusLabel.setText(operarios.size() + " operarios cargados.");
            SwingUtilities.invokeLater(this::updateStatusSpacer);
        } catch (Exception e) {
            statusLabel.setText("Error al conectar con la base de datos.");
            e.printStackTrace();
        }
    }

    private void reloadLinePanels() {
        try {
            LineaDAO dao = new LineaDAO();
            List<LineaProduccion> activeLines = dao.findAll();

            Map<String, List<SavedAssignment>> saved = new HashMap<>();
            for (int i = 0; i < lineaCardsPanels.size(); i++) {
                Integer dbId = indexToDbId.get(i);
                int turno = panelTurnoIds.get(i);
                if (dbId == null) continue;
                String key = dbId + "_" + turno;
                List<SavedAssignment> list = new ArrayList<>();

                JPanel leader = lineaLeaderPanels.get(i);
                for (Component c : leader.getComponents()) {
                    if (c instanceof FichaOperario && c.isVisible()) {
                        FichaOperario f = (FichaOperario) c;
                        list.add(new SavedAssignment(f.getOperario(), true, f.isFormacion()));
                    }
                }
                JPanel cards = lineaCardsPanels.get(i);
                for (Component c : cards.getComponents()) {
                    if (c instanceof FichaOperario && c.isVisible()) {
                        FichaOperario f = (FichaOperario) c;
                        list.add(new SavedAssignment(f.getOperario(), false, f.isFormacion()));
                    }
                }
                saved.put(key, list);
            }

            clearAllLines();
            lineaCardsPanels.clear();
            lineaLeaderPanels.clear();
            lineCountLabels.clear();
            panelTurnoIds.clear();
            panelCategorias.clear();
            indexToDbId.clear();
            dbIdToIndex.clear();
            linesContainer.removeAll();

            for (int i = 0; i < activeLines.size(); i++) {
                createLineaTurnoPanels(activeLines.get(i), i, activeLines.size());
            }

            for (int i = 0; i < activeLines.size(); i++) {
                int dbId = activeLines.get(i).getId();
                for (int t = 1; t <= 3; t++) {
                    String key = dbId + "_" + t;
                    List<SavedAssignment> ops = saved.get(key);
                    if (ops != null) {
                        for (SavedAssignment sa : ops) {
                            Operario op = sa.operario;
                            removeFromPool(op);
                            FichaOperario ficha = new FichaOperario(op, currentLineaPhotoSize);
                            if (sa.lider) ficha.setLider(true);
                            ficha.setOnStateChanged(onStateChanged);
                            ficha.setOnDoubleClick(() -> openOperarioInTab(op.getId()));
                            int panelIdx = dbIdToIndex.get(dbId) + (t - 1);
                            if (sa.lider) {
                                lineaLeaderPanels.get(panelIdx).add(ficha);
                            } else {
                                lineaCardsPanels.get(panelIdx).add(ficha);
                            }
                        }
                    }
                }
            }

            refreshCategoriaCombo();
            applyFilters();

            linesContainer.revalidate();
            linesContainer.repaint();
            poolRowsPanel.revalidate();
            poolRowsPanel.repaint();
            for (JPanel panel : lineaCardsPanels) { panel.revalidate(); panel.repaint(); }
            for (JPanel panel : lineaLeaderPanels) { panel.revalidate(); panel.repaint(); }
            updateAllCounts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SavedAssignment {
        final Operario operario;
        final boolean lider;
        final boolean formacion;
        SavedAssignment(Operario o, boolean l, boolean f) {
            operario = o; lider = l; formacion = f;
        }
    }

    private Color lightTint(Color c) {
        float factor = 0.12f;
        int r = (int) (c.getRed() * (1 - factor) + 255 * factor);
        int g = (int) (c.getGreen() * (1 - factor) + 255 * factor);
        int b = (int) (c.getBlue() * (1 - factor) + 255 * factor);
        return new Color(r, g, b);
    }

    private void clearAllLines() {
        for (JPanel panel : lineaCardsPanels) {
            for (Component c : panel.getComponents()) {
                if (c instanceof FichaOperario && c.isVisible()) {
                    Operario op = ((FichaOperario) c).getOperario();
                    FichaOperarioRow ficha = new FichaOperarioRow(op, POOL_PHOTO_SIZE);
                    addSortedToPool(ficha);
                }
            }
            panel.removeAll();
        }
        for (JPanel panel : lineaLeaderPanels) {
            for (Component c : panel.getComponents()) {
                if (c instanceof FichaOperario && c.isVisible()) {
                    Operario op = ((FichaOperario) c).getOperario();
                    FichaOperarioRow ficha = new FichaOperarioRow(op, POOL_PHOTO_SIZE);
                    addSortedToPool(ficha);
                }
            }
            panel.removeAll();
        }
        poolRowsPanel.revalidate();
        poolRowsPanel.repaint();
        updateAllCounts();
    }

    private void applyFilters() {
        for (int i = 0; i < lineaCardsPanels.size(); i++) {
            int turno = panelTurnoIds.get(i);
            String cat = panelCategorias.get(i);
            boolean visible = (currentTurno == 0 || turno == currentTurno)
                && ("TODAS".equals(currentCategoria) || currentCategoria.equals(cat));
            Component outer = lineaCardsPanels.get(i).getParent();
            if (outer != null) {
                outer.setVisible(visible);
                if (outer instanceof JComponent) {
                    ((JComponent) outer).invalidate();
                }
            }
        }
        linesContainer.invalidate();
        linesContainer.validate();
        linesContainer.repaint();
        if (linesScrollPane != null) {
            linesScrollPane.getViewport().invalidate();
            linesScrollPane.getViewport().validate();
            linesScrollPane.getViewport().repaint();
        }
    }

    private void refreshCategoriaCombo() {
        String prev = (String) categoriaCombo.getSelectedItem();
        categoriaCombo.removeAllItems();
        categoriaCombo.addItem("TODAS");
        try {
            LineaDAO dao = new LineaDAO();
            for (LineaProduccion lp : dao.findAll()) {
                String cat = lp.getCategoria();
                if (cat != null && !cat.isEmpty() && !containsItem(categoriaCombo, cat)) {
                    categoriaCombo.addItem(cat);
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        if (prev != null && containsItem(categoriaCombo, prev)) {
            categoriaCombo.setSelectedItem(prev);
        } else {
            categoriaCombo.setSelectedItem("TODAS");
            currentCategoria = "TODAS";
        }
    }

    private boolean containsItem(JComboBox<String> combo, String item) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (item.equals(combo.getItemAt(i))) return true;
        }
        return false;
    }

    private void addSortedToPool(FichaOperarioRow ficha) {
        String code = ficha.getOperario().getCodigo();
        Component[] components = poolRowsPanel.getComponents();
        int insertAt = components.length;
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof FichaOperarioRow) {
                if (code.compareTo(
                        ((FichaOperarioRow) components[i]).getOperario().getCodigo()) < 0) {
                    insertAt = i;
                    break;
                }
            }
        }
        poolRowsPanel.add(ficha, insertAt);
    }

    private void nuevaPlantilla() {
        try {
            String nombre = generatePlantillaName();
            JTextField nameField = new JTextField(nombre, 30);
            nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.add(new JLabel("Nombre de la nueva plantilla:"), BorderLayout.NORTH);
            panel.add(nameField, BorderLayout.CENTER);

            int result = JOptionPane.showConfirmDialog(this, panel,
                "Nueva plantilla", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return;

            nombre = nameField.getText().trim();
            if (nombre.isEmpty()) return;

            clearAllLines();
            PlantillaHeaderDAO dao = new PlantillaHeaderDAO();
            PlantillaHeader h = new PlantillaHeader(nombre,
                (String) estadoCombo.getSelectedItem());
            int id = dao.save(h);
            h.setId(id);
            currentPlantilla = h;
            new PlantillaDetalleTmpDAO().deleteAll();
            estadoCombo.setSelectedItem(h.getEstado());
            plantillaLabel.setText(h.getNombre() + " [" + h.getEstado() + "]");
            statusLabel.setText("Nueva plantilla creada: " + h.getNombre());
        } catch (Exception e) {
            statusLabel.setText("Error al crear plantilla.");
            e.printStackTrace();
        }
    }

    private void guardarPlantilla() {
        if (currentPlantilla == null) {
            statusLabel.setText("Primero crea una plantilla con Nueva.");
            return;
        }
        String[] opts = {"Actualizar", "Nueva", "Cancelar"};
        int option = JOptionPane.showOptionDialog(this,
            "¿Actualizar \"" + currentPlantilla.getNombre() + "\" o guardar como nueva?",
            "Guardar plantilla", JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
        if (option == 2 || option == JOptionPane.CLOSED_OPTION) return;
        if (option == 1) {
            copyPlantillaToNew();
            return;
        }
        saveAllToPlantillaTmp();
        try {
            new PlantillaDetalleTmpDAO().copyToPlantillaDetalle(currentPlantilla.getId());
            new PlantillaHeaderDAO().update(currentPlantilla.getId(),
                currentPlantilla.getNombre(), currentPlantilla.getEstado());
        } catch (Exception ex) { ex.printStackTrace(); }
        plantillaLabel.setText(currentPlantilla.getNombre() + " [" + currentPlantilla.getEstado() + "]");
        statusLabel.setText("Plantilla guardada: " + currentPlantilla.getNombre());
    }

    private void copyPlantillaToNew() {
        try {
            String nombre = generatePlantillaName();
            PlantillaHeaderDAO headerDAO = new PlantillaHeaderDAO();
            PlantillaHeader nuevo = new PlantillaHeader(nombre, "BORRADOR");
            int newId = headerDAO.save(nuevo);
            nuevo.setId(newId);

            PlantillaDetalleTmpDAO detDAO = new PlantillaDetalleTmpDAO();
            for (int t = 1; t <= 3; t++) {
                List<PlantillaEntry> entries =
                    detDAO.findByPlantillaIdAndTurno(currentPlantilla.getId(), t);
                for (PlantillaEntry e : entries) {
                    e.setTurnoId(t);
                    detDAO.save(newId, e);
                }
            }
            detDAO.copyToPlantillaDetalle(newId);

            currentPlantilla = nuevo;
            statusLabel.setText("Plantilla copiada como: " + nombre);
        } catch (Exception e) {
            statusLabel.setText("Error al copiar plantilla.");
            e.printStackTrace();
        }
    }

    private String generatePlantillaName() throws Exception {
        LocalDate today = LocalDate.now();
        String datePrefix = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        int week = today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        String prefix = datePrefix + "_Semana" + week + "_v";

        PlantillaHeaderDAO dao = new PlantillaHeaderDAO();
        List<PlantillaHeader> all = dao.findAll();
        int maxVersion = 0;
        for (PlantillaHeader h : all) {
            if (h.getNombre().startsWith(prefix)) {
                String suffix = h.getNombre().substring(prefix.length());
                try {
                    int v = Integer.parseInt(suffix);
                    if (v > maxVersion) maxVersion = v;
                } catch (NumberFormatException ignored) {}
            }
        }
        return prefix + (maxVersion + 1);
    }

    private void cargarPlantilla() {
        try {
            PlantillaHeaderDAO dao = new PlantillaHeaderDAO();
            List<PlantillaHeader> plantillas = dao.findAll();
            if (plantillas.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No hay plantillas guardadas.", "Cargar",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] cols = {"Nombre", "Estado", "Creada"};
            Object[][] data = new Object[plantillas.size()][3];
            java.time.format.DateTimeFormatter dtf =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (int i = 0; i < plantillas.size(); i++) {
                PlantillaHeader p = plantillas.get(i);
                data[i][0] = p.getNombre();
                data[i][1] = p.getEstado();
                data[i][2] = p.getCreatedAt() != null
                    ? p.getCreatedAt().toLocalDateTime().format(dtf) : "";
            }
            JTable table = new JTable(data, cols) {
                public java.awt.Component prepareRenderer(
                        javax.swing.table.TableCellRenderer r, int row, int col) {
                    java.awt.Component c = super.prepareRenderer(r, row, col);
                    if (!isRowSelected(row)) {
                        c.setBackground(row % 2 == 0
                            ? new Color(245, 247, 250) : Color.WHITE);
                    }
                    if (c instanceof javax.swing.JComponent) {
                        ((javax.swing.JComponent) c).setOpaque(true);
                    }
                    return c;
                }
            };
            table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            table.setRowHeight(26);
            table.getColumnModel().getColumn(0).setPreferredWidth(220);
            table.getColumnModel().getColumn(1).setPreferredWidth(70);
            table.getColumnModel().getColumn(2).setPreferredWidth(130);
            table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

            JScrollPane sp = new JScrollPane(table);
            sp.setPreferredSize(new Dimension(520, 340));

            Object[] buttons = {"Cargar", "Cancelar"};
            int result = JOptionPane.showOptionDialog(this, sp,
                "Cargar plantilla", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, buttons, buttons[0]);

            if (result != 0) return;
            int sel = table.getSelectedRow();
            if (sel < 0) return;

            PlantillaHeader selected = plantillas.get(sel);
            currentPlantilla = selected;
            estadoCombo.setSelectedItem(selected.getEstado());
            try {
                PlantillaDetalleTmpDAO tmpDAO = new PlantillaDetalleTmpDAO();
                tmpDAO.copyFromPlantillaDetalle(selected.getId());
            } catch (Exception ex) { ex.printStackTrace(); }
            rebuildPoolForTurno();
            plantillaLabel.setText(selected.getNombre() + " [" + selected.getEstado() + "]");
            statusLabel.setText("Plantilla cargada: " + selected.getNombre());
        } catch (Exception e) {
            statusLabel.setText("Error al cargar plantillas.");
            e.printStackTrace();
        }
    }

    private void rebuildPoolForTurno() {
        clearAllLines();
        poolRowsPanel.removeAll();

        java.util.Set<Integer> assignedIds = new java.util.HashSet<>();
        if (currentPlantilla != null) {
            try {
                PlantillaDetalleTmpDAO dao = new PlantillaDetalleTmpDAO();
                for (int t = 1; t <= 3; t++) {
                    List<PlantillaEntry> entries =
                        dao.findByPlantillaIdAndTurno(currentPlantilla.getId(), t);
                    for (PlantillaEntry entry : entries) {
                        Integer baseIdx = dbIdToIndex.get(entry.getLineaId());
                        if (baseIdx == null) continue;
                        int panelIdx = baseIdx + (t - 1);
                        if (panelIdx >= lineaCardsPanels.size()) continue;

                        Operario op = allOperarios.get(entry.getOperarioId());
                        if (op == null) continue;

                        FichaOperario ficha = new FichaOperario(op, currentLineaPhotoSize);
                        if (entry.isEsLider()) ficha.setLider(true);
                        ficha.setOnStateChanged(onStateChanged);
                        ficha.setOnDoubleClick(() -> openOperarioInTab(op.getId()));

                        if (entry.isEsLider()) {
                            lineaLeaderPanels.get(panelIdx).add(ficha);
                        } else {
                            lineaCardsPanels.get(panelIdx).add(ficha);
                        }
                        assignedIds.add(op.getId());
                    }
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        for (Operario op : allOperarios.values()) {
            if (assignedIds.contains(op.getId())) continue;
            if (op.getFoto() == null) {
                op.setFoto(ImageUtil.createPlaceholder(op.getCodigo()));
            }
            poolRowsPanel.add(new FichaOperarioRow(op, POOL_PHOTO_SIZE));
        }

        applyFilters();

        poolRowsPanel.revalidate();
        poolRowsPanel.repaint();
        for (JPanel panel : lineaCardsPanels) { panel.revalidate(); panel.repaint(); }
        for (JPanel panel : lineaLeaderPanels) { panel.revalidate(); panel.repaint(); }
        linesContainer.revalidate();
        linesContainer.repaint();
        updateAllCounts();
    }

    private void removeFromPool(Operario op) {
        for (Component c : poolRowsPanel.getComponents()) {
            if (c instanceof FichaOperarioRow
                    && ((FichaOperarioRow) c).getOperario().equals(op)) {
                c.setVisible(false);
                poolRowsPanel.remove(c);
                return;
            }
        }
    }

    private Operario findOperarioInPool(int operarioId) {
        for (Component c : poolRowsPanel.getComponents()) {
            if (c instanceof FichaOperarioRow) {
                Operario op = ((FichaOperarioRow) c).getOperario();
                if (op.getId() == operarioId) {
                    c.setVisible(false);
                    poolRowsPanel.remove(c);
                    return op;
                }
            }
        }
        return null;
    }

    private void saveAllToPlantillaTmp() {
        if (currentPlantilla == null) {
            try {
                String nombre = "Sesion_" + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss"));
                PlantillaHeaderDAO dao = new PlantillaHeaderDAO();
                PlantillaHeader h = new PlantillaHeader(nombre, "BORRADOR");
                int id = dao.save(h);
                h.setId(id);
                currentPlantilla = h;
                plantillaLabel.setText("(nueva sesión)");
            } catch (Exception e) {
                statusLabel.setText("Error al crear plantilla: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        try {
            PlantillaDetalleTmpDAO dao = new PlantillaDetalleTmpDAO();
            dao.deleteByPlantillaId(currentPlantilla.getId());
            int savedCount = 0;
            for (int i = 0; i < lineaCardsPanels.size(); i++) {
                Integer lineaId = indexToDbId.get(i);
                int turno = panelTurnoIds.get(i);
                if (lineaId == null) continue;
                int order = 0;
                JPanel cardsPanel = lineaCardsPanels.get(i);
                for (Component c : cardsPanel.getComponents()) {
                    if (c instanceof FichaOperario) {
                        FichaOperario ficha = (FichaOperario) c;
                        PlantillaEntry e = new PlantillaEntry(
                            lineaId, ficha.getOperario().getId(),
                            false, ficha.isFormacion(), order++);
                        e.setTurnoId(turno);
                        dao.save(currentPlantilla.getId(), e);
                        savedCount++;
                    }
                }
                JPanel leaderPanel = lineaLeaderPanels.get(i);
                for (Component c : leaderPanel.getComponents()) {
                    if (c instanceof FichaOperario) {
                        FichaOperario ficha = (FichaOperario) c;
                        PlantillaEntry e = new PlantillaEntry(
                            lineaId, ficha.getOperario().getId(),
                            true, ficha.isFormacion(), order++);
                        e.setTurnoId(turno);
                        dao.save(currentPlantilla.getId(), e);
                        savedCount++;
                    }
                }
            }
            statusLabel.setText(savedCount + " asignaciones en tmp");
        } catch (Exception e) {
            statusLabel.setText("Error BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openOperarioInTab(int operarioId) {
        tabbedPane.setSelectedIndex(2);
        operariosTabPanel.selectOperarioById(operarioId);
    }

    private List<String> discoverImageResources() {
        List<String> names = new ArrayList<>();
        try {
            URL url = getClass().getResource("/");
            if (url != null && "file".equals(url.getProtocol())) {
                try (DirectoryStream<Path> stream =
                        Files.newDirectoryStream(Paths.get(url.toURI()))) {
                    for (Path p : stream) {
                        String name = p.getFileName().toString().toLowerCase();
                        if (name.endsWith(".gif") || name.endsWith(".jpg")
                                || name.endsWith(".png") || name.endsWith(".jpeg")) {
                            names.add(p.getFileName().toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            try (DirectoryStream<Path> stream =
                    Files.newDirectoryStream(Paths.get("src/main/resources"))) {
                for (Path p : stream) {
                    String name = p.getFileName().toString().toLowerCase();
                    if (name.endsWith(".gif") || name.endsWith(".jpg")
                            || name.endsWith(".png") || name.endsWith(".jpeg")) {
                        names.add(p.getFileName().toString());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return names;
    }

    private Image loadImage(String filename) {
        BufferedImage bi = null;
        try (InputStream is = getClass().getResourceAsStream("/" + filename)) {
            if (is != null) {
                bi = ImageIO.read(is);
            }
        } catch (IOException ignored) {
        }
        if (bi == null) {
            try {
                Path path = Paths.get("src/main/resources", filename);
                if (Files.exists(path)) {
                    bi = ImageIO.read(path.toFile());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (bi == null) return null;

        if (bi.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage argb = new BufferedImage(
                bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = argb.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(bi, 0, 0, null);
            g2.dispose();
            return argb;
        }
        return bi;
    }

    private Color parseColor(String hex) {
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return Color.GRAY;
        }
    }

    private Image loadLogoImage() {
        try (InputStream is = getClass().getResourceAsStream("/gfx/logo.png")) {
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (IOException ignored) {
        }
        try {
            Path path = Paths.get("src/main/resources/gfx/logo.png");
            if (Files.exists(path)) {
                return ImageIO.read(path.toFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateAllCounts() {
        int poolCount = 0;
        for (Component c : poolRowsPanel.getComponents()) {
            if (c instanceof FichaOperarioRow) poolCount++;
        }
        poolCountLabel.setText(poolCount + " disponibles");

        for (int i = 0; i < lineaCardsPanels.size(); i++) {
            JPanel cardsPanel = lineaCardsPanels.get(i);
            int count = 0;
            for (Component c : cardsPanel.getComponents()) {
                if (c instanceof FichaOperario) count++;
            }
            if (i < lineCountLabels.size()) {
                lineCountLabels.get(i).setText(count + " operarios");
            }
        }
    }

    private void updateAllLineaCardSizes(int newSize) {
        for (JPanel cardsPanel : lineaCardsPanels) {
            updatePanelCardSizes(cardsPanel, newSize);
        }
        for (JPanel leaderPanel : lineaLeaderPanels) {
            updatePanelCardSizes(leaderPanel, newSize);
            updateLeaderSize(leaderPanel, 280);
        }
    }

    private void updatePanelCardSizes(JPanel panel, int newSize) {
        for (Component c : panel.getComponents()) {
            if (c instanceof FichaOperario) {
                ((FichaOperario) c).setPhotoSize(newSize);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private void updateLeaderSize(JPanel panel, int fixedWidth) {
        int h = panel.getLayout().preferredLayoutSize(panel).height;
        panel.setPreferredSize(new Dimension(fixedWidth, Math.max(h, 56)));
        panel.revalidate();
    }

    private void applyFilter() {
        String filter = filterField.getText().toLowerCase().trim();
        for (Component c : poolRowsPanel.getComponents()) {
            if (c instanceof FichaOperarioRow) {
                FichaOperarioRow row = (FichaOperarioRow) c;
                Operario op = row.getOperario();
                boolean visible = filter.isEmpty()
                    || op.getCodigo().toLowerCase().contains(filter)
                    || op.getNombre().toLowerCase().contains(filter);
                row.setVisible(visible);
            }
        }
        poolRowsPanel.revalidate();
        poolRowsPanel.repaint();
    }

    private class ContainerDropHandler extends TransferHandler {

        private static final long serialVersionUID = 1L;

        private final JPanel targetPanel;
        private final boolean poolMode;
        private final int maxCount;
        private final int turnoId;
        private final DataFlavor operarioFlavor;

        ContainerDropHandler(JPanel targetPanel, boolean poolMode) {
            this(targetPanel, poolMode, -1, 0);
        }

        ContainerDropHandler(JPanel targetPanel, boolean poolMode, int maxCount) {
            this(targetPanel, poolMode, maxCount, 0);
        }

        ContainerDropHandler(JPanel targetPanel, boolean poolMode, int maxCount, int turnoId) {
            this.targetPanel = targetPanel;
            this.poolMode = poolMode;
            this.maxCount = maxCount;
            this.turnoId = turnoId;
            this.operarioFlavor = OperarioTransferHandler.OPERARIO_FLAVOR;
        }

        private int getPhotoSize() {
            return poolMode ? POOL_PHOTO_SIZE : currentLineaPhotoSize;
        }

        private void addSortedToPool(FichaOperarioRow ficha) {
            String code = ficha.getOperario().getCodigo();
            Component[] components = poolRowsPanel.getComponents();
            int insertAt = components.length;
            for (int i = 0; i < components.length; i++) {
                if (components[i] instanceof FichaOperarioRow) {
                    if (code.compareTo(
                            ((FichaOperarioRow) components[i]).getOperario()
                                .getCodigo()) < 0) {
                        insertAt = i;
                        break;
                    }
                }
            }
            poolRowsPanel.add(ficha, insertAt);
        }

        private void removeOperarioFromAllPanels(Operario op, JPanel excludePanel) {
            if (poolRowsPanel != excludePanel) {
                for (Component c : poolRowsPanel.getComponents()) {
                    if (c instanceof FichaOperarioRow
                            && ((FichaOperarioRow) c).getOperario().equals(op)) {
                        c.setVisible(false);
                        poolRowsPanel.remove(c);
                        poolRowsPanel.revalidate();
                        poolRowsPanel.repaint();
                        return;
                    }
                }
            }
            for (JPanel cardsPanel : lineaCardsPanels) {
                if (cardsPanel == excludePanel) continue;
                for (Component c : cardsPanel.getComponents()) {
                    if (c instanceof FichaOperario
                            && ((FichaOperario) c).getOperario().equals(op)) {
                        c.setVisible(false);
                        cardsPanel.remove(c);
                        cardsPanel.revalidate();
                        cardsPanel.repaint();
                        return;
                    }
                }
            }
            for (JPanel leaderPanel : lineaLeaderPanels) {
                if (leaderPanel == excludePanel) continue;
                for (Component c : leaderPanel.getComponents()) {
                    if (c instanceof FichaOperario
                            && ((FichaOperario) c).getOperario().equals(op)) {
                        c.setVisible(false);
                        leaderPanel.remove(c);
                        leaderPanel.revalidate();
                        leaderPanel.repaint();
                        return;
                    }
                }
            }
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(operarioFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;

            try {
                Transferable t = support.getTransferable();
                Operario op = (Operario) t.getTransferData(operarioFlavor);

                for (Component c : targetPanel.getComponents()) {
                    if (c instanceof FichaOperario
                            && ((FichaOperario) c).getOperario().equals(op)) {
                        targetPanel.remove(c);
                        targetPanel.add(c);
                        targetPanel.revalidate();
                        targetPanel.repaint();
                        return true;
                    }
                    if (c instanceof FichaOperarioRow
                            && ((FichaOperarioRow) c).getOperario().equals(op)) {
                        return false;
                    }
                }

                if (maxCount > 0 && targetPanel.getComponentCount() >= maxCount) {
                    return false;
                }

                if (poolMode) {
                    FichaOperarioRow ficha = new FichaOperarioRow(op, getPhotoSize());
                    ficha.setOnStateChanged(onStateChanged);
                    addSortedToPool(ficha);
                } else {
                    FichaOperario ficha = new FichaOperario(op, getPhotoSize());
                    if (maxCount == 2) ficha.setLider(true);
                    ficha.setOnStateChanged(onStateChanged);
                    ficha.setOnDoubleClick(() -> openOperarioInTab(op.getId()));
                    targetPanel.add(ficha);
                }
                removeOperarioFromAllPanels(op, targetPanel);
                targetPanel.revalidate();
                targetPanel.repaint();
                saveAllToPlantillaTmp();
                return true;
            } catch (UnsupportedFlavorException | IOException e) {
                return false;
            }
        }
    }
}
