package com.airline.fleet.ui;

import com.airline.fleet.DBConnection;
import com.airline.fleet.FleetDAO;
import com.airline.fleet.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * LoginFrame: Modern Airline Authentication Gateway.
 * Navy Blue (#0A192F), White, and Light Blue palette.
 */
public class LoginFrame extends JFrame {

    private final FleetDAO fleetDAO;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        this.fleetDAO = new FleetDAO();
        setTitle("Commercial Airline Fleet Analytics Engine - Secure Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Theme.BG_LIGHT);

        initUI();
    }

    private void initUI() {
        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.setOpaque(false);

        // Top Navy Branding Banner
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.NAVY_DARK, getWidth(), getHeight(), Theme.NAVY_ACCENT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        banner.setPreferredSize(new Dimension(520, 130));
        banner.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel bannerText = new JPanel(new GridLayout(2, 1, 0, 4));
        bannerText.setOpaque(false);

        JLabel logoLabel = new JLabel("✈ COMMERCIAL AIRLINE FLEET");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoLabel.setForeground(Theme.WHITE);
        bannerText.add(logoLabel);

        JLabel subLogoLabel = new JLabel("Fuel Efficiency & Maintenance Analytics Engine");
        subLogoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLogoLabel.setForeground(Theme.LIGHT_BLUE_CYAN);
        bannerText.add(subLogoLabel);

        banner.add(bannerText, BorderLayout.CENTER);
        container.add(banner, BorderLayout.NORTH);

        // Center Login Form Card
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        centerWrapper.setOpaque(false);

        JPanel loginCard = Theme.createCardPanel();
        loginCard.setPreferredSize(new Dimension(440, 340));
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));

        JLabel loginHeader = new JLabel("Sign In to Flight Analytics Console");
        loginHeader.setFont(Theme.FONT_SUBTITLE);
        loginHeader.setForeground(Theme.NAVY_DARK);
        loginHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(loginHeader);
        loginCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Username
        JLabel userLabel = new JLabel("Username / ID");
        userLabel.setFont(Theme.FONT_SMALL);
        userLabel.setForeground(Theme.TEXT_MUTED);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(userLabel);
        loginCard.add(Box.createRigidArea(new Dimension(0, 4)));

        usernameField = new JTextField("admin");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        usernameField.setFont(Theme.FONT_BODY);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(usernameField);
        loginCard.add(Box.createRigidArea(new Dimension(0, 12)));

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.FONT_SMALL);
        passLabel.setForeground(Theme.TEXT_MUTED);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(passLabel);
        loginCard.add(Box.createRigidArea(new Dimension(0, 4)));

        passwordField = new JPasswordField("admin123");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        passwordField.setFont(Theme.FONT_BODY);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(passwordField);
        loginCard.add(Box.createRigidArea(new Dimension(0, 18)));

        // Login Button
        JButton loginBtn = Theme.createPrimaryButton("Authenticate & Launch Dashboard");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> attemptLogin());
        loginCard.add(loginBtn);
        loginCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // Status Feedback
        statusLabel = new JLabel("Ready for authorization.");
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.TEXT_MUTED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(statusLabel);
        loginCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // Quick Demo Accounts
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        demoPanel.setOpaque(false);
        demoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel demoLbl = new JLabel("Demo Roles:");
        demoLbl.setFont(Theme.FONT_SMALL);
        demoLbl.setForeground(Theme.TEXT_MUTED);
        demoPanel.add(demoLbl);

        JButton adminDemo = Theme.createOutlineButton("Admin");
        adminDemo.addActionListener(e -> { usernameField.setText("admin"); passwordField.setText("admin123"); });
        demoPanel.add(adminDemo);

        JButton engDemo = Theme.createOutlineButton("Engineer");
        engDemo.addActionListener(e -> { usernameField.setText("engineer"); passwordField.setText("engineer123"); });
        demoPanel.add(engDemo);

        JButton anaDemo = Theme.createOutlineButton("Analyst");
        anaDemo.addActionListener(e -> { usernameField.setText("analyst"); passwordField.setText("analyst123"); });
        demoPanel.add(anaDemo);

        loginCard.add(demoPanel);
        centerWrapper.add(loginCard);
        container.add(centerWrapper, BorderLayout.CENTER);

        // Enter key submits login
        getRootPane().setDefaultButton(loginBtn);

        setContentPane(container);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            statusLabel.setForeground(Theme.CRITICAL_RED);
            return;
        }

        try {
            User user = fleetDAO.authenticate(username, password);
            if (user != null) {
                statusLabel.setText("Login successful! Loading dashboard...");
                statusLabel.setForeground(Theme.SUCCESS_GREEN);

                // Open Main Dashboard Frame
                SwingUtilities.invokeLater(() -> {
                    MainDashboardFrame mainFrame = new MainDashboardFrame(user);
                    mainFrame.setVisible(true);
                    dispose(); // Close login window
                });

            } else {
                statusLabel.setText("Invalid credentials. Try demo credentials above.");
                statusLabel.setForeground(Theme.CRITICAL_RED);
            }
        } catch (SQLException e) {
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setForeground(Theme.CRITICAL_RED);

            int choice = JOptionPane.showConfirmDialog(
                this,
                "Could not connect to MySQL database (" + e.getMessage() + ").\n\nWould you like to continue in Offline Demo Mode or configure MySQL settings?",
                "MySQL Connection Required",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                // Allow offline demo access so user can explore UI and configure DB inside settings
                User demoUser = new User(1, username, "Demo Flight Director", "ADMIN");
                MainDashboardFrame mainFrame = new MainDashboardFrame(demoUser);
                mainFrame.setVisible(true);
                dispose();
            }
        }
    }
}
