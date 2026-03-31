package com.quickcommerce.gui.panels;

import com.quickcommerce.controller.OrderController;
import com.quickcommerce.controller.ProductController;
import com.quickcommerce.gui.UITheme;
import com.quickcommerce.gui.components.*;
import com.quickcommerce.model.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** All content panels rendered in the Vendor role's MainFrame. */
public class VendorPanel {

    private final Vendor         vendor;
    private final ProductController productService;
    private final OrderController   orderService;

    private DefaultTableModel productTableModel;

    public VendorPanel(Vendor vendor, ProductController ps, OrderController os) {
        this.vendor         = vendor;
        this.productService = ps;
        this.orderService   = os;
    }

    // ===================================================================
    // HOME
    // ===================================================================
    public JPanel buildHome() {
        JPanel p = contentPane();
        p.add(pageTitle("🏪  " + vendor.getStoreName() + " — Dashboard"), BorderLayout.NORTH);

        List<Product> catalogue = vendor.getProductCatalogue();
        long inStock      = catalogue.stream().filter(Product::isInStock).count();
        long lowStock     = catalogue.stream().filter(pr -> pr.getStockQuantity() < 10).count();
        long pendingOrders= orderService.getAllOrders().stream()
                .filter(o -> o.getStatus().name().equals("PENDING")).count();
        long confirmedOrders = orderService.getAllOrders().stream()
                .filter(o -> o.getStatus().name().equals("CONFIRMED")).count();

        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);
        grid.add(new StatCard("Products",      String.valueOf(catalogue.size()), "📋", UITheme.ACCENT_BLUE));
        grid.add(new StatCard("In Stock",      String.valueOf(inStock),          "✅", UITheme.ACCENT_GREEN));
        grid.add(new StatCard("Low Stock",     String.valueOf(lowStock),         "⚠️", UITheme.ACCENT_AMBER));
        grid.add(new StatCard("Pending Orders",String.valueOf(pendingOrders),    "⏳", UITheme.ACCENT_RED));

        // Recent products table
        CardPanel tableCard = new CardPanel(new BorderLayout(0, 10));
        tableCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        tableCard.setAccentColor(UITheme.ACCENT_TEAL);
        JLabel tbl = new JLabel("Your Product Catalogue");
        tbl.setFont(UITheme.FONT_HEADING);
        tbl.setForeground(UITheme.TEXT_PRIMARY);
        tableCard.add(tbl, BorderLayout.NORTH);
        tableCard.add(buildProductTableScroll(), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(grid,      BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    // ===================================================================
    // PRODUCTS
    // ===================================================================
    public JPanel buildProducts() {
        JPanel p = contentPane();
        p.add(pageTitle("📋  Products"), BorderLayout.NORTH);

        productTableModel = new DefaultTableModel(
                new String[]{"Product ID", "Name", "Category", "Price (₹)", "Stock", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshProductTable();

        JScrollPane tableScroll = new JScrollPane();
        JTable jt = new JTable(productTableModel);
        StyledTable.styleTable(jt);
        tableScroll.setViewportView(jt);
        StyledTable.styleScrollPane(tableScroll);

        // Control bar
        CardPanel ctrl = new CardPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        ctrl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        StyledTextField priceField = new StyledTextField("New Price (₹)");
        priceField.setPreferredSize(new Dimension(140, 36));
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        styleSpinner(stockSpinner);
        stockSpinner.setPreferredSize(new Dimension(80, 36));

        StyledButton updateBtn = new StyledButton("Update Selected", StyledButton.Style.SECONDARY);
        updateBtn.addActionListener(e -> {
            int row = jt.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(p, "Select a product to update."); return; }
            String pid = vendor.getProductCatalogue().get(row).getProductId();
            try {
                double newPrice = Double.parseDouble(priceField.getText().trim());
                int newStock    = (int) stockSpinner.getValue();
                vendor.updateProduct(pid, newPrice, newStock);
                refreshProductTable();
                JOptionPane.showMessageDialog(p, "Product updated successfully ✓");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(p, "Please enter a valid price.", "Input Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(p, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        StyledButton removeBtn = new StyledButton("Remove", StyledButton.Style.DANGER);
        removeBtn.addActionListener(e -> {
            int row = jt.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(p, "Select a product first."); return; }
            int confirm = JOptionPane.showConfirmDialog(p, "Remove this product?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String pid = vendor.getProductCatalogue().get(row).getProductId();
                vendor.removeProduct(pid);
                        productService.removeProduct(pid);
                refreshProductTable();
            }
        });

        JLabel priceLbl = label("New Price:");
        JLabel stockLbl = label("New Stock:");
        ctrl.add(priceLbl); ctrl.add(priceField);
        ctrl.add(stockLbl); ctrl.add(stockSpinner);
        ctrl.add(updateBtn);
        ctrl.add(removeBtn);

        p.add(tableScroll, BorderLayout.CENTER);
        p.add(ctrl,        BorderLayout.SOUTH);
        return p;
    }

    // ===================================================================
    // ADD PRODUCT
    // ===================================================================
    public JPanel buildAddProduct() {
        JPanel p = contentPane();
        p.add(pageTitle("➕  Add New Product"), BorderLayout.NORTH);

        CardPanel form = new CardPanel(new GridBagLayout());
        form.setElevated(true);
        form.setAccentColor(UITheme.ACCENT_GREEN);
        form.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 8, 8, 8);
        g.gridx = 0; g.weightx = 0.3;

        StyledTextField nameField     = new StyledTextField("e.g. Organic Honey 500g");
        StyledTextField categoryField = new StyledTextField("e.g. Dairy, Bakery, Fruits…");
        StyledTextField priceField    = new StyledTextField("e.g. 149.00");
        StyledTextField stockField    = new StyledTextField("e.g. 100");

        addFormRow(form, g, 0, "Product Name *",  nameField);
        addFormRow(form, g, 1, "Category *",       categoryField);
        addFormRow(form, g, 2, "Price (₹) *",      priceField);
        addFormRow(form, g, 3, "Initial Stock *",  stockField);

        JLabel statusLbl = new JLabel(" ");
        statusLbl.setFont(UITheme.FONT_SMALL);
        g.gridy = 4; g.gridx = 0; g.gridwidth = 2; g.insets = new Insets(4, 8, 4, 8);
        form.add(statusLbl, g);

        StyledButton saveBtn = new StyledButton("Add Product ✓", StyledButton.Style.PRIMARY);
        saveBtn.setPreferredSize(new Dimension(200, 44));
        g.gridy = 5; g.gridwidth = 2;
        form.add(saveBtn, g);

        saveBtn.addActionListener(e -> {
            String name     = nameField.getText().trim();
            String category = categoryField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();

            if (name.isEmpty() || category.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                statusLbl.setForeground(UITheme.ACCENT_RED);
                statusLbl.setText("✗ All fields are required.");
                return;
            }
            try {
                double price = Double.parseDouble(priceStr);
                int    stock = Integer.parseInt(stockStr);
                Product newProduct = new Product(name, category, price, stock);
                vendor.addProduct(newProduct);
                productService.addProduct(newProduct);
                statusLbl.setForeground(UITheme.ACCENT_GREEN);
                statusLbl.setText("✓ Product '" + name + "' added successfully!");
                nameField.setText(""); categoryField.setText("");
                priceField.setText(""); stockField.setText("");
            } catch (NumberFormatException ex) {
                statusLbl.setForeground(UITheme.ACCENT_RED);
                statusLbl.setText("✗ Price and Stock must be numbers.");
            } catch (Exception ex) {
                statusLbl.setForeground(UITheme.ACCENT_RED);
                statusLbl.setText("✗ " + ex.getMessage());
            }
        });

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(form);
        p.add(wrapper, BorderLayout.CENTER);
        return p;
    }

    // ===================================================================
    // ORDERS
    // ===================================================================
    public JPanel buildOrders() {
        JPanel p = contentPane();
        p.add(pageTitle("📦  Orders"), BorderLayout.NORTH);

        String[] cols = {"Order ID", "Customer", "Date", "Total (₹)", "Status", "Action"};
        List<Order> all = orderService.getAllOrders();
        Object[][] data = all.stream()
                .map(o -> new Object[]{
                        o.getOrderId(),
                        o.getCustomer().getName(),
                        o.getOrderDate().toLocalDate(),
                        String.format("%.2f", o.getTotalAmount()),
                        o.getStatus().getDisplayName(),
                        "Confirm"
                }).toArray(Object[][]::new);

        JScrollPane tableScroll = StyledTable.create(cols, data);
        JTable jt = (JTable) ((JViewport) tableScroll.getComponent(0)).getView();

        StyledButton confirmBtn = new StyledButton("Confirm Selected Order", StyledButton.Style.PRIMARY);
        confirmBtn.addActionListener(e -> {
            int row = jt.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(p, "Select an order to confirm."); return; }
            Order order = all.get(row);
            vendor.processOrder(order);
            ((javax.swing.table.DefaultTableModel) jt.getModel())
                    .setValueAt(order.getStatus().getDisplayName(), row, 4);
            JOptionPane.showMessageDialog(p, "Order confirmed ✓");
        });

        CardPanel ctrl = new CardPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        ctrl.add(confirmBtn);

        p.add(tableScroll, BorderLayout.CENTER);
        p.add(ctrl,        BorderLayout.SOUTH);
        return p;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private JScrollPane buildProductTableScroll() {
        String[] cols = {"Product ID", "Name", "Category", "Price (₹)", "Stock", "Status"};
        List<Product> catalogue = vendor.getProductCatalogue();
        Object[][] data = catalogue.stream().map(pr -> new Object[]{
                pr.getProductId(), pr.getName(), pr.getCategory(),
                String.format("%.2f", pr.getPrice()), pr.getStockQuantity(),
                pr.isInStock() ? "In Stock" : "Out of Stock"
        }).toArray(Object[][]::new);
        return StyledTable.create(cols, data);
    }

    private void refreshProductTable() {
        if (productTableModel == null) return;
        productTableModel.setRowCount(0);
        vendor.getProductCatalogue().forEach(pr -> productTableModel.addRow(new Object[]{
                pr.getProductId(), pr.getName(), pr.getCategory(),
                String.format("%.2f", pr.getPrice()), pr.getStockQuantity(),
                pr.isInStock() ? "In Stock" : "Out of Stock"
        }));
    }

    private void addFormRow(JPanel form, GridBagConstraints g, int row, String labelText, JComponent field) {
        g.gridy = row; g.gridx = 0; g.gridwidth = 1; g.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_LABEL);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        form.add(lbl, g);
        g.gridx = 1; g.weightx = 0.7;
        field.setPreferredSize(new Dimension(280, 42));
        form.add(field, g);
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
                g2.setPaint(new GradientPaint(0,0, UITheme.ACCENT_TEAL, getWidth(),0, new Color(0,0,0,0)));
                g2.fillRect(0, 0, getWidth(), 2);
                g2.dispose();
            }
        };
        sep.setPreferredSize(new Dimension(0, 2));
        row.add(sep, BorderLayout.SOUTH);
        return row;
    }

    private static JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UITheme.FONT_LABEL);
        l.setForeground(UITheme.TEXT_SECONDARY);
        return l;
    }

    private void styleSpinner(JSpinner s) {
        s.setBackground(UITheme.BG_INPUT);
        s.setForeground(UITheme.TEXT_PRIMARY);
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setBackground(UITheme.BG_INPUT);
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setForeground(UITheme.TEXT_PRIMARY);
    }
}
