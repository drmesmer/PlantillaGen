package com.plantillagen.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

public class WrapLayout extends FlowLayout {

    private static final long serialVersionUID = 1L;

    public WrapLayout() {
        super(LEFT, 5, 5);
    }

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return layoutSize(target, false);
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            Insets insets = target.getInsets();
            int maxWidth = targetWidth - insets.left - insets.right;
            int hgap = getHgap();
            int vgap = getVgap();

            int x = 0;
            int y = 0;
            int rowHeight = 0;
            int totalWidth = 0;

            for (int i = 0; i < target.getComponentCount(); i++) {
                Component c = target.getComponent(i);
                if (!c.isVisible()) continue;

                Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();

                if (x > 0 && x + hgap + d.width > maxWidth) {
                    totalWidth = Math.max(totalWidth, x);
                    y += rowHeight + vgap;
                    x = 0;
                    rowHeight = 0;
                }

                x += (x > 0 ? hgap : 0) + d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }

            totalWidth = Math.max(totalWidth, x);
            y += rowHeight;

            return new Dimension(totalWidth + insets.left + insets.right,
                    y + insets.top + insets.bottom);
        }
    }

    @Override
    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int maxWidth = target.getWidth() - insets.left - insets.right;
            int hgap = getHgap();
            int vgap = getVgap();
            int nmembers = target.getComponentCount();

            int x = insets.left;
            int y = insets.top;
            int rowHeight = 0;
            int rowStart = 0;

            for (int i = 0; i < nmembers; i++) {
                Component c = target.getComponent(i);
                if (!c.isVisible()) continue;

                Dimension d = c.getPreferredSize();

                if (i > rowStart && x + hgap + d.width > insets.left + maxWidth) {
                    layoutRow(target, rowStart, i - 1, y, rowHeight);
                    y += rowHeight + vgap;
                    x = insets.left;
                    rowHeight = 0;
                    rowStart = i;
                }

                x += (x > insets.left ? hgap : 0) + d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }

            layoutRow(target, rowStart, nmembers - 1, y, rowHeight);
        }
    }

    private void layoutRow(Container target, int start, int end, int y, int rowHeight) {
        int hgap = getHgap();
        int x = target.getInsets().left;

        for (int i = start; i <= end; i++) {
            Component c = target.getComponent(i);
            if (!c.isVisible()) continue;

            Dimension d = c.getPreferredSize();
            int cy = y + (rowHeight - d.height) / 2;
            c.setBounds(x, cy, d.width, d.height);
            x += d.width + hgap;
        }
    }
}
