package com.quickcommerce.gui;

import com.quickcommerce.controller.UserController;
import com.quickcommerce.gui.components.*;
import com.quickcommerce.model.*;
import java.awt.*;
import javax.swing.*;

/**
 * Full-screen login window with animated gradient background,
 * role selector tabs, and glowing input fields.
 */
public class LoginFrame extends JFrame {

    private final UserController userController;

    private StyledTextField   emailField;
    private StyledPasswordField passField;
    private JComboBox<String> roleCombo;
    private JLabel            statusLabel;

    // Pre-built demo accounts for each role
    private static final String[][] DEMO = {
        { "Customer",         "priya@gmail.com",         "priya123"  },
        { "Vendor",           "ravi@freshmart.com",      "vendor123" },
        { "Delivery Partner", "arjun@delivery.com",      "arjun123"  },
        { "Administrator",    "alice@quickcommerce.com", "admin123"  },
    };

    public LoginFrame(UserController userController) {
        this.userController = userController;
        initUI();
    }

    private void initUI() {
        setTitle("Quick Commerce — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        // Custom background panel
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                // Deep gradient background
                GradientPaint bg = new GradientPaint(0, 0, UITheme.BG_DEEP, w, h,
                        new Color(0x0F1B26));
                g2.setPaint(bg);
                g2.fillRect(0, 0, w, h);
                // Decorative circle blobs
                g2.setColor(new Color(46, 204, 113, 18));
                g2.fillOval(-80, -80, 340, 340);
                g2.setColor(new Color(26, 188, 156, 12));
                g2.fillOval(w - 200, h - 200, 360, 360);
                g2.setColor(new Color(52, 152, 219, 10));
                g2.fillOval(w / 2 - 100, -60, 250, 200);
                g2.dispose();
            }
        };
        root.setOpaque(true);
        setContentPane(root);

        root.add(buildHeroPanel(),  BorderLayout.WEST);
        root.add(buildFormPanel(),  BorderLayout.CENTER);
    }

    // ---- Left hero side ------------------------------------------------

    private JPanel buildHeroPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x1A3526),
                        getWidth(), getHeight(), new Color(0x0D2218));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(380, 620));
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 40, 8, 40);

        // Logo
        JLabel logo = new JLabel("🛒");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0; gbc.insets = new Insets(60, 40, 0, 40);
        panel.add(logo, gbc);

        // App name
        JLabel appName = new JLabel("QuickCommerce");
        appName.setFont(new Font("SansSerif", Font.BOLD, 24));
        appName.setForeground(UITheme.ACCENT_GREEN);
        appName.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1; gbc.insets = new Insets(10, 40, 4, 40);
        panel.add(appName, gbc);

        JLabel tagLine = new JLabel("Grocery at your doorstep");
        tagLine.setFont(UITheme.FONT_BODY);
        tagLine.setForeground(UITheme.TEXT_SECONDARY);
        tagLine.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2; gbc.insets = new Insets(0, 40, 40, 40);
        panel.add(tagLine, gbc);

        // Feature bullets
        String[] features = { "⚡  10-minute delivery", "🥦  Fresh & organic", "💳  Secure payments", "📦  Real-time tracking" };
        for (int i = 0; i < features.length; i++) {
            JLabel lbl = new JLabel(features[i]);
            lbl.setFont(UITheme.FONT_BODY);
            lbl.setForeground(UITheme.TEXT_SECONDARY);
            gbc.gridy = 3 + i;
            gbc.insets = new Insets(6, 50, 6, 40);
            panel.add(lbl, gbc);
        }

        return panel;
    }

    // ---- Right form side -----------------------------------------------

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        CardPanel card = new CardPanel(new GridBagLayout());
        card.setElevated(true);
        card.setPreferredSize(new Dimension(380, 460));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 30, 8, 30);

        // Title
        JLabel title = new JLabel("Welcome back");
        title.setFont(UITheme.FONT_DISPLAY);
        title.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridy = 0; gbc.insets = new Insets(30, 30, 4, 30);
        card.add(title, gbc);

        JLabel sub = new JLabel("Sign in to continue");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridy = 1; gbc.insets = new Insets(0, 30, 20, 30);
        card.add(sub, gbc);

        // Role selector
        gbc.gridy = 2; gbc.insets = new Insets(4, 30, 4, 30);
        card.add(makeLabel("Login As"), gbc);

        roleCombo = new JComboBox<>(new String[]{"Customer", "Vendor", "Delivery Partner", "Administrator"});
        styleCombo(roleCombo);
        roleCombo.addActionListener(e -> prefillDemo());
        gbc.gridy = 3;
        card.add(roleCombo, gbc);

        // Email
        gbc.gridy = 4; gbc.insets = new Insets(10, 30, 4, 30);
        card.add(makeLabel("Email"), gbc);
        emailField = new StyledTextField("Enter your email");
        gbc.gridy = 5; gbc.insets = new Insets(0, 30, 0, 30);
        card.add(emailField, gbc);

        // Password
        gbc.gridy = 6; gbc.insets = new Insets(10, 30, 4, 30);
        card.add(makeLabel("Password"), gbc);
        passField = new StyledPasswordField();
        gbc.gridy = 7; gbc.insets = new Insets(0, 30, 0, 30);
        card.add(passField, gbc);

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.ACCENT_RED);
        gbc.gridy = 8; gbc.insets = new Insets(4, 30, 0, 30);
        card.add(statusLabel, gbc);

        // Login button
        StyledButton loginBtn = new StyledButton("Sign In", StyledButton.Style.PRIMARY);
        loginBtn.setPreferredSize(new Dimension(300, 44));
        loginBtn.addActionListener(e -> attemptLogin());
        gbc.gridy = 9; gbc.insets = new Insets(10, 30, 30, 30);
        card.add(loginBtn, gbc);

        // Enter key triggers login
        passField.addActionListener(e -> attemptLogin());
        emailField.addActionListener(e -> passField.requestFocusInWindow());

        prefillDemo();
        outer.add(card);
        return outer;
    }

    private void prefillDemo() {
        int idx = roleCombo.getSelectedIndex();
        if (idx >= 0 && idx < DEMO.length) {
            emailField.setText(DEMO[idx][1]);
            passField.setText(DEMO[idx][2]);
        }
    }

    private void attemptLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passField.getPassword());
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }
        try {
            User user = userController.login(email, password);
            statusLabel.setForeground(UITheme.ACCENT_GREEN);
            statusLabel.setText("✓ Login successful!");
            SwingUtilities.invokeLater(() -> {
                new MainFrame(user, userController).setVisible(true);
                dispose();
            });
        } catch (Exception ex) {
            statusLabel.setForeground(UITheme.ACCENT_RED);
            statusLabel.setText("✗ " + ex.getMessage());
        }
    }

    // ---- Helpers -------------------------------------------------------

    private static JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_LABEL);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        return lbl;
    }

    private static void styleCombo(JComboBox<String> cb) {
        cb.setBackground(UITheme.BG_INPUT);
        cb.setForeground(UITheme.TEXT_PRIMARY);
        cb.setFont(UITheme.FONT_BODY);
        cb.setPreferredSize(new Dimension(300, 42));
        cb.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_NORMAL, 1));
        ((JLabel) cb.getRenderer()).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
    }
}
