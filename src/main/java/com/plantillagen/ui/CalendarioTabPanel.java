package com.plantillagen.ui;

import com.plantillagen.db.CalendarioDAO;
import com.plantillagen.db.PlantillaHeaderDAO;
import com.plantillagen.model.CalendarioEntry;
import com.plantillagen.model.PlantillaHeader;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CalendarioTabPanel extends JPanel {

    private List<PlantillaHeader> plantillas;
    private int selectedYear;
    private JPanel listPanel;
    private JTextField filterField;
    private JPanel monthsContainer;
    private JLabel yearLabel;
    private int selectedPlantillaIndex = -1;
    private PlantillaHeader selectedPlantilla;
    private Runnable onPlantillaDoubleClick;

    public void setOnPlantillaDoubleClick(Runnable callback) {
        this.onPlantillaDoubleClick = callback;
    }

    public PlantillaHeader getSelectedPlantilla() {
        return selectedPlantilla;
    }

    private Map<LocalDate, String> calendarioData = new HashMap<>();
    private Map<LocalDate, Color> colorData = new HashMap<>();
    private List<CalendarioEntry> calendarioEntries;

    private LocalDate dragStart = null;
    private LocalDate dragEnd = null;
    private final Map<LocalDate, JLabel> dayLabelMap = new HashMap<>();
    private final Set<JLabel> previewLabels = new HashSet<>();

    private static final Color ROW_EVEN = new Color(255, 255, 255);
    private static final Color ROW_ODD = new Color(247, 249, 252);
    private static final Color ROW_SELECTED = new Color(180, 210, 255);

    private static final Color TODAY_BG = new Color(255, 243, 205);
    private static final Color WEEKEND_BG = new Color(248, 248, 248);
    private static final Color SUNDAY_FG = new Color(200, 50, 50);
    private static final Color HEADER_BG = new Color(240, 242, 245);
    private static final Color WEEK_COLOR = new Color(150, 155, 165);
    private static final Color MONTH_TITLE_BG = new Color(70, 90, 120);
    private static final Color SEPARATOR = new Color(228, 231, 237);
    private static final Color DRAG_PREVIEW = new Color(160, 190, 255);

    private static final String[] DAY_NAMES = {"L", "M", "X", "J", "V", "S", "D"};
    private static final String[] MONTH_NAMES = {
        "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO",
        "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"
    };

    public CalendarioTabPanel() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        selectedYear = LocalDate.now().getYear();

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
        loadPlantillas();
        loadCalendarData();
        buildCalendar();
    }

    public void refreshPlantillas() {
        loadPlantillas();
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "PLANTILLAS",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Color.DARK_GRAY),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        filterField = new JTextField();
        filterField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterField.putClientProperty("JTextField.placeholderText", "Filtrar...");
        filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        panel.add(filterField, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

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
                BorderFactory.createEtchedBorder(), "CALENDARIO DE PRODUCCI\u00d3N",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Color.DARK_GRAY),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        topBar.setOpaque(false);

        JButton btnPrev = new JButton("<");
        btnPrev.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPrev.setFocusPainted(false);
        btnPrev.setPreferredSize(new Dimension(32, 28));
        btnPrev.addActionListener(e -> {
            selectedYear--;
            loadCalendarData();
            buildCalendar();
        });

        yearLabel = new JLabel(String.valueOf(selectedYear));
        yearLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton btnNext = new JButton(">");
        btnNext.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNext.setFocusPainted(false);
        btnNext.setPreferredSize(new Dimension(32, 28));
        btnNext.addActionListener(e -> {
            selectedYear++;
            loadCalendarData();
            buildCalendar();
        });

        topBar.add(btnPrev);
        topBar.add(yearLabel);
        topBar.add(btnNext);

        panel.add(topBar, BorderLayout.NORTH);

        monthsContainer = new JPanel(new GridLayout(3, 4, 6, 6));
        monthsContainer.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(monthsContainer);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void buildCalendar() {
        monthsContainer.removeAll();
        dayLabelMap.clear();
        yearLabel.setText(String.valueOf(selectedYear));

        for (int m = 1; m <= 12; m++) {
            monthsContainer.add(buildMonthPanel(m));
        }

        monthsContainer.revalidate();
        monthsContainer.repaint();
    }

    private JPanel buildMonthPanel(int month) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        panel.setMinimumSize(new Dimension(160, 160));

        JLabel titleLabel = new JLabel(MONTH_NAMES[month - 1], SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBackground(MONTH_TITLE_BG);
        titleLabel.setOpaque(true);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
            titleLabel.getPreferredSize().height));
        panel.add(titleLabel);

        JPanel headerRow = new JPanel(new GridLayout(1, 8, 0, 0)) {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = 26;
                return d;
            }
        };
        headerRow.setBackground(HEADER_BG);

        JLabel semHeader = new JLabel("Sem", SwingConstants.CENTER);
        semHeader.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        semHeader.setForeground(WEEK_COLOR);
        semHeader.setOpaque(true);
        semHeader.setBackground(HEADER_BG);
        semHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, SEPARATOR));
        headerRow.add(semHeader);

        for (int d = 0; d < 7; d++) {
            JLabel lbl = new JLabel(DAY_NAMES[d], SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 8));
            lbl.setForeground(d >= 5 ? SUNDAY_FG : Color.DARK_GRAY);
            lbl.setOpaque(true);
            lbl.setBackground(HEADER_BG);
            lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, d < 6 ? 1 : 0, SEPARATOR));
            headerRow.add(lbl);
        }

        panel.add(headerRow);

        JPanel weeksPanel = new JPanel(new GridLayout(6, 8, 0, 0));

        YearMonth ym = YearMonth.of(selectedYear, month);
        int daysInMonth = ym.lengthOfMonth();
        DayOfWeek firstDow = ym.atDay(1).getDayOfWeek();
        int startCol = (firstDow.getValue() - 1) % 7;

        LocalDate today = LocalDate.now();
        WeekFields wf = WeekFields.of(Locale.getDefault());

        int day = 1;
        int rowCount = (int) Math.ceil((startCol + daysInMonth) / 7.0);
        if (rowCount < 4) rowCount = 4;

        for (int week = 0; week < 6; week++) {
            LocalDate firstDayOfWeek = ym.atDay(1).minusDays(startCol).plusDays(week * 7);
            int weekNum = firstDayOfWeek.get(wf.weekOfWeekBasedYear());

            JLabel weekLabel = new JLabel(String.valueOf(weekNum), SwingConstants.CENTER);
            weekLabel.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            weekLabel.setForeground(WEEK_COLOR);
            weekLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, SEPARATOR));
            weekLabel.setPreferredSize(new Dimension(28, 18));

            if (week >= rowCount && day > daysInMonth) {
                weekLabel.setVisible(false);
            }
            weeksPanel.add(weekLabel);

            for (int d = 0; d < 7; d++) {
                JLabel dayLabel = new JLabel("", SwingConstants.CENTER);
                dayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                dayLabel.setOpaque(true);
                dayLabel.setPreferredSize(new Dimension(20, 18));

                if (week == 0 && d < startCol) {
                    dayLabel.setText("");
                    dayLabel.setBackground(Color.WHITE);
                } else if (day > daysInMonth) {
                    dayLabel.setText("");
                    dayLabel.setBackground(Color.WHITE);
                } else {
                    dayLabel.setText(String.valueOf(day));
                    LocalDate cellDate = ym.atDay(day);

                    Color assignedColor = colorData.get(cellDate);
                    String plantillaName = calendarioData.get(cellDate);
                    final boolean hasAssignment = plantillaName != null;

                    if (hasAssignment && assignedColor != null) {
                        dayLabel.setBackground(assignedColor);
                        dayLabel.setToolTipText(plantillaName);
                    } else if (cellDate.equals(today)) {
                        dayLabel.setBackground(TODAY_BG);
                    } else if (d >= 5) {
                        dayLabel.setBackground(WEEKEND_BG);
                    } else {
                        dayLabel.setBackground(Color.WHITE);
                    }

                    if (d == 6) {
                        dayLabel.setForeground(SUNDAY_FG);
                    } else {
                        dayLabel.setForeground(Color.DARK_GRAY);
                    }

                    final int dayNum = day;
                    final int monthNum = month;
                    final int dow = d;
                    final LocalDate clickedDate = cellDate;

                    dayLabelMap.put(clickedDate, dayLabel);
                    dayLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    dayLabel.addMouseListener(new MouseAdapter() {
                        public void mousePressed(MouseEvent e) {
                            if (e.getButton() == MouseEvent.BUTTON1) {
                                clearDragPreview();
                                dragStart = clickedDate;
                                dragEnd = clickedDate;
                                updateDragPreview(dragStart, dragStart);
                            }
                        }
                        public void mouseReleased(MouseEvent e) {
                            if (e.getButton() == MouseEvent.BUTTON3) {
                                if (hasAssignment) removeAssignment(clickedDate);
                            } else if (dragStart != null) {
                                clearDragPreview();
                                LocalDate end = dragEnd != null ? dragEnd : dragStart;
                                if (dragStart.equals(end)) {
                                    if (hasAssignment) removeAssignment(clickedDate);
                                    else assignPlantilla(clickedDate);
                                } else {
                                    LocalDate from = dragStart.isBefore(end) ? dragStart : end;
                                    LocalDate to = dragStart.isBefore(end) ? end : dragStart;
                                    boolean anyAssigned = false;
                                    LocalDate d = from;
                                    while (!d.isAfter(to)) {
                                        if (calendarioData.containsKey(d)) { anyAssigned = true; break; }
                                        d = d.plusDays(1);
                                    }
                                    if (anyAssigned) unassignRange(from, to);
                                    else assignRange(dragStart, end);
                                }
                            }
                            dragStart = null;
                            dragEnd = null;
                        }
                        public void mouseEntered(MouseEvent e) {
                            if (dragStart != null) {
                                if (!clickedDate.equals(dragEnd)) {
                                    dragEnd = clickedDate;
                                    updateDragPreview(dragStart, dragEnd);
                                }
                            } else {
                                if (hasAssignment && assignedColor != null)
                                    dayLabel.setBackground(assignedColor.darker());
                                else dayLabel.setBackground(dayLabel.getBackground().darker());
                            }
                        }
                        public void mouseExited(MouseEvent e) {
                            if (dragStart != null) return;
                            if (hasAssignment && assignedColor != null)
                                dayLabel.setBackground(assignedColor);
                            else if (clickedDate.equals(today))
                                dayLabel.setBackground(TODAY_BG);
                            else if (dow >= 5)
                                dayLabel.setBackground(WEEKEND_BG);
                            else dayLabel.setBackground(Color.WHITE);
                        }
                    });

                    day++;
                }

                if (week >= rowCount && day - 1 > daysInMonth) {
                    dayLabel.setVisible(false);
                }

                dayLabel.setBorder(BorderFactory.createMatteBorder(
                    0, 0, 1, d < 6 ? 1 : 0, SEPARATOR));
                weeksPanel.add(dayLabel);
            }
        }

        panel.add(weeksPanel);
        return panel;
    }

    private void assignPlantilla(LocalDate date) {
        if (selectedPlantilla == null) {
            JOptionPane.showMessageDialog(this,
                "Selecciona una plantilla en el panel izquierdo.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            CalendarioDAO dao = new CalendarioDAO();
            dao.save(new CalendarioEntry(selectedPlantilla.getId(), date, true));
            loadCalendarData();
            buildCalendar();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void assignRange(LocalDate start, LocalDate end) {
        if (selectedPlantilla == null) {
            JOptionPane.showMessageDialog(this,
                "Selecciona una plantilla en el panel izquierdo.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (start.isAfter(end)) {
            LocalDate tmp = start; start = end; end = tmp;
        }
        try {
            CalendarioDAO dao = new CalendarioDAO();
            LocalDate d = start;
            while (!d.isAfter(end)) {
                dao.save(new CalendarioEntry(selectedPlantilla.getId(), d, true));
                d = d.plusDays(1);
            }
            loadCalendarData();
            buildCalendar();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void unassignRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            LocalDate tmp = start; start = end; end = tmp;
        }
        try {
            CalendarioDAO dao = new CalendarioDAO();
            LocalDate d = start;
            while (!d.isAfter(end)) {
                for (CalendarioEntry e : calendarioEntries) {
                    if (e.getFecha().equals(d)) {
                        dao.deleteByPlantillaAndDate(e.getPlantillaId(), d);
                        break;
                    }
                }
                d = d.plusDays(1);
            }
            loadCalendarData();
            buildCalendar();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void removeAssignment(LocalDate date) {
        String name = calendarioData.get(date);
        if (name == null) return;
        try {
            CalendarioDAO dao = new CalendarioDAO();
            for (CalendarioEntry e : calendarioEntries) {
                if (e.getFecha().equals(date)) {
                    dao.deleteByPlantillaAndDate(e.getPlantillaId(), date);
                    break;
                }
            }
            loadCalendarData();
            buildCalendar();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadPlantillas() {
        try {
            PlantillaHeaderDAO dao = new PlantillaHeaderDAO();
            List<PlantillaHeader> all = dao.findAll();
            plantillas = new ArrayList<>();
            for (PlantillaHeader ph : all) {
                if ("ACTIVA".equalsIgnoreCase(ph.getEstado())) {
                    plantillas.add(ph);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            plantillas = new ArrayList<>();
        }
        refreshList();
    }

    private void loadCalendarData() {
        calendarioData.clear();
        colorData.clear();
        try {
            CalendarioDAO dao = new CalendarioDAO();
            calendarioEntries = dao.findByYear(selectedYear);
            for (CalendarioEntry e : calendarioEntries) {
                if (e.isActivo()) {
                    String name = e.getPlantillaNombre() != null
                        ? e.getPlantillaNombre() : "Plantilla #" + e.getPlantillaId();
                    calendarioData.put(e.getFecha(), name);
                    colorData.put(e.getFecha(), getPlantillaColor(e.getPlantillaId()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            calendarioEntries = new ArrayList<>();
        }
    }

    private void clearDragPreview() {
        for (JLabel lbl : previewLabels) {
            restoreDayBackground(lbl);
        }
        previewLabels.clear();
    }

    private void updateDragPreview(LocalDate from, LocalDate to) {
        clearDragPreview();
        if (from == null || to == null) return;
        LocalDate start = from.isBefore(to) ? from : to;
        LocalDate end = from.isBefore(to) ? to : from;
        Color previewColor;
        if (selectedPlantilla != null) {
            Color c = parseColor(selectedPlantilla.getColor());
            previewColor = new Color(
                c.getRed() + (255 - c.getRed()) * 2 / 5,
                c.getGreen() + (255 - c.getGreen()) * 2 / 5,
                c.getBlue() + (255 - c.getBlue()) * 2 / 5);
        } else {
            previewColor = DRAG_PREVIEW;
        }
        LocalDate d = start;
        while (!d.isAfter(end)) {
            JLabel lbl = dayLabelMap.get(d);
            if (lbl != null) {
                lbl.setBackground(previewColor);
                previewLabels.add(lbl);
            }
            d = d.plusDays(1);
        }
    }

    private void restoreDayBackground(JLabel lbl) {
        for (Map.Entry<LocalDate, JLabel> e : dayLabelMap.entrySet()) {
            if (e.getValue() == lbl) {
                LocalDate date = e.getKey();
                Color assigned = colorData.get(date);
                if (assigned != null) lbl.setBackground(assigned);
                else if (date.equals(LocalDate.now())) lbl.setBackground(TODAY_BG);
                else if (date.getDayOfWeek().getValue() >= 6) lbl.setBackground(WEEKEND_BG);
                else lbl.setBackground(Color.WHITE);
                return;
            }
        }
    }

    private Color getPlantillaColor(int plantillaId) {
        for (PlantillaHeader ph : plantillas) {
            if (ph.getId() == plantillaId) {
                return parseColor(ph.getColor());
            }
        }
        return new Color(180, 220, 200);
    }

    private Color parseColor(String hex) {
        try { return Color.decode(hex); } catch (Exception e) { return Color.GRAY; }
    }

    private void refreshList() {
        listPanel.removeAll();
        for (int i = 0; i < plantillas.size(); i++) {
            listPanel.add(createPlantillaRow(plantillas.get(i), i));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createPlantillaRow(PlantillaHeader ph, int index) {
        JPanel row = new JPanel(new BorderLayout(4, 0)) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                if (index == selectedPlantillaIndex) {
                    g.setColor(ROW_SELECTED);
                } else {
                    g.setColor(index % 2 == 0 ? ROW_EVEN : ROW_ODD);
                }
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(SEPARATOR);
                g.drawLine(10, getHeight() - 1, getWidth() - 10, getHeight() - 1);
            }
        };
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setPreferredSize(new Dimension(0, 30));
        row.setOpaque(false);

        Color c = parseColor(ph.getColor());
        JButton colorBtn = new JButton(" ");
        colorBtn.setBackground(c);
        colorBtn.setPreferredSize(new Dimension(18, 18));
        colorBtn.setMinimumSize(new Dimension(18, 18));
        colorBtn.setMaximumSize(new Dimension(18, 18));
        colorBtn.setFocusPainted(false);
        colorBtn.setBorder(BorderFactory.createEmptyBorder());
        colorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this,
                "Color de plantilla", c);
            if (newColor != null) {
                String hex = String.format("#%02X%02X%02X",
                    newColor.getRed(), newColor.getGreen(), newColor.getBlue());
                ph.setColor(hex);
                colorBtn.setBackground(newColor);
                try {
                    new PlantillaHeaderDAO().update(ph.getId(), ph.getNombre(),
                        ph.getEstado(), hex);
                    loadCalendarData();
                    buildCalendar();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
        row.add(colorBtn, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new BorderLayout(4, 0));
        infoPanel.setOpaque(false);

        JLabel lblName = new JLabel(" " + ph.getNombre());
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoPanel.add(lblName, BorderLayout.CENTER);

        row.add(infoPanel, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectPlantilla(index);
                if (e.getClickCount() == 2 && onPlantillaDoubleClick != null) {
                    onPlantillaDoubleClick.run();
                }
            }
        });
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return row;
    }

    private void selectPlantilla(int index) {
        selectedPlantillaIndex = index;
        if (index >= 0 && index < plantillas.size()) {
            selectedPlantilla = plantillas.get(index);
        }
        refreshList();
    }

    private void applyFilter() {
        String filter = filterField.getText().toLowerCase().trim();
        Component[] rows = listPanel.getComponents();
        for (int i = 0; i < rows.length && i < plantillas.size(); i++) {
            PlantillaHeader ph = plantillas.get(i);
            boolean show = filter.isEmpty()
                || ph.getNombre().toLowerCase().contains(filter);
            rows[i].setVisible(show);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}
