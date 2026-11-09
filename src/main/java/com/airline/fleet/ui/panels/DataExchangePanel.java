package com.airline.fleet.ui.panels;

import com.airline.fleet.AuditExporter;
import com.airline.fleet.TelemetryWorker;
import com.airline.fleet.model.User;
import com.airline.fleet.service.CsvImportService;
import com.airline.fleet.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;

/**
 * DataExchangePanel: CSV Flight Data Ingestion & TXT Maintenance Audit Report Exporter.
 */
public class DataExchangePanel extends JPanel {

    private final TelemetryWorker telemetryWorker;
    private final User currentUser;
    private final AuditExporter auditExporter;
    private final CsvImportService csvImportService;

    private JTextArea logArea;
    private JTextArea reportPreviewArea;
    private File lastGeneratedReport;

    public DataExchangePanel(TelemetryWorker telemetryWorker, User currentUser) {
        this.telemetryWorker = telemetryWorker;
        this.currentUser = currentUser;
        this.auditExporter = new AuditExporter();
        this.csvImportService = new CsvImportService(telemetryWorker);

        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Data Exchange: CSV Flight Ingestion & TXT Audit Exporter");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.NAVY_DARK);
        topPanel.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Automated Batch Telemetry Ingestion & Compliance Reporting");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        topPanel.add(subtitle, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Split: Left = Actions, Right = Preview/Console
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        centerPanel.setOpaque(false);

        // LEFT CARD: Actions
        JPanel leftCard = Theme.createCardPanel();
        leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));

        // 1. CSV Section
        JPanel csvSection = new JPanel(new BorderLayout(8, 8));
        csvSection.setOpaque(false);
        csvSection.setBorder(BorderFactory.createTitledBorder("1. CSV Flight Data Import (sample-data/flights.csv)"));

        JTextArea csvDesc = new JTextArea(
            "Batch imports flight records, validates flight numbers using regex ([A-Z]{2,3}\\d{3,4}), " +
            "computes three-phase fuel burn, SFC, carbon intensity, and evaluates engine vibration thresholds."
        );
        csvDesc.setFont(Theme.FONT_SMALL);
        csvDesc.setForeground(Theme.TEXT_MUTED);
        csvDesc.setWrapStyleWord(true);
        csvDesc.setLineWrap(true);
        csvDesc.setEditable(false);
        csvDesc.setOpaque(false);
        csvSection.add(csvDesc, BorderLayout.NORTH);

        JPanel csvBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        csvBtnRow.setOpaque(false);

        JButton importSampleBtn = Theme.createPrimaryButton("Import sample-data/flights.csv");
        importSampleBtn.addActionListener(e -> importSampleCsv());
        csvBtnRow.add(importSampleBtn);

        JButton chooseCsvBtn = Theme.createOutlineButton("Select Custom CSV File...");
        chooseCsvBtn.addActionListener(e -> chooseAndImportCsv());
        csvBtnRow.add(chooseCsvBtn);

        csvSection.add(csvBtnRow, BorderLayout.SOUTH);
        leftCard.add(csvSection);
        leftCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // 2. TXT Audit Section
        JPanel txtSection = new JPanel(new BorderLayout(8, 8));
        txtSection.setOpaque(false);
        txtSection.setBorder(BorderFactory.createTitledBorder("2. TXT Maintenance Audit Report Exporter"));

        JTextArea txtDesc = new JTextArea(
            "Generates formatted text audit reports with String.format(), substring() formatting, " +
            "aircraft health logs, engine vibration anomaly alerts, and ICAO compliance certificates."
        );
        txtDesc.setFont(Theme.FONT_SMALL);
        txtDesc.setForeground(Theme.TEXT_MUTED);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setLineWrap(true);
        txtDesc.setEditable(false);
        txtDesc.setOpaque(false);
        txtSection.add(txtDesc, BorderLayout.NORTH);

        JPanel txtBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        txtBtnRow.setOpaque(false);

        JButton exportTxtBtn = Theme.createPrimaryButton("📄 Export TXT Maintenance Report");
        exportTxtBtn.addActionListener(e -> exportMaintenanceReport());
        txtBtnRow.add(exportTxtBtn);

        JButton openFileBtn = Theme.createOutlineButton("Open in Notepad");
        openFileBtn.addActionListener(e -> openReportInSystemEditor());
        txtBtnRow.add(openFileBtn);

        txtSection.add(txtBtnRow, BorderLayout.SOUTH);
        leftCard.add(txtSection);
        leftCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Ingestion Console Log
        JPanel logSection = new JPanel(new BorderLayout(5, 5));
        logSection.setOpaque(false);
        logSection.setBorder(BorderFactory.createTitledBorder("Batch Ingestion Status Log"));

        logArea = new JTextArea(8, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(15, 23, 42));
        logArea.setForeground(new Color(147, 197, 253));
        logSection.add(new JScrollPane(logArea), BorderLayout.CENTER);
        leftCard.add(logSection);

        centerPanel.add(leftCard);

        // RIGHT CARD: Report Preview
        JPanel rightCard = Theme.createCardPanel();
        rightCard.setLayout(new BorderLayout(8, 8));

        JLabel previewTitle = new JLabel("Generated Audit Report Preview (TXT)");
        previewTitle.setFont(Theme.FONT_SUBTITLE);
        previewTitle.setForeground(Theme.NAVY_DARK);
        rightCard.add(previewTitle, BorderLayout.NORTH);

        reportPreviewArea = new JTextArea();
        reportPreviewArea.setEditable(false);
        reportPreviewArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        reportPreviewArea.setBackground(new Color(248, 250, 252));
        reportPreviewArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        reportPreviewArea.setText("Click 'Export TXT Maintenance Report' to generate and preview audit documentation.");

        JScrollPane previewScroll = new JScrollPane(reportPreviewArea);
        previewScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        rightCard.add(previewScroll, BorderLayout.CENTER);

        centerPanel.add(rightCard);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void importSampleCsv() {
        File sampleFile = new File("sample-data/flights.csv");
        if (!sampleFile.exists()) {
            JOptionPane.showMessageDialog(this, "sample-data/flights.csv not found at: " + sampleFile.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        processCsvFile(sampleFile);
    }

    private void chooseAndImportCsv() {
        JFileChooser chooser = new JFileChooser(new File("sample-data"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            processCsvFile(chooser.getSelectedFile());
        }
    }

    private void processCsvFile(File file) {
        logArea.append(">>> Starting CSV batch import from: " + file.getName() + "\n");
        CsvImportService.ImportResult result = csvImportService.importFlightsCsv(file);

        logArea.append(String.format("Import Complete: %d processed | %d succeeded | %d failed\n",
            result.getTotalProcessed(), result.getSuccessCount(), result.getFailureCount()));

        for (String err : result.getErrorMessages()) {
            logArea.append("  [NOTICE] " + err + "\n");
        }
        logArea.append(">>> Telemetry streams submitted to background ExecutorService worker threads.\n\n");

        JOptionPane.showMessageDialog(this,
            String.format("CSV Import Finished:\n• Total Processed: %d\n• Successfully Ingested: %d\n• Errors/Skipped: %d",
                result.getTotalProcessed(), result.getSuccessCount(), result.getFailureCount()),
            "CSV Batch Import",
            result.getFailureCount() == 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private void exportMaintenanceReport() {
        try {
            String auditor = currentUser != null ? currentUser.getFullName() + " (" + currentUser.getRole() + ")" : "Fleet Engineering Admin";
            lastGeneratedReport = auditExporter.generateMaintenanceReport(auditor);

            String content = Files.readString(lastGeneratedReport.toPath());
            reportPreviewArea.setText(content);
            reportPreviewArea.setCaretPosition(0);

            logArea.append(">>> Generated TXT Maintenance Audit Report: " + lastGeneratedReport.getAbsolutePath() + "\n");
            JOptionPane.showMessageDialog(this,
                "Maintenance Audit Report successfully generated!\nSaved at: " + lastGeneratedReport.getAbsolutePath(),
                "Report Generated",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to generate audit report: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openReportInSystemEditor() {
        if (lastGeneratedReport == null || !lastGeneratedReport.exists()) {
            JOptionPane.showMessageDialog(this, "Please generate a maintenance report first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(lastGeneratedReport);
            } else {
                new ProcessBuilder("notepad.exe", lastGeneratedReport.getAbsolutePath()).start();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not open text editor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
