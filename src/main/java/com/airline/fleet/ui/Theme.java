package com.airline.fleet.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern Airline Design Theme
 * Navy Blue, Light Blue, Crisp White color palette.
 */
public class Theme {

    // Palette Colors
    public static final Color NAVY_DARK = new Color(10, 25, 47);        // Primary Dark #0A192F
    public static final Color NAVY_ACCENT = new Color(15, 34, 64);      // Deep Sidebar #0F2240
    public static final Color NAVY_CARD = new Color(23, 42, 69);        // Card background in dark #172A45
    public static final Color LIGHT_BLUE = new Color(2, 132, 199);      // Primary Accent Blue #0284C7
    public static final Color LIGHT_BLUE_CYAN = new Color(56, 189, 248); // Vibrant cyan #38BDF8
    public static final Color LIGHT_BLUE_BG = new Color(224, 242, 254); // Subtle pill #E0F2FE
    public static final Color BG_LIGHT = new Color(248, 250, 252);      // Content area #F8FAFC
    public static final Color WHITE = Color.WHITE;
    public static final Color TEXT_DARK = new Color(15, 23, 42);        // #0F172A
    public static final Color TEXT_MUTED = new Color(100, 116, 139);    // #64748B
    public static final Color TEXT_LIGHT = new Color(241, 245, 249);    // #F1F5F9
    public static final Color BORDER_COLOR = new Color(226, 232, 240);  // #E2E8F0

    // Severity Colors
    public static final Color CRITICAL_RED = new Color(239, 68, 68);
    public static final Color WARNING_AMBER = new Color(245, 158, 11);
    public static final Color SUCCESS_GREEN = new Color(16, 185, 129);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_HEADER_NUM = new Font("Segoe UI", Font.BOLD, 24);

    /**
     * Creates a styled modern primary button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(WHITE);
        btn.setBackground(LIGHT_BLUE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(LIGHT_BLUE_CYAN);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(LIGHT_BLUE);
            }
        });
        return btn;
    }

    /**
     * Creates a secondary outline button.
     */
    public static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(NAVY_DARK);
        btn.setBackground(WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(7, 15, 7, 15)
        ));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(LIGHT_BLUE_BG);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(WHITE);
            }
        });
        return btn;
    }

    /**
     * Creates an alert/danger button.
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(WHITE);
        btn.setBackground(CRITICAL_RED);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /**
     * Creates a rounded card panel for content sections.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return panel;
    }

    /**
     * Standardizes JTable styling for professional presentation.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(TEXT_DARK);
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    }
}
