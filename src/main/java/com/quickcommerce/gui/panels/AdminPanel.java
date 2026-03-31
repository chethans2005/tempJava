package com.quickcommerce.gui.panels;

import com.quickcommerce.controller.OrderController;
import com.quickcommerce.controller.ProductController;
import com.quickcommerce.controller.UserController;
import com.quickcommerce.enums.OrderStatus;
import com.quickcommerce.gui.UITheme;
import com.quickcommerce.gui.components.*;
import com.quickcommerce.model.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;

/** All content panels rendered in the Administrator role's MainFrame. */
public class AdminPanel {

    private final Administrator  admin;
        private final UserController    userService;
        private final ProductController productService;
        private final OrderController   orderService;

        public AdminPanel(Administrator admin, UserController us, ProductController ps, OrderController os) {
                this.admin          = admin;
                this.userService    = us;
                this.productService = ps;
                this.orderService   = os;
    }

    // ===================================================================
    // HOME
    // ===================================================================
    public JPanel buildHome() {
        JPanel p = contentPane();
        p.add(pageTitle("🛡️  Admin Dashboard"), BorderLayout.NORTH);

        List<Order> allOrders = orderService.getAllOrders();
        List<User>  allUsers  = userService.getAllUsers();
        List<Product> allProd = productService.getAllProducts();

        long pending   = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long delivered = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        double revenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(Order::getTotalAmount).sum();

        JPanel topRow = new JPanel(new GridLayout(1, 4, 16, 0));
        topRow.setOpaque(false);
        topRow.add(new StatCard("Total Users",    String.valueOf(allUsers.size()),   "👥", UITheme.ACCENT_BLUE));
        topRow.add(new StatCard("Total Products", String.valueOf(allProd.size()),    "📦", UITheme.ACCENT_TEAL));
        topRow.add(new StatCard("Total Orders",   String.valueOf(allOrders.size()),  "🧾", UITheme.ACCENT_PURPLE));
        topRow.add(new StatCard("Revenue (₹)",    String.format("%.0f", revenue),   "💰", UITheme.ACCENT_GREEN));

        JPanel midRow = new JPanel(new GridLayout(1, 3, 16, 0));
        midRow.setOpaque(false);
        midRow.add(new StatCard("Pending",    String.valueOf(pending),   "⏳", UITheme.ACCENT_AMBER));
        midRow.add(new StatCard("Delivered",  String.valueOf(delivered), "✅", UITheme.ACCENT_GREEN));
        midRow.add(new StatCard("Cancelled",  String.valueOf(cancelled), "❌", UITheme.ACCENT_RED));

        // Active orders table
        CardPanel activeCard = new CardPanel(new BorderLayout(0, 10));
        activeCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        activeCard.setAccentColor(UITheme.ACCENT_PURPLE);
        JLabel tl = new JLabel("Active Orders");
        tl.setFont(UITheme.FONT_HEADING);
        tl.setForeground(UITheme.TEXT_PRIMARY);
        activeCard.add(tl, BorderLayout.NORTH);

        String[] cols = {"Order ID", "Customer", "Total (₹)", "Status"};
        Object[][] data = allOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.CANCELLED)
                .map(o -> new Object[]{
                        o.getOrderId(), o.getCustomer().getName(),
                        String.format("%.2f", o.getTotalAmount()), o.getStatus().getDisplayName()
                }).toArray(Object[][]::new);
        activeCard.add(StyledTable.create(cols, data), BorderLayout.CENTER);

        JPanel grid = new JPanel(new GridLayout(2, 1, 0, 16));
        grid.setOpaque(false);
        grid.add(topRow);
        grid.add(midRow);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(grid,       BorderLayout.NORTH);
        center.add(activeCard, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    // ===================================================================
    // USERS
    // ===================================================================
    public JPanel buildUsers() {
        JPanel p = contentPane();
        p.add(pageTitle("👥  User Management"), BorderLayout.NORTH);

        String[] cols = {"User ID", "Name", "Email", "Role", "Status"};
        Object[][] data = userService.getAllUsers().stream()
                .map(u -> new Object[]{
                        u.getUserId().substring(0, 8) + "…",
                        u.getName(), u.getEmail(), u.getRole(),
                        u.isLoggedIn() ? "Online" : "Offline"
                }).toArray(Object[][]::new);

        p.add(StyledTable.create(cols, data), BorderLayout.CENTER);

        // Role breakdown cards
        JPanel roleCards = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        roleCards.setOpaque(false);
        String[] roles = {"Customer", "Vendor", "Delivery Partner", "Administrator"};
        String[] emojis = {"🛍️", "🏪", "🏍️", "🛡️"};
        Color[]  colors = {UITheme.ACCENT_BLUE, UITheme.ACCENT_TEAL, UITheme.ACCENT_AMBER, UITheme.ACCENT_PURPLE};
        for (int i = 0; i < roles.length; i++) {
            final String role = roles[i];
            long count = userService.getAllUsers().stream()
                    .filter(u -> u.getRole().equals(role)).count();
            StatCard sc = new StatCard(role, String.valueOf(count), emojis[i], colors[i]);
            sc.setPreferredSize(new Dimension(180, 90));
            roleCards.add(sc);
        }
        p.add(roleCards, BorderLayout.SOUTH);
        return p;
    }

    // ===================================================================
    // ALL ORDERS
    // ===================================================================
    public JPanel buildOrders() {
        JPanel p = contentPane();
        p.add(pageTitle("📦  All Orders"), BorderLayout.NORTH);

        String[] cols = {"Order ID", "Customer", "Date", "Items", "Total (₹)", "Payment", "Status"};
        Object[][] data = orderService.getAllOrders().stream()
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .map(o -> new Object[]{
                        o.getOrderId(),
                        o.getCustomer().getName(),
                        o.getOrderDate().toLocalDate().toString(),
                        o.getOrderItems().size(),
                        String.format("%.2f", o.getTotalAmount()),
                        o.getPayment() != null ? o.getPayment().getPaymentMethod().getDisplayName() : "—",
                        o.getStatus().getDisplayName()
                }).toArray(Object[][]::new);

        JScrollPane scroll = StyledTable.create(cols, data);
        JTable jt = (JTable) ((JViewport) scroll.getComponent(0)).getView();

        // Manual status override
        String[] statuses = {"Pending","Confirmed","Preparing","Out for Delivery","Delivered","Cancelled"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        styleSmallCombo(statusCombo);

        StyledButton overrideBtn = new StyledButton("Override Status", StyledButton.Style.SECONDARY);
        overrideBtn.addActionListener(e -> {
            int row = jt.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(p, "Select an order first."); return; }
            Order order = orderService.getAllOrders().get(row);
            OrderStatus ns = OrderStatus.values()[statusCombo.getSelectedIndex()];
            orderService.updateOrderStatus(order.getOrderId(), ns);
            ((javax.swing.table.DefaultTableModel) jt.getModel())
                    .setValueAt(ns.getDisplayName(), row, 6);
        });

        CardPanel ctrl = new CardPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        ctrl.add(new JLabel("Set Status:") {{ setFont(UITheme.FONT_LABEL); setForeground(UITheme.TEXT_SECONDARY); }});
        ctrl.add(statusCombo);
        ctrl.add(overrideBtn);

        p.add(scroll, BorderLayout.CENTER);
        p.add(ctrl,   BorderLayout.SOUTH);
        return p;
    }

    // ===================================================================
    // REPORTS
    // ===================================================================
    public JPanel buildReports() {
        JPanel p = contentPane();
        p.add(pageTitle("📊  System Reports"), BorderLayout.NORTH);

        List<Order> allOrders = orderService.getAllOrders();
        double revenue  = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(Order::getTotalAmount).sum();
        double pending  = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .mapToDouble(Order::getTotalAmount).sum();
        double avgOrder = allOrders.isEmpty() ? 0 :
                allOrders.stream().mapToDouble(Order::getTotalAmount).average().orElse(0);

        // Metric cards
        JPanel metrics = new JPanel(new GridLayout(1, 3, 16, 0));
        metrics.setOpaque(false);
        metrics.add(new StatCard("Confirmed Revenue", "₹" + String.format("%.0f", revenue),    "💰", UITheme.ACCENT_GREEN));
        metrics.add(new StatCard("Pending Value",      "₹" + String.format("%.0f", pending),   "⏳", UITheme.ACCENT_AMBER));
        metrics.add(new StatCard("Avg Order Value",    "₹" + String.format("%.0f", avgOrder),  "📈", UITheme.ACCENT_BLUE));

        // Order breakdown by status
        CardPanel breakdownCard = new CardPanel(new BorderLayout(0, 10));
        breakdownCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        breakdownCard.setAccentColor(UITheme.ACCENT_TEAL);
        JLabel bl = new JLabel("Order Breakdown by Status");
        bl.setFont(UITheme.FONT_HEADING);
        bl.setForeground(UITheme.TEXT_PRIMARY);
        breakdownCard.add(bl, BorderLayout.NORTH);

        String[] cols = {"Status", "Count", "Total Value (₹)", "Percentage"};
        int total = allOrders.size();
        Object[][] data = new Object[OrderStatus.values().length][4];
        for (int i = 0; i < OrderStatus.values().length; i++) {
            OrderStatus s  = OrderStatus.values()[i];
            long   count   = allOrders.stream().filter(o -> o.getStatus() == s).count();
            double val     = allOrders.stream().filter(o -> o.getStatus() == s)
                    .mapToDouble(Order::getTotalAmount).sum();
            double pct     = total == 0 ? 0 : (count * 100.0 / total);
            data[i] = new Object[]{ s.getDisplayName(), count, String.format("%.2f", val),
                    String.format("%.1f%%", pct) };
        }
        breakdownCard.add(StyledTable.create(cols, data), BorderLayout.CENTER);

        // Category revenue
        CardPanel catCard = new CardPanel(new BorderLayout(0, 10));
        catCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        catCard.setAccentColor(UITheme.ACCENT_PURPLE);
        JLabel cl = new JLabel("Product Catalogue Overview");
        cl.setFont(UITheme.FONT_HEADING);
        cl.setForeground(UITheme.TEXT_PRIMARY);
        catCard.add(cl, BorderLayout.NORTH);

        String[] pCols = {"Product", "Category", "Price (₹)", "Stock", "Est. Inventory Value (₹)"};
        Object[][] pData = productService.getAllProducts().stream()
                .map(pr -> new Object[]{
                        pr.getName(), pr.getCategory(),
                        String.format("%.2f", pr.getPrice()), pr.getStockQuantity(),
                        String.format("%.2f", pr.getPrice() * pr.getStockQuantity())
                }).toArray(Object[][]::new);
        catCard.add(StyledTable.create(pCols, pData), BorderLayout.CENTER);

        JPanel tables = new JPanel(new GridLayout(1, 2, 16, 0));
        tables.setOpaque(false);
        tables.add(breakdownCard);
        tables.add(catCard);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(metrics, BorderLayout.NORTH);
        center.add(tables,  BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

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
                g2.setPaint(new GradientPaint(0,0, UITheme.ACCENT_PURPLE, getWidth(),0, new Color(0,0,0,0)));
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
        cb.setPreferredSize(new Dimension(200, 36));
    }
}
