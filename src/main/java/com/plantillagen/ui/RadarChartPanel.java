package com.plantillagen.ui;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class RadarChartPanel extends JPanel {

    private static final Color PENTAGON_BG = new Color(22, 26, 34);
    private static final Color PENTAGON_BORDER = new Color(60, 65, 75);
    private static final Color GRID_COLOR = new Color(70, 75, 85);
    private static final Color AXIS_COLOR = new Color(100, 105, 115);
    private static final Color FILL_COLOR = new Color(90, 200, 60, 160);
    private static final Color STROKE_COLOR = new Color(60, 180, 40);
    private static final Color LABEL_COLOR = new Color(160, 165, 175);
    private static final Color VALUE_COLOR = new Color(220, 225, 235);

    private static final String[] LABELS = {"EFI", "CAL", "SEG", "INI", "POL"};
    private static final String[] FULL_NAMES = {
        "Eficiencia", "Calidad", "Seguridad y Rigor", "Iniciativa", "Polivalencia"
    };
    private static final String[] TOOLTIPS = {
        "<html><b>1. Eficiencia (EFI)</b><br>Mide la productividad y velocidad.<br>Capacidad de mantener el ritmo de la<br>l\u00ednea, cumplir tiempos de ciclo y<br>alcanzar objetivos diarios sin retrasar<br>al resto del equipo.</html>",
        "<html><b>2. Calidad (CAL)</b><br>Representa la precisi\u00f3n y atenci\u00f3n<br>al detalle. Porcentaje de errores o<br>scrap casi nulo. Sigue estrictamente<br>las especificaciones del producto.</html>",
        "<html><b>3. Seguridad y Rigor (SEG)</b><br>Eval\u00faa el cumplimiento de normas y<br>protocolos. Disciplina en EPIs, orden<br>(5S) y respeto a se\u00f1ales de seguridad.<br>Pilar de la fiabilidad.</html>",
        "<html><b>4. Iniciativa (INI)</b><br>Capacidad de resoluci\u00f3n y proactividad.<br>Detecta aver\u00edas, propone mejoras y<br>no necesita supervisi\u00f3n constante para<br>actuar ante imprevistos.</html>",
        "<html><b>5. Polivalencia (POL)</b><br>Adaptabilidad y trabajo en equipo.<br>Capacidad de rotar por diferentes<br>puestos, aprender r\u00e1pido nuevas<br>m\u00e1quinas y colaborar con compa\u00f1eros.</html>"
    };
    private static final int NUM_AXES = 5;
    private static final int NUM_LEVELS = 5;
    private static final int PADDING = 18;

    private int[] values = {50, 50, 50, 50, 50};
    private final Rectangle2D.Float[] labelRects = new Rectangle2D.Float[NUM_AXES];
    private final Rectangle2D.Float[] valueRects = new Rectangle2D.Float[NUM_AXES];

    public RadarChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(180, 160));
        setMinimumSize(new Dimension(140, 120));
        setToolTipText("");
        for (int i = 0; i < NUM_AXES; i++) {
            labelRects[i] = new Rectangle2D.Float();
            valueRects[i] = new Rectangle2D.Float();
        }
    }

    public void setValues(int efi, int cal, int seg, int ini, int pol) {
        values[0] = efi;
        values[1] = cal;
        values[2] = seg;
        values[3] = ini;
        values[4] = pol;
        repaint();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (event == null) return null;
        int mx = event.getX();
        int my = event.getY();
        for (int i = 0; i < NUM_AXES; i++) {
            if (valueRects[i].contains(mx, my)) {
                return FULL_NAMES[i] + ": " + values[i];
            }
            if (labelRects[i].contains(mx, my)) {
                return TOOLTIPS[i];
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int radius = Math.min(w, h) / 2 - PADDING;
        if (radius < 20) radius = 20;
        int cy = h / 2 + radius / 7;

        double angleOffset = -Math.PI / 2;

        Point2D.Double[] outerPts = new Point2D.Double[NUM_AXES];
        for (int i = 0; i < NUM_AXES; i++) {
            double angle = angleOffset + i * 2 * Math.PI / NUM_AXES;
            outerPts[i] = new Point2D.Double(
                cx + radius * Math.cos(angle),
                cy + radius * Math.sin(angle));
        }

        Polygon bgPoly = new Polygon();
        for (int i = 0; i < NUM_AXES; i++) {
            bgPoly.addPoint((int) outerPts[i].x, (int) outerPts[i].y);
        }
        g2.setColor(PENTAGON_BG);
        g2.fillPolygon(bgPoly);
        g2.setColor(PENTAGON_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(bgPoly);

        for (int level = NUM_LEVELS - 1; level >= 1; level--) {
            double r = radius * level / NUM_LEVELS;
            Polygon poly = new Polygon();
            for (int i = 0; i < NUM_AXES; i++) {
                double angle = angleOffset + i * 2 * Math.PI / NUM_AXES;
                poly.addPoint((int) (cx + r * Math.cos(angle)), (int) (cy + r * Math.sin(angle)));
            }
            g2.setColor(GRID_COLOR);
            g2.setStroke(new BasicStroke(0.6f));
            g2.drawPolygon(poly);
        }

        g2.setColor(AXIS_COLOR);
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i < NUM_AXES; i++) {
            g2.drawLine(cx, cy, (int) outerPts[i].x, (int) outerPts[i].y);
        }

        Polygon dataPoly = new Polygon();
        for (int i = 0; i < NUM_AXES; i++) {
            double r = radius * values[i] / 99.0;
            double angle = angleOffset + i * 2 * Math.PI / NUM_AXES;
            dataPoly.addPoint((int) (cx + r * Math.cos(angle)), (int) (cy + r * Math.sin(angle)));
        }

        g2.setColor(FILL_COLOR);
        g2.fillPolygon(dataPoly);
        g2.setColor(STROKE_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(dataPoly);

        for (int i = 0; i < NUM_AXES; i++) {
            double r = radius * values[i] / 99.0;
            double angle = angleOffset + i * 2 * Math.PI / NUM_AXES;
            int dx = (int) (cx + r * Math.cos(angle));
            int dy = (int) (cy + r * Math.sin(angle));
            g2.setColor(STROKE_COLOR);
            g2.fillOval(dx - 3, dy - 3, 6, 6);
        }

        Font labelFont = new Font("Segoe UI", Font.BOLD, 11);
        Font valueFont = new Font("Segoe UI", Font.BOLD, 9);
        int labelGap = 14;
        for (int i = 0; i < NUM_AXES; i++) {
            double angle = angleOffset + i * 2 * Math.PI / NUM_AXES;
            int lx = (int) (cx + (radius + labelGap) * Math.cos(angle));
            int ly = (int) (cy + (radius + labelGap) * Math.sin(angle));

            g2.setColor(LABEL_COLOR);
            g2.setFont(labelFont);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(LABELS[i]);
            int th = fm.getAscent();
            g2.drawString(LABELS[i], lx - tw / 2, ly + th / 2);
            labelRects[i].setRect(lx - tw / 2 - 2, ly - th / 2 - 2, tw + 4, th + 4);

            double valueR = radius * values[i] / 99.0 + 12;
            int vx = (int) (cx + valueR * Math.cos(angle));
            int vy = (int) (cy + valueR * Math.sin(angle));
            g2.setColor(VALUE_COLOR);
            g2.setFont(valueFont);
            String vs = String.valueOf(values[i]);
            int vw = g2.getFontMetrics().stringWidth(vs);
            int vh = g2.getFontMetrics().getAscent();
            g2.drawString(vs, vx - vw / 2, vy + vh / 2);
            valueRects[i].setRect(vx - vw / 2 - 2, vy - vh / 2 - 2, vw + 4, vh + 4);
        }

        g2.dispose();
    }
}
