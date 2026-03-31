package com.quickcommerce.gui.components;

import com.quickcommerce.gui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Rounded password field with the same visual language as {@link StyledTextField}. */
public class StyledPasswordField extends JPasswordField {

    private boolean focused = false;

    public StyledPasswordField() {
        setOpaque(false);
        setForeground(UITheme.TEXT_PRIMARY);
        setCaretColor(UITheme.ACCENT_GREEN);
        setFont(UITheme.FONT_BODY);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setEchoChar('●');

        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { focused = true;  repaint(); }
            @Override public void focusLost (java.awt.event.FocusEvent e) { focused = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        int arc = UITheme.RADIUS_MEDIUM;
        if (focused) {
            g2.setColor(new Color(46, 204, 113, 30));
            g2.fill(new RoundRectangle2D.Float(-3, -3, w + 6, h + 6, arc + 6, arc + 6));
        }
        g2.setColor(UITheme.BG_INPUT);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
        g2.setColor(focused ? UITheme.ACCENT_GREEN : UITheme.BORDER_NORMAL);
        g2.setStroke(new BasicStroke(focused ? 1.5f : 1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, 42);
    }
}
