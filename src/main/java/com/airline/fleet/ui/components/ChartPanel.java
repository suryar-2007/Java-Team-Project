package com.airline.fleet.ui.components;

import com.airline.fleet.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.Map;

/**
 * Custom High-Resolution Java2D Vector Chart Panel.
 * Renders Bar Charts, Doughnut Charts, and Gauge visualizers without external dependencies.
 */
public class ChartPanel extends JPanel {

    public enum ChartType {
        BAR_CATEGORY_FUEL,
        DOUGHNUT_FLEET_GRADE,
        VIBRATION_STATUS_GAUGE
    }

    private final ChartType chartType;
    private final String chartTitle;
    private Map<String, ?> chartData;

    public ChartPanel(String title, ChartType type) {
        this.chartTitle = title;
        this.chartType = type;
        setOpaque(false);
        setPreferredSize(new Dimension(380, 260));
    }

    public void updateData(Map<String, ?> data) {
        this.chartData = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Card background
        g2.setColor(Theme.WHITE);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
        g2.setColor(Theme.BORDER_COLOR);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

        // Header
        g2.setFont(Theme.FONT_SUBTITLE);
        g2.setColor(Theme.TEXT_DARK);
        g2.drawString(chartTitle, 18, 28);
        g2.setColor(Theme.BORDER_COLOR);
        g2.drawLine(18, 38, w - 18, 38);

        if (chartData == null || chartData.isEmpty()) {
            g2.setFont(Theme.FONT_BODY);
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString("No operational data recorded yet.", 24, h / 2);
            g2.dispose();
            return;
        }

        switch (chartType) {
            case BAR_CATEGORY_FUEL:
                renderBarChart(g2, w, h);
                break;
            case DOUGHNUT_FLEET_GRADE:
                renderDoughnutChart(g2, w, h);
                break;
            case VIBRATION_STATUS_GAUGE:
                renderVibrationGauge(g2, w, h);
                break;
        }

        g2.dispose();
    }

    @SuppressWarnings("unchecked")
    private void renderBarChart(Graphics2D g2, int w, int h) {
        Map<String, Double> data = (Map<String, Double>) chartData;
        double maxVal = 1.0;
        for (Double v : data.values()) {
            if (v != null && v > maxVal) maxVal = v;
        }

        int startY = 60;
        int usableHeight = h - startY - 40;
        int barHeight = Math.max(18, usableHeight / (data.size() * 2));
        int currentY = startY + 10;

        Color[] colors = { new Color(14, 165, 233), new Color(99, 102, 241), new Color(168, 85, 247) };
        int colorIdx = 0;

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            String label = entry.getKey().replace("_", " ");
            if (label.length() > 16) label = label.substring(0, 14) + "..";
            double val = entry.getValue() != null ? entry.getValue() : 0.0;

            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.TEXT_DARK);
            g2.drawString(label, 20, currentY + 12);

            int barMaxW = w - 190;
            int barW = (int) ((val / maxVal) * barMaxW);

            g2.setColor(new Color(241, 245, 249));
            g2.fillRoundRect(120, currentY, barMaxW, barHeight, 6, 6);

            g2.setColor(colors[colorIdx % colors.length]);
            g2.fillRoundRect(120, currentY, Math.max(4, barW), barHeight, 6, 6);

            // Value text
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString(String.format("%,.0f kg", val), 120 + barMaxW + 8, currentY + 13);

            currentY += barHeight + 14;
            colorIdx++;
        }
    }

    @SuppressWarnings("unchecked")
    private void renderDoughnutChart(Graphics2D g2, int w, int h) {
        Map<String, Integer> data = (Map<String, Integer>) chartData;
        int total = 0;
        for (Integer cnt : data.values()) {
            if (cnt != null) total += cnt;
        }

        if (total == 0) total = 1; // avoid divide by zero

        int size = Math.min(w / 2 - 20, h - 90);
        int cx = 30;
        int cy = 60;

        double curAngle = 90.0;
        Color[] gradeColors = {
            Theme.SUCCESS_GREEN,        // Grade A
            Theme.LIGHT_BLUE,           // Grade B
            Theme.WARNING_AMBER,        // Grade C
            Theme.CRITICAL_RED          // Outlier
        };
        String[] grades = { "A", "B", "C", "OUTLIER" };

        for (int i = 0; i < grades.length; i++) {
            int count = data.getOrDefault(grades[i], 0);
            double arcAngle = (count / (double) total) * 360.0;
            if (arcAngle > 0) {
                g2.setColor(gradeColors[i]);
                g2.fill(new Arc2D.Double(cx, cy, size, size, curAngle, -arcAngle, Arc2D.PIE));
                curAngle -= arcAngle;
            }
        }

        // Inner circle hole (Doughnut effect)
        int holeSize = size / 2;
        int holeOffset = (size - holeSize) / 2;
        g2.setColor(Theme.WHITE);
        g2.fillOval(cx + holeOffset, cy + holeOffset, holeSize, holeSize);

        // Center total text
        g2.setFont(Theme.FONT_SUBTITLE);
        g2.setColor(Theme.TEXT_DARK);
        String totStr = total + " Flts";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totStr, cx + (size - fm.stringWidth(totStr)) / 2, cy + (size / 2) + 5);

        // Legend on right
        int legendX = cx + size + 25;
        int legendY = cy + 20;
        for (int i = 0; i < grades.length; i++) {
            int count = data.getOrDefault(grades[i], 0);
            g2.setColor(gradeColors[i]);
            g2.fillRoundRect(legendX, legendY, 12, 12, 3, 3);

            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.TEXT_DARK);
            String label = grades[i] + " (" + count + ")";
            g2.drawString(label, legendX + 18, legendY + 10);
            legendY += 22;
        }
    }

    private void renderVibrationGauge(Graphics2D g2, int w, int h) {
        // Renders visual alert thresholds: Normal (<2.5), Caution (2.5-4.0), Critical (>4.0)
        int barX = 30;
        int barY = 90;
        int barW = w - 60;
        int barH = 26;

        // Normal zone (0 to 2.5 mils) -> 50% width
        int wNormal = (int) (barW * 0.50);
        // Caution zone (2.5 to 4.0 mils) -> 30% width
        int wCaution = (int) (barW * 0.30);
        // Critical zone (> 4.0 mils) -> 20% width
        int wCritical = barW - wNormal - wCaution;

        g2.setColor(Theme.SUCCESS_GREEN);
        g2.fillRoundRect(barX, barY, wNormal, barH, 8, 8);
        g2.fillRect(barX + 6, barY, wNormal - 6, barH);

        g2.setColor(Theme.WARNING_AMBER);
        g2.fillRect(barX + wNormal, barY, wCaution, barH);

        g2.setColor(Theme.CRITICAL_RED);
        g2.fillRoundRect(barX + wNormal + wCaution, barY, wCritical, barH, 8, 8);
        g2.fillRect(barX + wNormal + wCaution, barY, 6, barH);

        // Labels
        g2.setFont(Theme.FONT_SMALL);
        g2.setColor(Theme.TEXT_DARK);
        g2.drawString("Normal: < 2.5 mils", barX, barY + barH + 20);
        g2.drawString("Caution: 2.5 - 4.0 mils", barX + wNormal - 15, barY + barH + 20);
        g2.drawString("Critical: > 4.0 mils", barX + wNormal + wCaution - 10, barY + barH + 20);

        g2.setFont(Theme.FONT_BODY);
        g2.setColor(Theme.TEXT_MUTED);
        g2.drawString("Automated Telemetry alerts trigger immediately upon Critical breach.", barX, barY + barH + 45);
    }
}
