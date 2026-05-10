package com.plantillagen.ui;

import com.plantillagen.db.CalendarioDAO;
import com.plantillagen.db.PlantillaHeaderDAO;
import com.plantillagen.model.CalendarioEntry;
import com.plantillagen.model.PlantillaHeader;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarioTabPanel extends JPanel {

    private List<PlantillaHeader> plantillas;
    private int selectedYear;
    private JPanel listPanel;
    private JTextField filterField;
    private JPanel monthsContainer;
    private JLabel yearLabel;
    private int selectedPlantillaIndex = -1;
    private PlantillaHeader selectedPlantilla;

    private static final Color ROW_EVEN = new Color(255, 255, 255);
    private static final Color ROW_ODD = new Color(247, 249, 252);
    private static final Color ROW_SELECTED = new Color(200, 220, 255);

    private static final Color TODAY_BG = new Color(255, 243, 205);
    private static final Color WEEKEND_BG = new Color(248, 248, 248);
    private static final Color SUNDAY_FG = new Color(200, 50, 50);
    private static final Color HEADER_BG = new Color(240, 242, 245);
    private static final Color WEEK_COLOR = new Color(150, 155, 165);
    private static final Color MONTH_TITLE_BG = new Color(70, 90, 120);
    private static final Color SEPARATOR = new Color(228, 231, 237);

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
        buildCalendar();
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

        JButton btnPrev = new JButton("\u25C0");
        btnPrev.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPrev.setFocusPainted(false);
        btnPrev.setPreferredSize(new Dimension(36, 28));
        btnPrev.addActionListener(e -> {
            selectedYear--;
            buildCalendar();
        });

        yearLabel = new JLabel(String.valueOf(selectedYear));
        yearLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton btnNext = new JButton("\u25B6");
        btnNext.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNext.setFocusPainted(false);
        btnNext.setPreferredSize(new Dimension(36, 28));
        btnNext.addActionListener(e -> {
            selectedYear++;
            buildCalendar();
        });

        JComboBox<Integer> yearCombo = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 5; y <= currentYear + 5; y++) {
            yearCombo.addItem(y);
        }
        yearCombo.setSelectedItem(selectedYear);
        yearCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        yearCombo.addActionListener(e -> {
            Integer y = (Integer) yearCombo.getSelectedItem();
            if (y != null && y != selectedYear) {
                selectedYear = y;
                buildCalendar();
            }
        });

        topBar.add(btnPrev);
        topBar.add(yearLabel);
        topBar.add(btnNext);
        topBar.add(new JLabel("  Ir a:"));
        topBar.add(yearCombo);

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
        yearLabel.setText(String.valueOf(selectedYear));

        for (int m = 1; m <= 12; m++) {
            monthsContainer.add(buildMonthPanel(m));
        }

        monthsContainer.revalidate();
        monthsContainer.repaint();
    }

    private JPanel buildMonthPanel(int month) {
        JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        JLabel titleLabel = new JLabel(MONTH_NAMES[month - 1], SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBackground(MONTH_TITLE_BG);
        titleLabel.setOpaque(true);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel headerRow = new JPanel(new GridLayout(1, 8, 0, 0));
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
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            lbl.setForeground(d >= 5 ? SUNDAY_FG : Color.DARK_GRAY);
            lbl.setOpaque(true);
            lbl.setBackground(HEADER_BG);
            lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, d < 6 ? 1 : 0, SEPARATOR));
            headerRow.add(lbl);
        }

        panel.add(headerRow, BorderLayout.CENTER);

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

                    if (cellDate.equals(today)) {
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
                    dayLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    dayLabel.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            onDayClicked(monthNum, dayNum);
                        }
                        public void mouseEntered(MouseEvent e) {
                            dayLabel.setBackground(dayLabel.getBackground().darker());
                        }
                        public void mouseExited(MouseEvent e) {
                            LocalDate cd = YearMonth.of(selectedYear, monthNum).atDay(dayNum);
                            if (cd.equals(today)) dayLabel.setBackground(TODAY_BG);
                            else if (dow >= 5) dayLabel.setBackground(WEEKEND_BG);
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

        panel.add(weeksPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void onDayClicked(int month, int day) {
        System.out.println("Clicked: " + day + "/" + month + "/" + selectedYear);
    }

    private void loadPlantillas() {
        try {
            PlantillaHeaderDAO dao = new PlantillaHeaderDAO();
            plantillas = dao.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            plantillas = new ArrayList<>();
        }
        refreshList();
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

        JPanel infoPanel = new JPanel(new BorderLayout(4, 0));
        infoPanel.setOpaque(false);

        JLabel lblName = new JLabel(" " + ph.getNombre());
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoPanel.add(lblName, BorderLayout.CENTER);

        JLabel lblEstado = new JLabel(ph.getEstado());
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblEstado.setForeground(Color.GRAY);
        lblEstado.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        infoPanel.add(lblEstado, BorderLayout.EAST);

        row.add(infoPanel, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectPlantilla(index);
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
                || ph.getNombre().toLowerCase().contains(filter)
                || ph.getEstado().toLowerCase().contains(filter);
            rows[i].setVisible(show);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}
