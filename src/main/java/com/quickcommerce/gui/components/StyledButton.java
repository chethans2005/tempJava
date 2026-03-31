package com.quickcommerce.gui.components;

import com.quickcommerce.gui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * A fully custom-painted, rounded button with gradient fill,
 * hover glow, and press-down animations.
 */
public class StyledButton extends JButton {

    public enum Style { PRIMARY, SECONDARY, DANGER, GHOST }

    private final Style  style;
    private boolean      hovered  = false;
    private boolean      pressed  = false;
    private float        glowAlpha = 0f;

    public StyledButton(String text, Style style) {
        super(text);
        this.style = style;
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(UITheme.FONT_LABEL);
        setForeground(UITheme.TEXT_PRIMARY);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { pressed = true;  repaint(); }
            @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
        });
    }

    public StyledButton(String text) { this(text, Style.PRIMARY); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth(), h = getHeight();
        int arc = UITheme.RADIUS_MEDIUM;

        // ---- Background fill ----
        switch (style) {
            case PRIMARY -> {
                GradientPaint gp = pressed
                        ? new GradientPaint(0, 0, UITheme.ACCENT_TEAL, w, h, UITheme.ACCENT_GREEN)
                        : hovered
                        ? new GradientPaint(0, 0, new Color(0x3DE882), w, h, new Color(0x22D9B5))
                        : UITheme.accentGradient(0, 0, w, h);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
                setForeground(UITheme.TEXT_INVERSE);
            }
            case SECONDARY -> {
                Color fill = pressed ? UITheme.BG_CARD_HOVER
                        : hovered   ? UITheme.BG_CARD_HOVER
                        : UITheme.BG_CARD;
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
                g2.setColor(UITheme.BORDER_NORMAL);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, arc, arc));
            }
            case DANGER -> {
                Color fill = pressed ? UITheme.ACCENT_RED.darker()
                        : hovered   ? UITheme.ACCENT_RED.brighter()
                        : UITheme.ACCENT_RED;
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
                setForeground(Color.WHITE);
            }
            case GHOST -> {
                if (hovered || pressed) {
                    g2.setColor(new Color(UITheme.ACCENT_GREEN.getRed(),
                            UITheme.ACCENT_GREEN.getGreen(),
                            UITheme.ACCENT_GREEN.getBlue(), pressed ? 40 : 20));
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
                }
                setForeground(UITheme.ACCENT_GREEN);
            }
        }

        // ---- Glow ring on hover (PRIMARY only) ----
        if (style == Style.PRIMARY && hovered) {
            g2.setColor(new Color(0x2ECC71_50 | 0xFF000000, true));
            // We use a simple glow by drawing a slightly larger translucent shape
            g2.setColor(new Color(46, 204, 113, 40));
            g2.setStroke(new BasicStroke(4f));
            g2.draw(new RoundRectangle2D.Float(-2, -2, w + 4, h + 4, arc + 4, arc + 4));
        }

        g2.dispose();

        // Paint text on top
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width + 24, 110), Math.max(d.height, 36));
    }
}
