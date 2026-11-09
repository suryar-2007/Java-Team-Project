package com.airline.fleet.ui.panels;

import com.airline.fleet.FleetDAO;
import com.airline.fleet.model.FleetCategory;
import com.airline.fleet.model.FleetGrade;
import com.airline.fleet.service.AnalyticsService;
import com.airline.fleet.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * AnalyticsPanel: Aviation Engineering Calculations & Configurable Scoring Center.
 * Demonstrates:
 * 1. SFC = TotalFuelBurned / (PayloadWeight * Distance)
 * 2. Fleet Carbon Intensity = (FuelBurned * 3.16) / ASK
 * 3. Fleet Grading A, B, C and Outlier
 * 4. Configurable scoring parameters and vibration limits
 */
public class AnalyticsPanel extends JPanel {

    private final FleetDAO fleetDAO;
    private final AnalyticsService analyticsService;

    // Interactive Calculator Fields
    private JTextField fuelField;
    private JTextField payloadField;
    private JTextField distanceField;
    private JTextField seatsField;
    private JComboBox<FleetCategory> categoryField;
    private JLabel sfcResultLabel;
    private JLabel carbonResultLabel;
    private JLabel gradeResultLabel;
    private JTextArea mathBreakdownArea;

    // Configurable Parameters Fields
    private JTextField co2FactorField;
    private JTextField vibCautionField;
    private JTextField vibCriticalField;
    private JTextField gradeAField;
    private JTextField gradeBField;
    private JTextField outlierPctField;

    public AnalyticsPanel() {
        this.fleetDAO = new FleetDAO();
        this.analyticsService = new AnalyticsService();

        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadSystemConfig();
        runCalculation();
    }

    private void initUI() {
        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Aviation Fuel Efficiency & Carbon Analytics Engine");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.NAVY_DARK);
        topPanel.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("ICAO Annex 16 Vol IV & Aviation Engineering Compliance");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        topPanel.add(subtitle, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Split: Left = Interactive Calculator, Right = Configurable Parameters
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        centerPanel.setOpaque(false);

        // =====================================================================
        // LEFT CARD: Interactive Calculation Simulator
        // =====================================================================
        JPanel calcCard = Theme.createCardPanel();
        calcCard.setLayout(new BorderLayout(10, 10));

        JLabel calcTitle = new JLabel("Interactive Aviation Formula Simulator");
        calcTitle.setFont(Theme.FONT_SUBTITLE);
        calcTitle.setForeground(Theme.NAVY_DARK);
        calcCard.add(calcTitle, BorderLayout.NORTH);

        JPanel calcForm = new JPanel(new GridLayout(6, 2, 8, 8));
        calcForm.setOpaque(false);

        fuelField = new JTextField("42500");
        payloadField = new JTextField("48500");
        distanceField = new JTextField("5550");
        seatsField = new JTextField("396");
        categoryField = new JComboBox<>(FleetCategory.values());

        calcForm.add(new JLabel("Total Fuel Burned (kg):")); calcForm.add(fuelField);
        calcForm.add(new JLabel("Payload Weight (kg):")); calcForm.add(payloadField);
        calcForm.add(new JLabel("Flight Distance (km):")); calcForm.add(distanceField);
        calcForm.add(new JLabel("Available Seats (ASK):")); calcForm.add(seatsField);
        calcForm.add(new JLabel("Fleet Category:")); calcForm.add(categoryField);

        JButton computeBtn = Theme.createPrimaryButton("⚡ Calculate Engineering Metrics");
        computeBtn.addActionListener(e -> runCalculation());
        calcForm.add(new JLabel("")); calcForm.add(computeBtn);

        calcCard.add(calcForm, BorderLayout.CENTER);

        // Results Box
        JPanel resultsBox = new JPanel(new BorderLayout(5, 5));
        resultsBox.setOpaque(false);
        resultsBox.setBorder(BorderFactory.createTitledBorder("Calculated Engineering Output"));

        JPanel labelGrid = new JPanel(new GridLayout(3, 1, 4, 4));
        labelGrid.setOpaque(false);

        sfcResultLabel = new JLabel("Specific Fuel Consumption (SFC): -");
        sfcResultLabel.setFont(Theme.FONT_BOLD);
        sfcResultLabel.setForeground(Theme.LIGHT_BLUE);
        labelGrid.add(sfcResultLabel);

        carbonResultLabel = new JLabel("Fleet Carbon Intensity: -");
        carbonResultLabel.setFont(Theme.FONT_BOLD);
        carbonResultLabel.setForeground(Theme.WARNING_AMBER);
        labelGrid.add(carbonResultLabel);

        gradeResultLabel = new JLabel("Assigned Fleet Efficiency Grade: -");
        gradeResultLabel.setFont(Theme.FONT_BOLD);
        gradeResultLabel.setForeground(Theme.SUCCESS_GREEN);
        labelGrid.add(gradeResultLabel);

        resultsBox.add(labelGrid, BorderLayout.NORTH);

        mathBreakdownArea = new JTextArea(5, 30);
        mathBreakdownArea.setEditable(false);
        mathBreakdownArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        mathBreakdownArea.setBackground(new Color(241, 245, 249));
        mathBreakdownArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        resultsBox.add(new JScrollPane(mathBreakdownArea), BorderLayout.CENTER);

        calcCard.add(resultsBox, BorderLayout.SOUTH);
        centerPanel.add(calcCard);

        // =====================================================================
        // RIGHT CARD: Configurable Scoring Parameters
        // =====================================================================
        JPanel configCard = Theme.createCardPanel();
        configCard.setLayout(new BorderLayout(10, 10));

        JLabel configTitle = new JLabel("Configurable Scoring & Safety Parameters");
        configTitle.setFont(Theme.FONT_SUBTITLE);
        configTitle.setForeground(Theme.NAVY_DARK);
        configCard.add(configTitle, BorderLayout.NORTH);

        JPanel configForm = new JPanel(new GridLayout(7, 2, 8, 8));
        configForm.setOpaque(false);

        co2FactorField = new JTextField("3.16");
        vibCautionField = new JTextField("2.5");
        vibCriticalField = new JTextField("4.0");
        gradeAField = new JTextField("0.000038");
        gradeBField = new JTextField("0.000048");
        outlierPctField = new JTextField("25.0");

        configForm.add(new JLabel("CO2 Emission Constant (kg CO2/kg fuel):")); configForm.add(co2FactorField);
        configForm.add(new JLabel("Vibration Caution Limit (mils):")); configForm.add(vibCautionField);
        configForm.add(new JLabel("Vibration Critical Limit (mils):")); configForm.add(vibCriticalField);
        configForm.add(new JLabel("Grade A Max Threshold (SFC):")); configForm.add(gradeAField);
        configForm.add(new JLabel("Grade B Max Threshold (SFC):")); configForm.add(gradeBField);
        configForm.add(new JLabel("Outlier Threshold (% over baseline):")); configForm.add(outlierPctField);

        JButton saveConfigBtn = Theme.createPrimaryButton("💾 Save Parameters to MySQL");
        saveConfigBtn.addActionListener(e -> saveSystemConfig());
        configForm.add(new JLabel("")); configForm.add(saveConfigBtn);

        configCard.add(configForm, BorderLayout.CENTER);

        // Aviation Standard Explanations
        JTextArea explanationArea = new JTextArea();
        explanationArea.setEditable(false);
        explanationArea.setFont(Theme.FONT_SMALL);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setBackground(new Color(248, 250, 252));
        explanationArea.setText(
            "MATHEMATICAL SPECIFICATIONS & AVIATION STANDARDS:\n\n" +
            "1. Specific Fuel Consumption (SFC):\n" +
            "   SFC = TotalFuelBurned / (PayloadWeight * Distance)\n" +
            "   Units: kg_fuel / (kg_payload * km). Lower values reflect superior aerodynamic efficiency.\n\n" +
            "2. Fleet Carbon Intensity:\n" +
            "   Fleet Carbon Intensity = (FuelBurned * 3.16) / ASK\n" +
            "   where 3.16 is the standard ICAO Jet-A stoichiometric emission factor, and ASK = Seats * Distance.\n\n" +
            "3. Engine Vibration Limits:\n" +
            "   Normal: < 2.5 mils pk-pk\n" +
            "   Caution / Advisory: 2.5 - 4.0 mils\n" +
            "   Critical / Alert: > 4.0 mils (Automatic grounding alert triggered)\n\n" +
            "4. Fleet Grading:\n" +
            "   Grade A (<= 0.000038) | Grade B (<= 0.000048) | Grade C (> 0.000048) | Outlier (> +25% baseline)"
        );
        explanationArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        configCard.add(new JScrollPane(explanationArea), BorderLayout.SOUTH);

        centerPanel.add(configCard);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void runCalculation() {
        try {
            double fuel = Double.parseDouble(fuelField.getText().trim());
            double payload = Double.parseDouble(payloadField.getText().trim());
            double dist = Double.parseDouble(distanceField.getText().trim());
            int seats = Integer.parseInt(seatsField.getText().trim());
            FleetCategory cat = (FleetCategory) categoryField.getSelectedItem();

            double sfc = analyticsService.calculateSFC(fuel, payload, dist);
            double carbon = analyticsService.calculateCarbonIntensity(fuel, seats, payload, dist);
            FleetGrade grade = analyticsService.evaluateFleetGrade(sfc, cat != null ? cat.getDefaultBaselineSfc() : 0.000042);

            sfcResultLabel.setText(String.format("Specific Fuel Consumption (SFC): %.7f kg_fuel/(kg*km)", sfc));
            carbonResultLabel.setText(String.format("Fleet Carbon Intensity: %.4f kg CO2/ASK", carbon));
            gradeResultLabel.setText(String.format("Assigned Fleet Efficiency Grade: %s", grade.getDescription()));

            mathBreakdownArea.setText(
                String.format("STEP-BY-STEP MATHEMATICAL PROOF:\n" +
                    "• SFC = %,.0f kg / (%,.0f kg * %,.0f km) = %.7f kg_fuel/(kg*km)\n" +
                    "• ASK = %d seats * %,.0f km = %,.0f Available Seat Kilometers\n" +
                    "• Total CO2 = %,.0f kg fuel * %.2f factor = %,.1f kg CO2\n" +
                    "• Carbon Intensity = %,.1f kg CO2 / %,.0f ASK = %.4f kg CO2/ASK\n" +
                    "• Evaluation: Classified as %s",
                    fuel, payload, dist, sfc,
                    seats, dist, (seats * dist),
                    fuel, analyticsService.getCo2Factor(), (fuel * analyticsService.getCo2Factor()),
                    (fuel * analyticsService.getCo2Factor()), (seats * dist), carbon,
                    grade.name())
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Calculation error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSystemConfig() {
        try {
            co2FactorField.setText(fleetDAO.getConfigValue("co2_emission_factor", "3.16"));
            vibCautionField.setText(fleetDAO.getConfigValue("vibration_caution_limit", "2.5"));
            vibCriticalField.setText(fleetDAO.getConfigValue("vibration_critical_limit", "4.0"));
            gradeAField.setText(fleetDAO.getConfigValue("sfc_grade_a_threshold", "0.000038"));
            gradeBField.setText(fleetDAO.getConfigValue("sfc_grade_b_threshold", "0.000048"));
            outlierPctField.setText(fleetDAO.getConfigValue("sfc_outlier_pct_deviation", "25.0"));

            applyLocalServiceSettings();
        } catch (Exception ignored) {}
    }

    private void saveSystemConfig() {
        try {
            double co2 = Double.parseDouble(co2FactorField.getText().trim());
            double caution = Double.parseDouble(vibCautionField.getText().trim());
            double critical = Double.parseDouble(vibCriticalField.getText().trim());
            double a = Double.parseDouble(gradeAField.getText().trim());
            double b = Double.parseDouble(gradeBField.getText().trim());
            double out = Double.parseDouble(outlierPctField.getText().trim());

            fleetDAO.setConfigValue("co2_emission_factor", String.valueOf(co2), "CO2 factor");
            fleetDAO.setConfigValue("vibration_caution_limit", String.valueOf(caution), "Caution vibration limit");
            fleetDAO.setConfigValue("vibration_critical_limit", String.valueOf(critical), "Critical vibration limit");
            fleetDAO.setConfigValue("sfc_grade_a_threshold", String.valueOf(a), "Grade A limit");
            fleetDAO.setConfigValue("sfc_grade_b_threshold", String.valueOf(b), "Grade B limit");
            fleetDAO.setConfigValue("sfc_outlier_pct_deviation", String.valueOf(out), "Outlier percent");

            applyLocalServiceSettings();

            JOptionPane.showMessageDialog(this, "Aviation scoring parameters successfully saved to MySQL airline_fleet_db!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            runCalculation();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric parameters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyLocalServiceSettings() {
        try {
            analyticsService.setCo2Factor(Double.parseDouble(co2FactorField.getText().trim()));
            analyticsService.setVibrationCaution(Double.parseDouble(vibCautionField.getText().trim()));
            analyticsService.setVibrationCritical(Double.parseDouble(vibCriticalField.getText().trim()));
            analyticsService.setSfcGradeA(Double.parseDouble(gradeAField.getText().trim()));
            analyticsService.setSfcGradeB(Double.parseDouble(gradeBField.getText().trim()));
            analyticsService.setOutlierPctDev(Double.parseDouble(outlierPctField.getText().trim()));
        } catch (Exception ignored) {}
    }
}
