package com.plantillagen.ui;

import com.plantillagen.model.Operario;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class FichaOperario extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int SHADOW_GAP = 5;
    private static final int PAD = 8;
    private static final int PHOTO_TEXT_GAP = 10;
    private static final int BADGE_SIZE = 15;

    private final Operario operario;
    private int photoSize;
    private Image scaledPhoto;
    private boolean hover;
    private boolean lider;
    private boolean formacion;

    private String namePart;
    private String apellido1Part;
    private String apellido2Part;

    public FichaOperario(Operario operario, int photoSize) {
        this.operario = operario;
        this.photoSize = photoSize;

        this.scaledPhoto = scaleImage(operario.getFoto(), photoSize);

        setName(operario.getCodigo());
        setOpaque(false);

        String[] parts = operario.getNombre().split(" ");
        if (parts.length >= 1) namePart = parts[parts.length - 1];
        if (parts.length >= 2) apellido1Part = parts[0];
        if (parts.length >= 3) apellido2Part = parts[1];

        int cardW = photoSize + 110;
        int cardH = Math.max(PAD + photoSize + PAD + SHADOW_GAP, 86);
        setPreferredSize(new Dimension(cardW, cardH));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        setupTransferHandler();
        setupDragGesture();
        setupHover();
    }

    public Operario getOperario() {
        return operario;
    }

    public boolean isFormacion() {
        return formacion;
    }

    public void setLider(boolean lider) {
        this.lider = lider;
        repaint();
    }

    private Runnable onStateChanged;
    private Runnable onDoubleClick;

    public void setOnStateChanged(Runnable callback) {
        this.onStateChanged = callback;
    }

    public void setOnDoubleClick(Runnable callback) {
        this.onDoubleClick = callback;
    }

    public void setPhotoSize(int newSize) {
        this.photoSize = newSize;
        this.scaledPhoto = scaleImage(operario.getFoto(), photoSize);
        setPreferredSize(new Dimension(photoSize + 120, photoSize + 60));
        revalidate();
        repaint();
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

        int cx = 1;
        int cy = 1;
        int cw = w - 2 - SHADOW_GAP;
        int ch = h - 2 - SHADOW_GAP;

        for (int i = SHADOW_GAP; i >= 1; i--) {
            int alpha = (SHADOW_GAP - i + 1) * 15;
            g2d.setColor(new Color(0, 0, 0, alpha));
            g2d.fill(new RoundRectangle2D.Float(cx + i, cy + i, cw, ch, 8, 8));
        }

        g2d.setColor(hover ? new Color(240, 245, 255) : Color.WHITE);
        g2d.fill(new RoundRectangle2D.Float(cx, cy, cw, ch, 8, 8));

        g2d.setColor(new Color(210, 210, 210));
        g2d.draw(new RoundRectangle2D.Float(cx, cy, cw, ch, 8, 8));

        int photoX = cx + PAD;
        int photoY = cy + PAD;
        g2d.drawImage(scaledPhoto, photoX, photoY, photoSize, photoSize, this);

        int textX = photoX + photoSize + PHOTO_TEXT_GAP;
        int textY = photoY + PAD;

        g2d.setColor(new Color(45, 50, 60));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2d.drawString(operario.getCodigo(), textX, textY + 9);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        int textMaxW = cw - (textX - cx) - PAD;
        int textBase = textY + 19;
        if (namePart != null) g2d.drawString(clipText(g2d, namePart, textMaxW), textX, textBase + 7);
        if (apellido1Part != null) g2d.drawString(clipText(g2d, apellido1Part, textMaxW), textX, textBase + 19);
        if (apellido2Part != null) g2d.drawString(clipText(g2d, apellido2Part, textMaxW), textX, textBase + 31);

        int badgeX = cx + PAD;
        int badgeY = cy + ch - BADGE_SIZE - 3;

        int avg = (operario.getEfi() + operario.getCal() + operario.getSeg()
            + operario.getIni() + operario.getPol()) / 5;
        Color avgColor;
        if (avg > 80) avgColor = new Color(50, 180, 40);
        else if (avg > 60) avgColor = new Color(230, 140, 20);
        else avgColor = new Color(210, 50, 40);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String avgStr = String.valueOf(avg);
        FontMetrics avgFm = g2d.getFontMetrics();
        int avgW = avgFm.stringWidth(avgStr);
        int avgX = photoX + photoSize - avgW - 6;
        int avgY = badgeY + BADGE_SIZE - 3;
        int bx = avgX - 3;
        int by = avgY - avgFm.getAscent() + 2;
        int bw = avgW + 6;
        int bh = avgFm.getAscent() + 2;
        g2d.setColor(new Color(20, 20, 20, 100));
        g2d.fillRoundRect(bx, by, bw, bh, 6, 6);
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(avgStr, avgX + 1, avgY + 2);
        g2d.drawString(avgStr, avgX - 1, avgY + 2);
        g2d.setColor(avgColor);
        g2d.drawString(avgStr, avgX, avgY);

        if (lider) {
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fillRoundRect(badgeX + 1, badgeY + 1, BADGE_SIZE, BADGE_SIZE, 4, 4);
            g2d.setColor(new Color(255, 140, 0));
            g2d.fillRoundRect(badgeX, badgeY, BADGE_SIZE, BADGE_SIZE, 4, 4);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 8));
            FontMetrics lfm = g2d.getFontMetrics();
            String lb = "L";
            int lw = lfm.stringWidth(lb);
            g2d.drawString(lb, badgeX + (BADGE_SIZE - lw) / 2, badgeY + BADGE_SIZE - 4);
            badgeX += BADGE_SIZE + 4;
        }

        if (formacion) {
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fillRoundRect(badgeX + 1, badgeY + 1, BADGE_SIZE, BADGE_SIZE, 4, 4);
            g2d.setColor(new Color(255, 190, 30));
            g2d.fillRoundRect(badgeX, badgeY, BADGE_SIZE, BADGE_SIZE, 4, 4);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 8));
            FontMetrics bfm = g2d.getFontMetrics();
            String ft = "F";
            int fw = bfm.stringWidth(ft);
            g2d.drawString(ft, badgeX + (BADGE_SIZE - fw) / 2, badgeY + BADGE_SIZE - 4);
        }

        g2d.dispose();
    }

    private String clipText(Graphics2D g, String text, int maxW) {
        if (text == null) return "";
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(text) <= maxW) return text;
        while (text.length() > 0 && fm.stringWidth(text + "...") > maxW) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
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
                java.awt.Container parent = FichaOperario.this.getParent();
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
                } else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    if (onDoubleClick != null) {
                        onDoubleClick.run();
                    }
                }
            }
        };
        addMouseListener(adapter);
    }
}
