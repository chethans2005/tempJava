package com.quickcommerce.gui;

import com.quickcommerce.controller.OrderController;
import com.quickcommerce.controller.ProductController;
import com.quickcommerce.controller.UserController;
import com.quickcommerce.gui.components.*;
import com.quickcommerce.gui.panels.*;
import com.quickcommerce.model.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

/**
 * Main application window.
 *
 * Layout:
 *   ┌─────────────────────────────────────────────────┐
 *   │  TOP BAR  (app name + user badge + logout)       │
 *   ├───────────┬─────────────────────────────────────┤
 *   │  SIDEBAR  │  CONTENT (CardLayout panels)         │
 *   │  (nav     │                                      │
 *   │   items)  │                                      │
 *   └───────────┴─────────────────────────────────────┘
 */
public class MainFrame extends JFrame {

    private final User        loggedInUser;
    private final UserController userController;

    // Controllers (created once, passed into panels)
    private final ProductController productController;
    private final OrderController   orderController;

    private JPanel      contentArea;
    private CardLayout  cardLayout;
    private JButton     activeNavBtn = null;

    public MainFrame(User loggedInUser, UserController userController) {
        this.loggedInUser   = loggedInUser;
        this.userController = userController;
        this.productController = AppContext.getProductController();
        this.orderController   = AppContext.getOrderController();

        initUI();
        setTitle("Quick Commerce — " + loggedInUser.getRole() + " Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    // -----------------------------------------------------------------------
    // UI Construction
    // -----------------------------------------------------------------------

    private void initUI() {
        // Root background
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.BG_DEEP);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(true);
        setContentPane(root);

        root.add(buildTopBar(),  BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);
    }

    // ---- Top Bar -------------------------------------------------------

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.BG_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Bottom separator
                g2.setColor(UITheme.BORDER_SUBTLE);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(0, 56));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Left — logo
        JLabel logoLabel = new JLabel("🛒  QuickCommerce");
        logoLabel.setFont(UITheme.FONT_TITLE);
        logoLabel.setForeground(UITheme.ACCENT_GREEN);
        bar.add(logoLabel, BorderLayout.WEST);

        // Right — user badge + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        String roleEmoji = switch (loggedInUser.getRole()) {
            case "Customer"         -> "🛍️";
            case "Vendor"           -> "🏪";
            case "Delivery Partner" -> "🏍️";
            case "Administrator"    -> "🛡️";
            default                 -> "👤";
        };
        JLabel userBadge = new JLabel(roleEmoji + "  " + loggedInUser.getName()
                + "  •  " + loggedInUser.getRole());
        userBadge.setFont(UITheme.FONT_BODY);
        userBadge.setForeground(UITheme.TEXT_SECONDARY);

        StyledButton logoutBtn = new StyledButton("Logout", StyledButton.Style.GHOST);
        logoutBtn.addActionListener(e -> {
            loggedInUser.logout();
            new LoginFrame(userController).setVisible(true);
            dispose();
        });

        right.add(userBadge);
        right.add(logoutBtn);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ---- Sidebar -------------------------------------------------------

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.BG_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BORDER_SUBTLE);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(UITheme.SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        // Build nav items based on role
        switch (loggedInUser.getRole()) {
            case "Customer"         -> buildCustomerNav(sidebar);
            case "Vendor"           -> buildVendorNav(sidebar);
            case "Delivery Partner" -> buildDeliveryNav(sidebar);
            case "Administrator"    -> buildAdminNav(sidebar);
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void buildCustomerNav(JPanel s) {
        addNavSection(s, "SHOPPING");
        addNavItem(s, "🏠  Dashboard",   "home");
        addNavItem(s, "🔍  Browse",       "browse");
        addNavItem(s, "🛒  My Cart",      "cart");
        addNavSection(s, "ORDERS");
        addNavItem(s, "📦  My Orders",    "orders");
    }

    private void buildVendorNav(JPanel s) {
        addNavSection(s, "STORE");
        addNavItem(s, "🏠  Dashboard",   "home");
        addNavItem(s, "📋  Products",    "products");
        addNavItem(s, "➕  Add Product", "addproduct");
        addNavSection(s, "ORDERS");
        addNavItem(s, "📦  Orders",      "orders");
    }

    private void buildDeliveryNav(JPanel s) {
        addNavSection(s, "DELIVERIES");
        addNavItem(s, "🏠  Dashboard",    "home");
        addNavItem(s, "📋  My Orders",    "orders");
    }

    private void buildAdminNav(JPanel s) {
        addNavSection(s, "OVERVIEW");
        addNavItem(s, "🏠  Dashboard",   "home");
        addNavSection(s, "MANAGEMENT");
        addNavItem(s, "👥  Users",       "users");
        addNavItem(s, "📦  All Orders",  "orders");
        addNavItem(s, "📊  Reports",     "reports");
    }

    private void addNavSection(JPanel s, String label) {
        JLabel lbl = new JLabel("  " + label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(14, 18, 6, 0));
        s.add(lbl);
    }

    private void addNavItem(JPanel s, String label, String cardName) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (this == activeNavBtn) {
                    // Active pill
                    g2.setColor(new Color(46, 204, 113, 28));
                    g2.fill(new RoundRectangle2D.Float(8, 2, getWidth() - 16, getHeight() - 4, 8, 8));
                    // Left accent bar
                    g2.setColor(UITheme.ACCENT_GREEN);
                    g2.fillRoundRect(0, 8, 3, getHeight() - 16, 2, 2);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 10));
                    g2.fill(new RoundRectangle2D.Float(8, 2, getWidth() - 16, getHeight() - 4, 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(UITheme.TEXT_SECONDARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(UITheme.SIDEBAR_WIDTH, 40));
        btn.setPreferredSize(new Dimension(UITheme.SIDEBAR_WIDTH, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (activeNavBtn != null) {
                activeNavBtn.setForeground(UITheme.TEXT_SECONDARY);
            }
            activeNavBtn = btn;
            btn.setForeground(UITheme.ACCENT_GREEN);
            cardLayout.show(contentArea, cardName);
            s.repaint();
        });

        // First nav item auto-selected
        if (activeNavBtn == null) {
            activeNavBtn = btn;
            btn.setForeground(UITheme.ACCENT_GREEN);
        }

        s.add(btn);
    }

    // ---- Content Area --------------------------------------------------

    private JPanel buildContent() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UITheme.BG_DEEP);

        switch (loggedInUser.getRole()) {
            case "Customer" -> {
                CustomerPanel cp = new CustomerPanel((Customer) loggedInUser, productController, orderController);
                contentArea.add(cp.buildHome(),    "home");
                contentArea.add(cp.buildBrowse(),  "browse");
                contentArea.add(cp.buildCart(),    "cart");
                contentArea.add(cp.buildOrders(),  "orders");
            }
            case "Vendor" -> {
                VendorPanel vp = new VendorPanel((Vendor) loggedInUser, productController, orderController);
                contentArea.add(vp.buildHome(),       "home");
                contentArea.add(vp.buildProducts(),   "products");
                contentArea.add(vp.buildAddProduct(), "addproduct");
                contentArea.add(vp.buildOrders(),     "orders");
            }
            case "Delivery Partner" -> {
                DeliveryPartnerPanel dp = new DeliveryPartnerPanel(
                        (DeliveryPartner) loggedInUser, orderController);
                contentArea.add(dp.buildHome(),   "home");
                contentArea.add(dp.buildOrders(), "orders");
            }
            case "Administrator" -> {
                AdminPanel ap = new AdminPanel((Administrator) loggedInUser,
                        userController, productController, orderController);
                contentArea.add(ap.buildHome(),    "home");
                contentArea.add(ap.buildUsers(),   "users");
                contentArea.add(ap.buildOrders(),  "orders");
                contentArea.add(ap.buildReports(), "reports");
            }
        }

        cardLayout.show(contentArea, "home");
        return contentArea;
    }
}
