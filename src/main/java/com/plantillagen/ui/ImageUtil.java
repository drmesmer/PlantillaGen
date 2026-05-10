package com.plantillagen.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ImageUtil {

    private static final Color[] COLORS = {
        new Color(66, 133, 244),
        new Color(219, 68, 55),
        new Color(244, 180, 0),
        new Color(15, 157, 88),
        new Color(171, 71, 188),
        new Color(255, 112, 67),
        new Color(0, 150, 136),
        new Color(63, 81, 181),
        new Color(233, 30, 99),
        new Color(121, 85, 72),
    };

    private static final int SOURCE_SIZE = 64;

    public static Image createPlaceholder(String codigo) {
        return createPlaceholder(SOURCE_SIZE, SOURCE_SIZE, codigo);
    }

    public static Image createPlaceholder(int width, int height, String codigo) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int hash = Math.abs(codigo.hashCode());
        Color bgColor = COLORS[hash % COLORS.length];

        g2d.setColor(bgColor);
        g2d.fillOval(2, 2, width - 4, height - 4);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, width / 3));
        FontMetrics fm = g2d.getFontMetrics();
        String text = String.valueOf(hash % 100);
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2d.drawString(text, (width - textWidth) / 2, (height + textHeight) / 2 - 3);

        g2d.dispose();
        return img;
    }
}
