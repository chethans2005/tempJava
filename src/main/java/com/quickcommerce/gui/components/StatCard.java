package com.quickcommerce.gui.components;

import com.quickcommerce.gui.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * A compact metric card showing a label, a large value, and a coloured icon/emoji.
 * Used on all dashboards for KPI-style summaries.
 */
public class StatCard extends CardPanel {

    private final JLabel valueLabel;
    private final JLabel titleLabel;
    private final JLabel iconLabel;

    public StatCard(String title, String value, String emoji, Color accentColor) {
        setLayout(new BorderLayout(8, 4));
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        setAccentColor(accentColor);
        setElevated(true);

        iconLabel = new JLabel(emoji);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        iconLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);
        textPanel.add(valueLabel);
        textPanel.add(titleLabel);

        add(textPanel,  BorderLayout.CENTER);
        add(iconLabel,  BorderLayout.EAST);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
        repaint();
    }

    public JLabel getValueLabel() { return valueLabel; }
}
