package com.plantillagen.ui;

import com.plantillagen.model.Operario;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class FichaOperarioRow extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color ROW_EVEN = new Color(255, 255, 255);
    private static final Color ROW_ODD = new Color(247, 249, 252);
    private static final Color ROW_HOVER = new Color(232, 240, 254);
    private static final Color SEPARATOR = new Color(228, 231, 237);
    private static final Color TEXT_COLOR = new Color(45, 50, 60);
    private static final Color INDEX_COLOR = new Color(150, 155, 165);

    private final Operario operario;
    private final int photoSize;
    private Image scaledPhoto;
    private boolean hover;
    private boolean formacion;

    public FichaOperarioRow(Operario operario, int photoSize) {
        this.operario = operario;
        this.photoSize = photoSize;

        this.scaledPhoto = scaleImage(operario.getFoto(), photoSize);

        setOpaque(false);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, photoSize + 14));
        setPreferredSize(new Dimension(0, photoSize + 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        setupTransferHandler();
        setupDragGesture();
        setupHover();
    }

    public Operario getOperario() {
        return operario;
    }

    private static Image scaleImage(Image src, int size) {
        if (src == null) {
            return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        }
        int w = src.getWidth(null);
        int h = src.getHeight(null);
        if (w <= 0 || h <= 0) {
            return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        }
        BufferedImage argb = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = argb.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(argb, 0, 0, size, size, null);
        g.dispose();
        return scaled;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getParent() == null) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color bg = getRowBackground();
        g2d.setColor(bg);
        g2d.fillRect(0, 0, w, h);

        int indexColW = 30;
        int padX = indexColW + 6;
        int photoY = (h - photoSize) / 2;

        int visibleIndex = getVisibleIndex();
        if (visibleIndex >= 0) {
            g2d.setColor(INDEX_COLOR);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics ifm = g2d.getFontMetrics();
            String istr = String.valueOf(visibleIndex + 1);
            int ix = indexColW - ifm.stringWidth(istr) - 4;
            int iy = (h + ifm.getAscent() - ifm.getDescent()) / 2;
            g2d.drawString(istr, ix, iy);
        }

        Shape oldClip = g2d.getClip();
        g2d.clip(new Ellipse2D.Float(padX, photoY, photoSize, photoSize));
        g2d.drawImage(scaledPhoto, padX, photoY, photoSize, photoSize, this);
        g2d.setClip(oldClip);

        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g2d.getFontMetrics();
        String text = operario.getCodigo() + "  " + operario.getNombre();
        int textX = padX + photoSize + 10;
        int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(text, textX, textY);

        g2d.setColor(SEPARATOR);
        g2d.drawLine(padX / 2, h - 1, w - padX / 2, h - 1);

        g2d.setColor(new Color(0, 0, 0, 12));
        g2d.drawLine(padX / 2, h, w - padX / 2, h);
        g2d.setColor(new Color(0, 0, 0, 6));
        g2d.drawLine(padX / 2, h + 1, w - padX / 2, h + 1);

        int avg = (operario.getEfi() + operario.getCal() + operario.getSeg()
            + operario.getIni() + operario.getPol()) / 5;
        Color avgColor;
        if (avg > 80) avgColor = new Color(50, 180, 40);
        else if (avg > 60) avgColor = new Color(230, 140, 20);
        else avgColor = new Color(210, 50, 40);
        String avgStr = String.valueOf(avg);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics afm = g2d.getFontMetrics();
        int aw = afm.stringWidth(avgStr);
        int badgeLeft = formacion ? 50 : 28;
        int ax = w - badgeLeft;
        int ay = (h + afm.getAscent() - afm.getDescent()) / 2;
        g2d.setColor(avgColor);
        g2d.drawString(avgStr, ax - aw / 2, ay);

        if (formacion) {
            int fx = w - 22;
            int fy = (h - 16) / 2;
            g2d.setColor(Color.BLACK);
            g2d.fillRoundRect(fx - 1, fy - 1, 18, 18, 4, 4);
            g2d.setColor(new Color(255, 215, 0));
            g2d.fillRoundRect(fx, fy, 16, 16, 4, 4);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics ffm = g2d.getFontMetrics();
            String ft = "F";
            int fw2 = ffm.stringWidth(ft);
            g2d.drawString(ft, fx + (16 - fw2) / 2, fy + 13);
        }

        g2d.dispose();
    }

    private Color getRowBackground() {
        if (hover) return ROW_HOVER;

        java.awt.Container parent = getParent();
        if (parent != null) {
            Component[] siblings = parent.getComponents();
            for (int i = 0; i < siblings.length; i++) {
                if (siblings[i] == this) {
                    return (i % 2 == 0) ? ROW_EVEN : ROW_ODD;
                }
            }
        }
        return ROW_EVEN;
    }

    private int getVisibleIndex() {
        java.awt.Container parent = getParent();
        if (parent == null) return -1;
        int idx = 0;
        for (Component c : parent.getComponents()) {
            if (c == this) return idx;
            if (c.isVisible() && c instanceof FichaOperarioRow) idx++;
        }
        return -1;
    }

    public boolean isFormacion() {
        return formacion;
    }

    private Runnable onStateChanged;

    public void setOnStateChanged(Runnable callback) {
        this.onStateChanged = callback;
    }

    private void setupTransferHandler() {
        setTransferHandler(new TransferHandler() {
            @Override
            public int getSourceActions(javax.swing.JComponent c) {
                return MOVE;
            }

            @Override
            protected Transferable createTransferable(javax.swing.JComponent c) {
                return new OperarioTransferHandler.OperarioTransferable(getOperario());
            }

            @Override
            public Icon getVisualRepresentation(Transferable t) {
                return new ImageIcon(scaledPhoto);
            }

            @Override
            protected void exportDone(javax.swing.JComponent source,
                                       Transferable data, int action) {
                if (action == MOVE) {
                    source.setVisible(false);
                    java.awt.Container parent = source.getParent();
                    if (parent != null) {
                        parent.remove(source);
                        parent.revalidate();
                        parent.repaint();
                    }
                }
            }

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(
                    OperarioTransferHandler.OPERARIO_FLAVOR);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                java.awt.Container parent = FichaOperarioRow.this.getParent();
                if (parent instanceof javax.swing.JComponent) {
                    TransferHandler parentHandler =
                        ((javax.swing.JComponent) parent).getTransferHandler();
                    if (parentHandler != null) {
                        return parentHandler.importData(support);
                    }
                }
                return false;
            }
        });
    }

    private void setupDragGesture() {
        DragSource.getDefaultDragSource()
            .createDefaultDragGestureRecognizer(
                this, DnDConstants.ACTION_MOVE,
                new DragGestureListener() {
                    @Override
                    public void dragGestureRecognized(DragGestureEvent dge) {
                        Transferable transferable =
                            new OperarioTransferHandler.OperarioTransferable(
                                getOperario());
                        DragSource.getDefaultDragSource().startDrag(
                            dge, DragSource.DefaultMoveDrop,
                            scaledPhoto, new java.awt.Point(photoSize / 2, photoSize / 2),
                            transferable, new java.awt.dnd.DragSourceAdapter() {
                                @Override
                                public void dragDropEnd(
                                        java.awt.dnd.DragSourceDropEvent dsde) {
                                    // source removal is handled by target's importData
                                }
                            });
                    }
                });
    }

    private void setupHover() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    formacion = !formacion;
                    repaint();
                    if (onStateChanged != null) {
                        onStateChanged.run();
                    }
                }
            }
        };
        addMouseListener(adapter);
    }
}
