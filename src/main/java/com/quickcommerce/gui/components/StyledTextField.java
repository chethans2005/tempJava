package com.quickcommerce.gui.components;

import com.quickcommerce.gui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A custom-painted text field with rounded corners, placeholder support,
 * and a glowing focus ring.
 */
public class StyledTextField extends JTextField {

    private String placeholder;
    private boolean focused = false;

    public StyledTextField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setForeground(UITheme.TEXT_PRIMARY);
        setCaretColor(UITheme.ACCENT_GREEN);
        setFont(UITheme.FONT_BODY);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setBackground(UITheme.BG_INPUT);

        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { focused = true;  repaint(); }
            @Override public void focusLost (java.awt.event.FocusEvent e) { focused = false; repaint(); }
        });
    }

    public StyledTextField() { this(""); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int arc = UITheme.RADIUS_MEDIUM;

        // Glow when focused
        if (focused) {
            g2.setColor(new Color(46, 204, 113, 30));
            g2.fill(new RoundRectangle2D.Float(-3, -3, w + 6, h + 6, arc + 6, arc + 6));
        }

        // Background
        g2.setColor(UITheme.BG_INPUT);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // Border
        g2.setColor(focused ? UITheme.ACCENT_GREEN : UITheme.BORDER_NORMAL);
        g2.setStroke(new BasicStroke(focused ? 1.5f : 1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

        g2.dispose();
        super.paintComponent(g);

        // Placeholder
        if (getText().isEmpty() && !placeholder.isEmpty() && !isFocusOwner()) {
            Graphics2D ph = (Graphics2D) g.create();
            ph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ph.setColor(UITheme.TEXT_MUTED);
            ph.setFont(UITheme.FONT_BODY);
            Insets ins = getInsets();
            FontMetrics fm = ph.getFontMetrics();
            ph.drawString(placeholder, ins.left, (h - fm.getHeight()) / 2 + fm.getAscent());
            ph.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, 42);
    }
}
