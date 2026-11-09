package com.airline.fleet.ui.components;

import com.airline.fleet.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern Dashboard KPI Metric Card component.
 */
public class KpiCard extends JPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subLabel;
    private Color accentColor = Theme.LIGHT_BLUE;

    public KpiCard(String title, String initialValue, String subText, Color accent) {
        this.accentColor = accent != null ? accent : Theme.LIGHT_BLUE;
        setLayout(new BorderLayout(5, 5));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 18, 16, 18));
        setPreferredSize(new Dimension(220, 110));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(Theme.FONT_SMALL);
        titleLabel.setForeground(Theme.TEXT_MUTED);
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Accent dot/indicator
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillOval(2, 2, 8, 8);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(12, 12));
        dot.setOpaque(false);
        topPanel.add(dot, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(Theme.FONT_HEADER_NUM);
        valueLabel.setForeground(Theme.TEXT_DARK);
        add(valueLabel, BorderLayout.CENTER);

        subLabel = new JLabel(subText);
        subLabel.setFont(Theme.FONT_SMALL);
        subLabel.setForeground(accentColor);
        add(subLabel, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubText(String subText) {
        subLabel.setText(subText);
    }

    public void setAccentColor(Color color) {
        this.accentColor = color;
        subLabel.setForeground(color);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        g2.setColor(Theme.BORDER_COLOR);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        // Top accent line
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, 4, 4, 4);
        g2.dispose();
    }
}
