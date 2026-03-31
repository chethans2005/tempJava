package com.quickcommerce.gui.panels;

import com.quickcommerce.controller.OrderController;
import com.quickcommerce.controller.ProductController;
import com.quickcommerce.enums.PaymentMethod;
import com.quickcommerce.gui.UITheme;
import com.quickcommerce.gui.components.*;
import com.quickcommerce.model.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * All content panels rendered in the Customer role's MainFrame.
 */
public class CustomerPanel {

    private final Customer       customer;
    private final ProductController productService;
    private final OrderController   orderService;

    // Cart table model — updated live
    private DefaultTableModel cartTableModel;
    private JLabel            cartTotalLabel;

    public CustomerPanel(Customer customer, ProductController ps, OrderController os) {
        this.customer       = customer;
        this.productService = ps;
        this.orderService   = os;
    }

    // ===================================================================
    // HOME
    // ===================================================================
    public JPanel buildHome() {
        JPanel p = contentPane();

        p.add(pageTitle("🏠  Welcome, " + customer.getName()), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        long totalOrders    = customer.getOrderHistory().size();
        long delivered      = customer.getOrderHistory().stream()
                .filter(o -> o.getStatus().name().equals("DELIVERED")).count();
        double totalSpend   = customer.getOrderHistory().stream()
                .mapToDouble(Order::getTotalAmount).sum();
        int cartCount       = customer.getCart().getCartItems().size();

        grid.add(new StatCard("Total Orders",    String.valueOf(totalOrders), "📦", UITheme.ACCENT_BLUE));
        grid.add(new StatCard("Delivered",        String.valueOf(delivered),   "✅", UITheme.ACCENT_GREEN));
        grid.add(new StatCard("Total Spend",     "₹" + String.format("%.0f", totalSpend), "💰", UITheme.ACCENT_AMBER));
        grid.add(new StatCard("Cart Items",       String.valueOf(cartCount),   "🛒", UITheme.ACCENT_PURPLE));

        // Recent orders mini-table
        CardPanel recentCard = new CardPanel(new BorderLayout(0, 10));
        recentCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        recentCard.setAccentColor(UITheme.ACCENT_BLUE);
        JLabel recentTitle = new JLabel("Recent Orders");
        recentTitle.setFont(UITheme.FONT_HEADING);
        recentTitle.setForeground(UITheme.TEXT_PRIMARY);
        recentCard.add(recentTitle, BorderLayout.NORTH);

        String[] cols = {"Order ID", "Date", "Items", "Total", "Status"};
        List<Order> history = customer.getOrderHistory();
        Object[][] data = history.stream()
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .limit(5)
                .map(o -> new Object[]{
                        o.getOrderId(),
                        o.getOrderDate().toLocalDate(),
                        o.getOrderItems().size(),
                        "₹" + String.format("%.2f", o.getTotalAmount()),
                        o.getStatus().getDisplayName()
                }).toArray(Object[][]::new);
        recentCard.add(StyledTable.create(cols, data), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(grid,       BorderLayout.NORTH);
        center.add(recentCard, BorderLayout.CENTER);
        p.add(scrollWrap(center), BorderLayout.CENTER);
        return p;
    }

    // ===================================================================
    // BROWSE PRODUCTS
    // ===================================================================
    public JPanel buildBrowse() {
        JPanel p = contentPane();
        p.add(pageTitle("🔍  Browse Products"), BorderLayout.NORTH);

        String[] cols = {"Product ID", "Name", "Category", "Price (₹)", "Stock"};
        List<Product> all = productService.getAllProducts();
        Object[][] data = all.stream().map(pr -> new Object[]{
                pr.getProductId(), pr.getName(), pr.getCategory(),
                String.format("%.2f", pr.getPrice()), pr.getStockQuantity()
        }).toArray(Object[][]::new);

        JScrollPane table = StyledTable.create(cols, data);
        // Re-fetch the JTable to make rows selectable
        JTable jt = (JTable) ((JViewport) table.getComponent(0)).getView();

        CardPanel controlCard = new CardPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        controlCard.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        qtySpinner.setPreferredSize(new Dimension(70, 36));
        styleSpinner(qtySpinner);

        StyledButton addBtn = new StyledButton("Add to Cart ➕", StyledButton.Style.PRIMARY);
        addBtn.addActionListener(e -> {
            int row = jt.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(p, "Please select a product first."); return; }
            Product pr = all.get(row);
            int qty = (int) qtySpinner.getValue();
            try {
                customer.addToCart(pr, qty);
                JOptionPane.showMessageDialog(p,
                        qty + "x '" + pr.getName() + "' added to cart! ✓",
                        "Cart Updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(p, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JLabel qtyLbl = new JLabel("Quantity:");
        qtyLbl.setFont(UITheme.FONT_LABEL);
        qtyLbl.setForeground(UITheme.TEXT_SECONDARY);
        controlCard.add(qtyLbl);
        controlCard.add(qtySpinner);
        controlCard.add(addBtn);

        p.add(controlCard, BorderLayout.SOUTH);
        p.add(table,       BorderLayout.CENTER);
        return p;
    }

    // ===================================================================
    // CART
    // ===================================================================
    public JPanel buildCart() {
        JPanel p = contentPane();
        p.add(pageTitle("🛒  My Cart"), BorderLayout.NORTH);

        cartTableModel = new DefaultTableModel(
                new String[]{"Product", "Category", "Unit Price", "Qty", "Line Total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshCartTable();

        JScrollPane tableScroll = new JScrollPane();
        JTable table = new JTable(cartTableModel);
        StyledTable.styleTable(table);
        tableScroll.setViewportView(table);
        StyledTable.styleScrollPane(tableScroll);

        // Bottom bar
        CardPanel bottomBar = new CardPanel(new BorderLayout(16, 0));
        bottomBar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        bottomBar.setAccentColor(UITheme.ACCENT_GREEN);

        cartTotalLabel = new JLabel("Total: ₹0.00");
        cartTotalLabel.setFont(UITheme.FONT_TITLE);
        cartTotalLabel.setForeground(UITheme.ACCENT_GREEN);
        updateCartTotal();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        StyledButton removeBtn = new StyledButton("Remove Selected", StyledButton.Style.DANGER);
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            String productId = customer.getCart().getCartItems().get(row).getProduct().getProductId();
            customer.getCart().removeItem(productId);
            refreshCartTable();
            updateCartTotal();
        });

        String[] methods = {"UPI", "Credit Card", "Debit Card", "Cash on Delivery"};
        JComboBox<String> payCombo = new JComboBox<>(methods);
        styleSmallCombo(payCombo);

        StyledButton checkoutBtn = new StyledButton("Checkout →", StyledButton.Style.PRIMARY);
        checkoutBtn.addActionListener(e -> {
            if (customer.getCart().getCartItems().isEmpty()) {
                JOptionPane.showMessageDialog(p, "Your cart is empty!");
                return;
            }
            PaymentMethod pm = switch (payCombo.getSelectedIndex()) {
                case 1 -> PaymentMethod.CREDIT_CARD;
                case 2 -> PaymentMethod.DEBIT_CARD;
                case 3 -> PaymentMethod.CASH_ON_DELIVERY;
                default -> PaymentMethod.UPI;
            };
                try {
                Order order = customer.placeOrder();
                order.checkout(pm);
                orderService.registerOrder(order);
                refreshCartTable();
                updateCartTotal();
                JOptionPane.showMessageDialog(p,
                        "Order placed! 🎉\nOrder ID: " + order.getOrderId()
                        + "\nPayment: " + pm.getDisplayName()
                        + "\nTotal: ₹" + String.format("%.2f", order.getTotalAmount()),
                        "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(p, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(removeBtn);
        btnPanel.add(new JLabel("Pay via:") {{ setFont(UITheme.FONT_LABEL); setForeground(UITheme.TEXT_SECONDARY); }});
        btnPanel.add(payCombo);
        btnPanel.add(checkoutBtn);

        bottomBar.add(cartTotalLabel, BorderLayout.WEST);
        bottomBar.add(btnPanel,       BorderLayout.EAST);

        p.add(tableScroll, BorderLayout.CENTER);
        p.add(bottomBar,   BorderLayout.SOUTH);
        return p;
    }

    private void refreshCartTable() {
        if (cartTableModel == null) return;
        cartTableModel.setRowCount(0);
        customer.getCart().getCartItems().forEach(ci -> cartTableModel.addRow(new Object[]{
                ci.getProduct().getName(),
                ci.getProduct().getCategory(),
                "₹" + String.format("%.2f", ci.getPriceAtAddition()),
                ci.getQuantity(),
                "₹" + String.format("%.2f", ci.getTotal())
        }));
    }

    private void updateCartTotal() {
        if (cartTotalLabel != null)
            cartTotalLabel.setText("Total: ₹" + String.format("%.2f", customer.getCart().calculateTotal()));
    }

    // ===================================================================
    // ORDERS
    // ===================================================================
    public JPanel buildOrders() {
        JPanel p = contentPane();
        p.add(pageTitle("📦  My Orders"), BorderLayout.NORTH);

        String[] cols = {"Order ID", "Date", "Items", "Total", "Payment", "Status"};
        Object[][] data = customer.getOrderHistory().stream()
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .map(o -> new Object[]{
                        o.getOrderId(),
                        o.getOrderDate().toLocalDate().toString(),
                        o.getOrderItems().size(),
                        "₹" + String.format("%.2f", o.getTotalAmount()),
                        o.getPayment() != null ? o.getPayment().getPaymentMethod().getDisplayName() : "—",
                        o.getStatus().getDisplayName()
                }).toArray(Object[][]::new);

        p.add(StyledTable.create(cols, data), BorderLayout.CENTER);
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
        // Accent line underneath
        JSeparator sep = new JSeparator() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, UITheme.ACCENT_GREEN, getWidth(), 0, new Color(0, 0, 0, 0)));
                g2.fillRect(0, 0, getWidth(), 2);
                g2.dispose();
            }
        };
        sep.setPreferredSize(new Dimension(0, 2));
        row.add(sep, BorderLayout.SOUTH);
        return row;
    }

    private JPanel scrollWrap(JPanel inner) {
        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.BG_DEEP);
        scroll.setOpaque(false);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scroll);
        return wrapper;
    }

    private void styleSpinner(JSpinner s) {
        s.setBackground(UITheme.BG_INPUT);
        s.setForeground(UITheme.TEXT_PRIMARY);
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setBackground(UITheme.BG_INPUT);
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setForeground(UITheme.TEXT_PRIMARY);
    }

    private void styleSmallCombo(JComboBox<String> cb) {
        cb.setBackground(UITheme.BG_INPUT);
        cb.setForeground(UITheme.TEXT_PRIMARY);
        cb.setFont(UITheme.FONT_BODY);
        cb.setPreferredSize(new Dimension(160, 36));
    }
}
