package com.quickcommerce.gui.components;

import com.quickcommerce.gui.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Factory / helper that produces consistently styled {@link JTable} instances
 * matching the dark Quick Commerce theme.
 */
public final class StyledTable {

    private StyledTable() {}

    /**
     * Creates a fully styled, non-editable JTable wrapped in a JScrollPane.
     *
     * @param columnNames column headers
     * @param data        2D array of row data
     * @return a configured JScrollPane containing the table
     */
    public static JScrollPane create(String[] columnNames, Object[][] data) {
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        styleScrollPane(scroll);
        return scroll;
    }

    /**
     * Creates an empty table (no data) with the given columns —
     * useful when you'll populate it dynamically later.
     */
    public static JScrollPane createEmpty(String[] columnNames) {
        return create(columnNames, new Object[0][columnNames.length]);
    }

    // ------------------------------------------------------------------

    public static void styleTable(JTable table) {
        table.setBackground(UITheme.BG_PANEL);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0x2ECC71_28, true));
        table.setSelectionForeground(UITheme.ACCENT_GREEN);
        table.setFocusable(false);

        // Alternating row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (sel) {
                    setBackground(new Color(46, 204, 113, 30));
                    setForeground(UITheme.ACCENT_GREEN);
                } else {
                    setBackground(row % 2 == 0 ? UITheme.BG_PANEL : UITheme.BG_CARD);
                    setForeground(UITheme.TEXT_PRIMARY);
                }
                return this;
            }
        });

        // Header
        JTableHeader header = table.getTableHeader();
        header.setBackground(UITheme.BG_SIDEBAR);
        header.setForeground(UITheme.TEXT_SECONDARY);
        header.setFont(UITheme.FONT_LABEL);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_NORMAL));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    public static void styleScrollPane(JScrollPane scroll) {
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(UITheme.BG_PANEL);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_SUBTLE, 1));
        scroll.getVerticalScrollBar().setBackground(UITheme.BG_PANEL);
    }
}
