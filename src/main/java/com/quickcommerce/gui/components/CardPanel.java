package com.quickcommerce.gui.components;

import com.quickcommerce.gui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A JPanel that paints itself as a rounded, gradient-filled card
 * with an optional accent top-border stripe.
 */
public class CardPanel extends JPanel {

    private Color  accentColor = null;   // null = no accent stripe
    private boolean elevated   = false;

    public CardPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
    }

    public CardPanel(LayoutManager layout) {
        setOpaque(false);
        setLayout(layout);
    }

    public void setAccentColor(Color c) { this.accentColor = c; repaint(); }
    public void setElevated(boolean e)  { this.elevated = e;    repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int arc = UITheme.RADIUS_LARGE;

        // Optional drop shadow
        if (elevated) {
            for (int i = 6; i >= 1; i--) {
                g2.setColor(new Color(0, 0, 0, 18 * i / 6));
                g2.fill(new RoundRectangle2D.Float(i, i + 2, w - i * 2, h - i * 2, arc, arc));
            }
        }

        // Card body gradient
        GradientPaint gp = UITheme.cardGradient(0, 0, 0, h);
        g2.setPaint(gp);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // Subtle border
        g2.setColor(UITheme.BORDER_SUBTLE);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

        // Accent top stripe
        if (accentColor != null) {
            g2.setColor(accentColor);
            g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            g2.fillRect(0, 0, w, 4);
            g2.setClip(null);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
