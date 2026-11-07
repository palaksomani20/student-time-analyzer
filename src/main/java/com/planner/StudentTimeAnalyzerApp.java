/*
 * StudentTimeAnalyzerApp.java
 * Visual & Fun Modern Web-Style Student Time Planner & Analytics Hub
 * Supporting Multi-User Session Isolation & Data Persistence
 */

package com.planner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StudentTimeAnalyzerApp extends Application {

    // Modern Colorful Web Theme Palette
    private static final String COLOR_BG_CANVAS = "#f8fafc";
    private static final String COLOR_CARD_BG = "#ffffff";
    private static final String COLOR_PRIMARY = "#6366f1";       // Vibrant Indigo
    private static final String COLOR_SECONDARY = "#ec4899";     // Vibrant Pink/Magenta
    private static final String COLOR_TEXT_DARK = "#0f172a";
    private static final String COLOR_TEXT_MUTED = "#64748b";
    private static final String COLOR_BORDER = "#e2e8f0";

    private static final String CSV_FILE = "student_planner_data.csv";

    // Category Specific Vibrant Hex Colors
    private static final Map<Category, String> CATEGORY_COLORS = Map.of(
            Category.SLEEP, "#8b5cf6",         // Purple
            Category.STUDY, "#3b82f6",         // Blue
            Category.EXERCISE, "#10b981",      // Emerald
            Category.MEALS, "#f59e0b",         // Amber
            Category.ENTERTAINMENT, "#ec4899", // Pink
            Category.OTHER, "#64748b"          // Slate
    );

    // Application State
    private String loggedInUser = "";
    private String userTargetSleep = "8.0";
    private String userTargetStudy = "6.0";
    private LocalDate selectedDate = LocalDate.now();

    private BorderPane mainRoot;
    // Database indexed by User -> Date -> List of TimeActivity
    private final Map<String, Map<LocalDate, List<TimeActivity>>> database = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        loadDataFromCSV();

        mainRoot = new BorderPane();
        mainRoot.setStyle("-fx-background-color: " + COLOR_BG_CANVAS + "; -fx-font-family: 'Segoe UI', 'Inter', sans-serif;");

        // Layer 1: Open with modern Web Sign-In page
        showLoginView();

        Scene scene = new Scene(mainRoot, 1080, 750);
        primaryStage.setTitle("⚡ Student Time Planner & Visual Analytics Hub");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Helper method to retrieve activities for the current user and date
    private List<TimeActivity> getCurrentUserActivities(LocalDate date) {
        Map<LocalDate, List<TimeActivity>> userMap = database.getOrDefault(loggedInUser, Collections.emptyMap());
        return userMap.getOrDefault(date, Collections.emptyList());
    }

    // ==========================================
    // LAYER 1: AUTHENTICATION (LOGIN / SIGN UP)
    // ==========================================
    private void showLoginView() {
        mainRoot.setTop(null);

        VBox card = new VBox(22);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(440);
        card.setPadding(new Insets(45));
        card.setStyle("-fx-background-color: " + COLOR_CARD_BG + ";" +
                "-fx-background-radius: 20px;" +
                "-fx-border-radius: 20px;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-effect: dropshadow(three-pass-box, rgba(99, 102, 241, 0.12), 25, 0, 0, 10);");

        Label logo = new Label("✨ TimeHub AI");
        logo.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: linear-gradient(to right, #6366f1, #ec4899);");

        Label title = new Label("Welcome Back, Champion!");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_TEXT_DARK + ";");

        Label subtitle = new Label("Track your study power & sleep balance seamlessly.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COLOR_TEXT_MUTED + ";");

        TextField usernameField = new TextField();
        usernameField.setPromptText("👤 Username or Student ID");
        usernameField.setStyle(getInputStyle());

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("🔒 Password");
        passwordField.setStyle(getInputStyle());

        Button loginBtn = createGradientButton("🚀 Launch Workspace", COLOR_PRIMARY, COLOR_SECONDARY);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            String user = usernameField.getText().trim();
            if (!user.isEmpty()) {
                loggedInUser = user;
                showProfileCustomizationView();
            } else {
                showAlert("Authentication Required", "Please enter a valid student username.");
            }
        });

        Button guestBtn = new Button("🎮 Continue as Guest Explorer");
        guestBtn.setMaxWidth(Double.MAX_VALUE);
        guestBtn.setStyle(getSecondaryButtonStyle());
        guestBtn.setOnAction(e -> {
            loggedInUser = "Guest Explorer";
            showProfileCustomizationView();
        });

        card.getChildren().addAll(logo, title, subtitle, usernameField, passwordField, loginBtn, guestBtn);
        animateFadeIn(card);

        StackPane container = new StackPane(card);
        container.setPadding(new Insets(40));
        mainRoot.setCenter(container);
    }

    // ==========================================
    // LAYER 2: PROFILE & TARGET CUSTOMIZATION
    // ==========================================
    private void showProfileCustomizationView() {
        mainRoot.setTop(null);

        VBox card = new VBox(22);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(540);
        card.setPadding(new Insets(45));
        card.setStyle("-fx-background-color: " + COLOR_CARD_BG + ";" +
                "-fx-background-radius: 20px;" +
                "-fx-border-radius: 20px;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-effect: dropshadow(three-pass-box, rgba(99, 102, 241, 0.12), 25, 0, 0, 10);");

        Label header = new Label("🎯 Personalize Goals for " + loggedInUser);
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: " + COLOR_PRIMARY + ";");

        Label subHeader = new Label("Set up your daily power targets to generate habit scores and real-time alerts.");
        subHeader.setWrapText(true);
        subHeader.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COLOR_TEXT_MUTED + ";");

        Label sleepLabel = new Label("🌙 Target Daily Sleep (Hours):");
        sleepLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: " + COLOR_TEXT_DARK + ";");
        TextField sleepInput = new TextField(userTargetSleep);
        sleepInput.setStyle(getInputStyle());

        Label studyLabel = new Label("📚 Target Daily Focus Study (Hours):");
        studyLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: " + COLOR_TEXT_DARK + ";");
        TextField studyInput = new TextField(userTargetStudy);
        studyInput.setStyle(getInputStyle());

        Button proceedBtn = createGradientButton("⚡ Save Goals & Open Dashboard", COLOR_PRIMARY, COLOR_SECONDARY);
        proceedBtn.setMaxWidth(Double.MAX_VALUE);
        proceedBtn.setOnAction(e -> {
            userTargetSleep = sleepInput.getText().trim();
            userTargetStudy = studyInput.getText().trim();
            showMainWorkspaceHub();
        });

        card.getChildren().addAll(header, subHeader, new Separator(), sleepLabel, sleepInput, studyLabel, studyInput, proceedBtn);
        animateFadeIn(card);

        StackPane container = new StackPane(card);
        container.setPadding(new Insets(40));
        mainRoot.setCenter(container);
    }

    // ==========================================
    // LAYER 3: MAIN NAVIGATION HUB (DASHBOARD)
    // ==========================================
    private void showMainWorkspaceHub() {
        // Top Navigation Header
        HBox navbar = new HBox(15);
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setPadding(new Insets(16, 40, 16, 40));
        navbar.setStyle("-fx-background-color: " + COLOR_CARD_BG + "; -fx-border-color: transparent transparent " + COLOR_BORDER + " transparent;");

        Label logo = new Label("✨ TimeHub");
        logo.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: " + COLOR_PRIMARY + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        DatePicker datePicker = new DatePicker(selectedDate);
        datePicker.setStyle("-fx-background-color: transparent; -fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 8px;");
        datePicker.setOnAction(e -> {
            selectedDate = datePicker.getValue();
            showMainWorkspaceHub();
        });

        Label userBadge = new Label("👤 " + loggedInUser);
        userBadge.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: " + COLOR_PRIMARY + "; -fx-font-weight: 700; -fx-padding: 8 16 8 16; -fx-background-radius: 20px;");

        Button logoutBtn = new Button("🚪 Sign Out");
        logoutBtn.setStyle(getSecondaryButtonStyle());
        logoutBtn.setOnAction(e -> {
            loggedInUser = "";
            showLoginView();
        });

        navbar.getChildren().addAll(logo, spacer, new Label("📅 Date:"), datePicker, userBadge, logoutBtn);
        mainRoot.setTop(navbar);

        // Center Content Grid
        VBox content = new VBox(25);
        content.setPadding(new Insets(30, 40, 30, 40));

        // Quick Stats Summary Banner
        HBox statsBanner = createQuickStatsBanner();

        Label welcomeText = new Label("Workspace Modules");
        welcomeText.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_TEXT_DARK + ";");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        VBox cardScheduler = createVisualHubCard("📅 Activity Scheduler", "Log, edit, and categorize daily time slots", "purple", "Open Scheduler ➔", this::showSchedulerSubView);
        VBox cardBreakdown = createVisualHubCard("📊 Visual Breakdown", "Interactive colorful pie chart allocation analysis", "blue", "Open Analytics ➔", this::showBreakdownSubView);
        VBox cardInsights = createVisualHubCard("💡 Health & Habit Coach", "Sleep deficit badges, consistency score & AI tips", "emerald", "View Insights ➔", this::showInsightsSubView);
        VBox cardWeekly = createVisualHubCard("📈 Weekly History Trends", "7-day Sleep vs. Study comparison charts", "pink", "View Trends ➔", this::showWeeklyTrendsSubView);

        grid.add(cardScheduler, 0, 0);
        grid.add(cardBreakdown, 1, 0);
        grid.add(cardInsights, 0, 1);
        grid.add(cardWeekly, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        content.getChildren().addAll(statsBanner, welcomeText, grid);
        animateFadeIn(content);

        mainRoot.setCenter(new ScrollPane(content) {{
            setFitToWidth(true);
            setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        }});
    }

    private HBox createQuickStatsBanner() {
        HBox banner = new HBox(20);
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(20));
        banner.setStyle("-fx-background-color: " + COLOR_CARD_BG + ";" +
                "-fx-background-radius: 16px;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 10, 0, 0, 4);");

        List<TimeActivity> list = getCurrentUserActivities(selectedDate);
        double sleepTotal = 0;
        double studyTotal = 0;
        double totalLogged = 0;

        for (TimeActivity a : list) {
            double h = a.getDurationHours();
            totalLogged += h;
            if (a.category == Category.SLEEP) sleepTotal += h;
            if (a.category == Category.STUDY) studyTotal += h;
        }

        VBox stat1 = createStatMetric("⏱ Total Logged", String.format("%.1f hrs", totalLogged), "#6366f1");
        VBox stat2 = createStatMetric("🌙 Sleep Hours", String.format("%.1f hrs", sleepTotal), "#8b5cf6");
        VBox stat3 = createStatMetric("📚 Study Hours", String.format("%.1f hrs", studyTotal), "#3b82f6");
        VBox stat4 = createStatMetric("🏆 Productivity Level", totalLogged > 0 ? (studyTotal >= 4 ? "High 🔥" : "Moderate ⚡") : "No Data", "#10b981");

        banner.getChildren().addAll(stat1, new Separator(), stat2, new Separator(), stat3, new Separator(), stat4);
        HBox.setHgrow(stat1, Priority.ALWAYS);
        HBox.setHgrow(stat2, Priority.ALWAYS);
        HBox.setHgrow(stat3, Priority.ALWAYS);
        HBox.setHgrow(stat4, Priority.ALWAYS);
        return banner;
    }

    private VBox createStatMetric(String label, String value, String colorHex) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: " + COLOR_TEXT_MUTED + ";");

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: " + colorHex + ";");

        box.getChildren().addAll(lbl, val);
        return box;
    }

    private VBox createVisualHubCard(String title, String description, String themeTag, String buttonText, Runnable action) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(26));

        String accentColor = switch (themeTag) {
            case "purple" -> "#8b5cf6";
            case "blue" -> "#3b82f6";
            case "emerald" -> "#10b981";
            case "pink" -> "#ec4899";
            default -> COLOR_PRIMARY;
        };

        card.setStyle("-fx-background-color: " + COLOR_CARD_BG + ";" +
                "-fx-background-radius: 16px;" +
                "-fx-border-radius: 16px;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-border-width: 1px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 12, 0, 0, 5);");

        // Hover scale animation effect
        card.setOnMouseEntered(e -> animateCardHover(card, 1.02));
        card.setOnMouseExited(e -> animateCardHover(card, 1.0));

        Label t = new Label(title);
        t.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: " + COLOR_TEXT_DARK + ";");

        Label d = new Label(description);
        d.setWrapText(true);
        d.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COLOR_TEXT_MUTED + ";");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btn = createGradientButton(buttonText, accentColor, COLOR_PRIMARY);
        btn.setOnAction(e -> action.run());

        card.getChildren().addAll(t, d, spacer, btn);
        return card;
    }

    // ==========================================
    // LAYER 4: SUB-MODULE VIEWS (DOUBLE LAYER)
    // ==========================================
    private void showSubModuleContainer(String title, Pane bodyContent) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25, 40, 30, 40));

        HBox subHeader = new HBox(15);
        subHeader.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("⬅ Back to Hub");
        backBtn.setStyle(getSecondaryButtonStyle());
        backBtn.setOnAction(e -> showMainWorkspaceHub());

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: " + COLOR_TEXT_DARK + ";");

        subHeader.getChildren().addAll(backBtn, titleLabel);

        root.getChildren().addAll(subHeader, new Separator(), bodyContent);
        animateFadeIn(root);

        mainRoot.setCenter(new ScrollPane(root) {{
            setFitToWidth(true);
            setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        }});
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private void showSchedulerSubView() {
        VBox layout = new VBox(22);

        // Form Bar
        HBox form = new HBox(12);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(18));
        form.setStyle("-fx-background-color: " + COLOR_CARD_BG + "; -fx-background-radius: 14px; -fx-border-color: " + COLOR_BORDER + ";");

        TextField titleIn = new TextField();
        titleIn.setPromptText("📝 Activity Title (e.g., Java Practice)");
        titleIn.setStyle(getInputStyle());

        ComboBox<Category> catIn = new ComboBox<>(FXCollections.observableArrayList(Category.values()));
        catIn.setPromptText("🏷 Category");
        catIn.setStyle("-fx-background-color: #ffffff; -fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 8px;");

        TextField startIn = new TextField();
        startIn.setPromptText("⏰ Start (HH:MM)");
        startIn.setPrefWidth(120);
        startIn.setStyle(getInputStyle());

        TextField endIn = new TextField();
        endIn.setPromptText("⏰ End (HH:MM)");
        endIn.setPrefWidth(120);
        endIn.setStyle(getInputStyle());

        Button addBtn = createGradientButton("➕ Add Entry", COLOR_PRIMARY, COLOR_SECONDARY);

        form.getChildren().addAll(titleIn, catIn, startIn, endIn, addBtn);
        HBox.setHgrow(titleIn, Priority.ALWAYS);

        // Data Table
        TableView<TimeActivity> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: white; -fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 12px;");

        TableColumn<TimeActivity, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(data -> new SimpleStringProperty(getCategoryWithIcon(data.getValue().category)));

        TableColumn<TimeActivity, String> colTitle = new TableColumn<>("Activity Title");
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().title));

        TableColumn<TimeActivity, String> colStart = new TableColumn<>("Start Time");
        colStart.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().startTime.toString()));

        TableColumn<TimeActivity, String> colEnd = new TableColumn<>("End Time");
        colEnd.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().endTime.toString()));

        TableColumn<TimeActivity, String> colHrs = new TableColumn<>("Duration");
        colHrs.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.1f hrs", data.getValue().getDurationHours())));

        table.getColumns().addAll(colCat, colTitle, colStart, colEnd, colHrs);

        // User-isolated database lookup
        Map<LocalDate, List<TimeActivity>> userMap = database.computeIfAbsent(loggedInUser, k -> new HashMap<>());
        List<TimeActivity> currentList = userMap.computeIfAbsent(selectedDate, k -> new ArrayList<>());
        ObservableList<TimeActivity> tableData = FXCollections.observableArrayList(currentList);
        table.setItems(tableData);

        addBtn.setOnAction(e -> {
            try {
                String t = titleIn.getText().trim();
                Category c = catIn.getValue();
                LocalTime s = LocalTime.parse(startIn.getText().trim());
                LocalTime end = LocalTime.parse(endIn.getText().trim());

                if (!t.isEmpty() && c != null) {
                    TimeActivity act = new TimeActivity(t, c, s, end);
                    currentList.add(act);
                    tableData.add(act);
                    saveDataToCSV();

                    titleIn.clear();
                    startIn.clear();
                    endIn.clear();
                }
            } catch (Exception ex) {
                showAlert("Input Validation Error", "Please enter valid times in 24-hour HH:MM format (e.g., 09:00, 23:00).");
            }
        });

        Button deleteBtn = new Button("🗑 Delete Selected Entry");
        deleteBtn.setStyle(getSecondaryButtonStyle());
        deleteBtn.setOnAction(e -> {
            TimeActivity sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                currentList.remove(sel);
                tableData.remove(sel);
                saveDataToCSV();
            }
        });

        layout.getChildren().addAll(form, table, deleteBtn);
        showSubModuleContainer("📅 Daily Scheduler - " + selectedDate + " (" + loggedInUser + ")", layout);
    }

    private void showBreakdownSubView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: " + COLOR_CARD_BG + "; -fx-background-radius: 16px; -fx-border-color: " + COLOR_BORDER + ";");

        List<TimeActivity> list = getCurrentUserActivities(selectedDate);
        Map<Category, Double> totals = new EnumMap<>(Category.class);
        for (Category c : Category.values()) totals.put(c, 0.0);
        for (TimeActivity a : list) totals.put(a.category, totals.get(a.category) + a.getDurationHours());

        PieChart pie = new PieChart();
        pie.setTitle("Logged Time Allocation (" + selectedDate + ")");
        pie.setAnimated(true);

        totals.forEach((cat, hrs) -> {
            if (hrs > 0) {
                PieChart.Data slice = new PieChart.Data(getCategoryWithIcon(cat) + " (" + String.format("%.1fh", hrs) + ")", hrs);
                pie.getData().add(slice);
            }
        });

        if (pie.getData().isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(40));
            Label emptyLbl = new Label("🎨 No entries logged for " + selectedDate + " yet.\nAdd activities in the Scheduler to see colorful pie analytics!");
            emptyLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: " + COLOR_TEXT_MUTED + "; -fx-text-alignment: center;");
            emptyBox.getChildren().add(emptyLbl);
            layout.getChildren().add(emptyBox);
        } else {
            for (PieChart.Data data : pie.getData()) {
                String labelName = data.getName();
                for (Category cat : Category.values()) {
                    if (labelName.contains(cat.name())) {
                        String hex = CATEGORY_COLORS.getOrDefault(cat, "#6366f1");
                        data.getNode().setStyle("-fx-pie-color: " + hex + ";");
                        break;
                    }
                }
            }
            layout.getChildren().add(pie);
        }

        showSubModuleContainer("📊 Category Breakdown Analytics", layout);
    }

    private void showInsightsSubView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: " + COLOR_CARD_BG + "; -fx-background-radius: 16px; -fx-border-color: " + COLOR_BORDER + ";");

        List<TimeActivity> list = getCurrentUserActivities(selectedDate);
        double sleepHrs = 0;
        double studyHrs = 0;
        for (TimeActivity a : list) {
            if (a.category == Category.SLEEP) sleepHrs += a.getDurationHours();
            if (a.category == Category.STUDY) studyHrs += a.getDurationHours();
        }

        double targetSleep = Double.parseDouble(userTargetSleep.replaceAll("[^0-9.]", "8.0"));
        double targetStudy = Double.parseDouble(userTargetStudy.replaceAll("[^0-9.]", "6.0"));

        Label title = new Label("💡 Health Score & Productivity Coach");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: " + COLOR_TEXT_DARK + ";");

        HBox metricsBox = new HBox(15);
        Label sleepReport = createPillBadge(String.format("🌙 Sleep: %.1f / %.1f hrs Target", sleepHrs, targetSleep), sleepHrs >= targetSleep ? "#10b981" : "#ef4444");
        Label studyReport = createPillBadge(String.format("📚 Study: %.1f / %.1f hrs Target", studyHrs, targetStudy), studyHrs >= targetStudy ? "#10b981" : "#f59e0b");
        Label healthBadge = createPillBadge("🏆 Wellness Rating: " + (sleepHrs >= targetSleep && studyHrs >= targetStudy ? "Excellent ✨" : "Needs Tuning ⚡"), COLOR_PRIMARY);
        metricsBox.getChildren().addAll(sleepReport, studyReport, healthBadge);

        VBox feedbackCard = new VBox(12);
        feedbackCard.setPadding(new Insets(20));
        feedbackCard.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12px; -fx-border-color: " + COLOR_BORDER + ";");

        Label fbTitle = new Label("🤖 Coach Recommendation");
        fbTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + COLOR_PRIMARY + ";");

        StringBuilder sb = new StringBuilder();
        if (list.isEmpty()) {
            sb.append("No activities logged for today. Head over to the Scheduler to start logging!");
        } else {
            if (sleepHrs < targetSleep) {
                sb.append("⚠️ Sleep Warning: Logged ").append(String.format("%.1f", sleepHrs)).append(" hrs (Deficit of ").append(String.format("%.1f", targetSleep - sleepHrs)).append(" hrs). Prioritize rest tonight!\n");
            } else {
                sb.append("✅ Great job! Sleep target of ").append(targetSleep).append(" hrs achieved.\n");
            }

            if (studyHrs >= targetStudy) {
                sb.append("🔥 High Focus Day! Goal of ").append(targetStudy).append(" study hours completed.\n");
            } else {
                sb.append("📌 Study Goal: You need ").append(String.format("%.1f", targetStudy - studyHrs)).append(" more hours to hit your target study score today.\n");
            }
        }

        Label fbContent = new Label(sb.toString());
        fbContent.setWrapText(true);
        fbContent.setStyle("-fx-font-size: 14px; -fx-text-fill: " + COLOR_TEXT_DARK + ";");

        feedbackCard.getChildren().addAll(fbTitle, fbContent);
        layout.getChildren().addAll(title, metricsBox, new Separator(), feedbackCard);
        showSubModuleContainer("💡 Health & Habits Coach", layout);
    }

    @SuppressWarnings("unchecked")
    private void showWeeklyTrendsSubView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: " + COLOR_CARD_BG + "; -fx-background-radius: 16px; -fx-border-color: " + COLOR_BORDER + ";");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Logged Hours");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("📈 7-Day Sleep vs. Study Trend");
        chart.setAnimated(false);

        XYChart.Series<String, Number> sleepSeries = new XYChart.Series<>();
        sleepSeries.setName("🌙 Sleep Hours");
        XYChart.Series<String, Number> studySeries = new XYChart.Series<>();
        studySeries.setName("📚 Study Hours");

        for (int i = 6; i >= 0; i--) {
            LocalDate d = selectedDate.minusDays(i);
            String dStr = d.format(DateTimeFormatter.ofPattern("MMM dd"));
            List<TimeActivity> activities = getCurrentUserActivities(d);

            double slp = 0;
            double std = 0;
            for (TimeActivity a : activities) {
                if (a.category == Category.SLEEP) slp += a.getDurationHours();
                if (a.category == Category.STUDY) std += a.getDurationHours();
            }

            sleepSeries.getData().add(new XYChart.Data<>(dStr, slp));
            studySeries.getData().add(new XYChart.Data<>(dStr, std));
        }

        chart.getData().addAll(sleepSeries, studySeries);

        // Interactive Tooltips & Bar Visibility
        for (XYChart.Series<String, Number> series : chart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip(series.getName() + "\nDate: " + data.getXValue() + "\nHours: " + String.format("%.1f hrs", data.getYValue().doubleValue()));
                Tooltip.install(data.getNode(), tooltip);
            }
        }

        layout.getChildren().add(chart);
        showSubModuleContainer("📈 Multi-Day Consistency Trends", layout);
    }

    // ==========================================
    // PERSISTENCE & USER DATA STORAGE
    // ==========================================
    private void loadDataFromCSV() {
        database.clear();
        Path path = Paths.get(CSV_FILE);
        if (!Files.exists(path)) return;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("User") || line.startsWith("Date")) continue;
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String user = parts[0].trim();
                    LocalDate d = LocalDate.parse(parts[1].trim());
                    Category cat = Category.valueOf(parts[2].trim());
                    String t = parts[3].trim();
                    LocalTime s = LocalTime.parse(parts[4].trim());
                    LocalTime end = LocalTime.parse(parts[5].trim());

                    database.computeIfAbsent(user, k -> new HashMap<>())
                            .computeIfAbsent(d, k -> new ArrayList<>())
                            .add(new TimeActivity(t, cat, s, end));
                }
            }
        } catch (Exception ignored) {}
    }

    private void saveDataToCSV() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_FILE))) {
            writer.write("User,Date,Category,Title,StartTime,EndTime\n");
            for (Map.Entry<String, Map<LocalDate, List<TimeActivity>>> userEntry : database.entrySet()) {
                String user = userEntry.getKey();
                for (Map.Entry<LocalDate, List<TimeActivity>> dateEntry : userEntry.getValue().entrySet()) {
                    String dStr = dateEntry.getKey().toString();
                    for (TimeActivity a : dateEntry.getValue()) {
                        writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                                user, dStr, a.category.name(), a.title, a.startTime, a.endTime));
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    // ==========================================
    // STYLES & VISUAL HELPERS
    // ==========================================
    private Button createGradientButton(String text, String colorHex1, String colorHex2) {
        Button btn = new Button(text);
        String baseStyle = "-fx-background-color: linear-gradient(to right, " + colorHex1 + ", " + colorHex2 + ");" +
                "-fx-text-fill: #ffffff;" +
                "-fx-font-weight: 700;" +
                "-fx-padding: 10 20 10 20;" +
                "-fx-background-radius: 10px;" +
                "-fx-cursor: hand;";
        btn.setStyle(baseStyle);

        btn.setOnMouseEntered(e -> btn.setStyle(baseStyle + " -fx-effect: dropshadow(three-pass-box, " + colorHex1 + ", 12, 0, 0, 4);"));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        return btn;
    }

    private String getSecondaryButtonStyle() {
        return "-fx-background-color: #f1f5f9;" +
                "-fx-text-fill: " + COLOR_TEXT_DARK + ";" +
                "-fx-font-weight: 700;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-border-radius: 10px;" +
                "-fx-cursor: hand;";
    }

    private String getInputStyle() {
        return "-fx-background-color: #ffffff;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-border-radius: 8px;" +
                "-fx-padding: 9 14 9 14;";
    }

    private Label createPillBadge(String text, String colorHex) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-background-color: " + colorHex + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: 700;" +
                "-fx-padding: 6 14 6 14;" +
                "-fx-background-radius: 20px;");
        return lbl;
    }

    private String getCategoryWithIcon(Category cat) {
        return switch (cat) {
            case SLEEP -> "🌙 SLEEP";
            case STUDY -> "📚 STUDY";
            case EXERCISE -> "🏋️ EXERCISE";
            case MEALS -> "🍽 MEALS";
            case ENTERTAINMENT -> "🎮 ENTERTAINMENT";
            case OTHER -> "⚙️ OTHER";
        };
    }

    private void animateFadeIn(Node node) {
        FadeTransition ft = new FadeTransition(javafx.util.Duration.millis(350), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void animateCardHover(Node node, double scale) {
        ScaleTransition st = new ScaleTransition(javafx.util.Duration.millis(180), node);
        st.setToX(scale);
        st.setToY(scale);
        st.play();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public enum Category {
        SLEEP, STUDY, EXERCISE, MEALS, ENTERTAINMENT, OTHER
    }

    public static class TimeActivity {
        public String title;
        public Category category;
        public LocalTime startTime;
        public LocalTime endTime;

        public TimeActivity(String title, Category category, LocalTime startTime, LocalTime endTime) {
            this.title = title;
            this.category = category;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public double getDurationHours() {
            long min = java.time.Duration.between(startTime, endTime).toMinutes();
            if (min < 0) min += 24 * 60;
            return min / 60.0;
        }
    }
}