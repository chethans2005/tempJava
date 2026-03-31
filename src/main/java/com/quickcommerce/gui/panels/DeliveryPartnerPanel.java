package com.quickcommerce.gui.panels;

import com.quickcommerce.controller.OrderController;
import com.quickcommerce.enums.OrderStatus;
import com.quickcommerce.gui.UITheme;
import com.quickcommerce.gui.components.*;
import com.quickcommerce.model.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** All content panels rendered in the Delivery Partner role's MainFrame. */
public class DeliveryPartnerPanel {

    private final DeliveryPartner deliveryPartner;
    private final OrderController  orderController;

    public DeliveryPartnerPanel(DeliveryPartner dp, OrderController oc) {
        this.deliveryPartner = dp;
        this.orderController = oc;
    }

    // ===================================================================
    // HOME
    // ===================================================================
    public JPanel buildHome() {
        JPanel p = contentPane();
        p.add(pageTitle("🏍️  Delivery Dashboard"), BorderLayout.NORTH);

        List<Order> assigned = deliveryPartner.getAssignedOrders();
        long total     = assigned.size();
        long delivered = assigned.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long active    = assigned.stream().filter(o -> o.getStatus() != OrderStatus.DELIVERED
                && o.getStatus() != OrderStatus.CANCELLED).count();

        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 0));
        grid.setOpaque(false);
        grid.add(new StatCard("Total Assigned", String.valueOf(total),     "📋", UITheme.ACCENT_BLUE));
        grid.add(new StatCard("Delivered",       String.valueOf(delivered), "✅", UITheme.ACCENT_GREEN));
        grid.add(new StatCard("Active",          String.valueOf(active),    "🚀", UITheme.ACCENT_AMBER));

        // Availability badge
        CardPanel avail = new CardPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        avail.setAccentColor(deliveryPartner.isAvailable() ? UITheme.ACCENT_GREEN : UITheme.ACCENT_AMBER);
        JLabel availLbl = new JLabel(deliveryPartner.isAvailable()
                ? "🟢  You are AVAILABLE for new orders"
                : "🟡  You are currently BUSY");
        availLbl.setFont(UITheme.FONT_HEADING);
        availLbl.setForeground(deliveryPartner.isAvailable() ? UITheme.ACCENT_GREEN : UITheme.ACCENT_AMBER);
        avail.add(availLbl);

        // Recent assigned orders
        CardPanel tableCard = new CardPanel(new BorderLayout(0, 10));
        tableCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        tableCard.setAccentColor(UITheme.ACCENT_BLUE);
        JLabel tl = new JLabel("Assigned Orders");
        tl.setFont(UITheme.FONT_HEADING);
        tl.setForeground(UITheme.TEXT_PRIMARY);
        tableCard.add(tl, BorderLayout.NORTH);
        tableCard.add(buildOrderTable(), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(grid,      BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout(0, 16));
        mid.setOpaque(false);
        mid.add(avail,     BorderLayout.NORTH);
        mid.add(tableCard, BorderLayout.CENTER);
        center.add(mid, BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    // ===================================================================
    // ORDERS
    // ===================================================================
    public JPanel buildOrders() {
        JPanel p = contentPane();
        p.add(pageTitle("📋  My Assigned Orders"), BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Order ID", "Customer", "Address", "Total (₹)", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        deliveryPartner.getAssignedOrders().forEach(o -> model.addRow(new Object[]{
                o.getOrderId(),
                o.getCustomer().getName(),
                o.getCustomer().getDeliveryAddress(),
                String.format("%.2f", o.getTotalAmount()),
                o.getStatus().getDisplayName()
        }));

        JScrollPane scroll = new JScrollPane();
        JTable table = new JTable(model);
        StyledTable.styleTable(table);
        scroll.setViewportView(table);
        StyledTable.styleScrollPane(scroll);

        // Status update controls
        String[] statuses = {"Out for Delivery", "Delivered"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        styleSmallCombo(statusCombo);

        StyledButton updateBtn = new StyledButton("Update Status →", StyledButton.Style.PRIMARY);
        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(p, "Select an order first."); return; }
            Order order = deliveryPartner.getAssignedOrders().get(row);
            OrderStatus newStatus = statusCombo.getSelectedIndex() == 0
                    ? OrderStatus.OUT_FOR_DELIVERY : OrderStatus.DELIVERED;
            try {
                deliveryPartner.updateDeliveryStatus(order.getOrderId(), newStatus);
                model.setValueAt(newStatus.getDisplayName(), row, 4);
                JOptionPane.showMessageDialog(p, "Status updated to: " + newStatus.getDisplayName() + " ✓");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(p, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        CardPanel ctrl = new CardPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        ctrl.add(new JLabel("Set Status:") {{ setFont(UITheme.FONT_LABEL); setForeground(UITheme.TEXT_SECONDARY); }});
        ctrl.add(statusCombo);
        ctrl.add(updateBtn);

        p.add(scroll, BorderLayout.CENTER);
        p.add(ctrl,   BorderLayout.SOUTH);
        return p;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private JScrollPane buildOrderTable() {
        String[] cols = {"Order ID", "Customer", "Status", "Amount"};
        Object[][] data = deliveryPartner.getAssignedOrders().stream()
                .map(o -> new Object[]{
                        o.getOrderId(), o.getCustomer().getName(),
                        o.getStatus().getDisplayName(),
                        "₹" + String.format("%.2f", o.getTotalAmount())
                }).toArray(Object[][]::new);
        return StyledTable.create(cols, data);
    }

    private JPanel contentPane() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(UITheme.BG_DEEP);
        p.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        return p;
    }

    private JPanel pageTitle(String text) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_DISPLAY);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        row.add(lbl, BorderLayout.WEST);
        JSeparator sep = new JSeparator() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0, UITheme.ACCENT_AMBER, getWidth(),0, new Color(0,0,0,0)));
                g2.fillRect(0, 0, getWidth(), 2);
                g2.dispose();
            }
        };
        sep.setPreferredSize(new Dimension(0, 2));
        row.add(sep, BorderLayout.SOUTH);
        return row;
    }

    private void styleSmallCombo(JComboBox<String> cb) {
        cb.setBackground(UITheme.BG_INPUT);
        cb.setForeground(UITheme.TEXT_PRIMARY);
        cb.setFont(UITheme.FONT_BODY);
        cb.setPreferredSize(new Dimension(180, 36));
    }
}
