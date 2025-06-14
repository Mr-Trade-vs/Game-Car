package integrative.task.control;

import integrative.task.model.PoliceCar;
import integrative.task.model.TypeIncident;
import integrative.task.structures.BinaryTreeIncident;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;

public class AlgorithmController {
    private VBox algorithmPanel;
    private Button dijkstraButton;
    private Button bfsButton;
    private Button incidentStatsButton;
    private Button showHelpButton;
    private Button hideHelpButton;
    private Label titleLabel;
    private Canvas canvas; // Reference to the canvas for focus management
    private BinaryTreeIncident incidentTree; // Reference to the incident tree

    // Points system
    private static int points = 0;
    private Label pointsLabel;

    // Reference to the car controller for toggling help buttons
    private CarController carController;


    public AlgorithmController() {
        initializeUI();
    }

    public AlgorithmController(Canvas canvas) {
        this.canvas = canvas;
        initializeUI();
    }

    public AlgorithmController(Canvas canvas, BinaryTreeIncident incidentTree) {
        this.canvas = canvas;
        this.incidentTree = incidentTree;
        initializeUI();
    }

    public AlgorithmController(Canvas canvas, BinaryTreeIncident incidentTree, CarController carController) {
        this.canvas = canvas;
        this.incidentTree = incidentTree;
        this.carController = carController;
        initializeUI();
    }

    private void initializeUI() {
        // Create the main panel
        algorithmPanel = new VBox(10);
        algorithmPanel.setPadding(new Insets(10));
        algorithmPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");
        algorithmPanel.setMaxWidth(250);
        algorithmPanel.setMaxHeight(200); // Increased height to accommodate new buttons

        // Create the points label (positioned separately in the top-left corner)
        pointsLabel = new Label("Points: " + points);
        pointsLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        pointsLabel.setTextFill(Color.WHITE);
        pointsLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 5; -fx-padding: 5;");
        pointsLabel.getStyleClass().add("points-label"); // Add CSS class for lookup
        pointsLabel.setTranslateX(10);
        pointsLabel.setTranslateY(10);

        // Create the title label
        titleLabel = new Label("Police Car Algorithm");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.WHITE);

        // Create the buttons
        dijkstraButton = createButton("Use Dijkstra", !PoliceCar.useBFS);
        bfsButton = createButton("Use BFS", PoliceCar.useBFS);

        // Create the incident statistics button
        incidentStatsButton = new Button("Incident Stats");
        incidentStatsButton.setPrefWidth(200);
        incidentStatsButton.setPrefHeight(60);
        incidentStatsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        // Position the incident stats button in the bottom-right corner
        incidentStatsButton.setTranslateX(-20);
        incidentStatsButton.setTranslateY(-20);

        // Set action handler for the incident stats button
        incidentStatsButton.setOnAction(e -> {
            showIncidentStatistics();
            returnFocusToCanvas();
        });

        // Update the button text initially
        updateIncidentStatsButton();

        // Create the show/hide help buttons
        showHelpButton = new Button("Monitoreo");
        showHelpButton.setPrefWidth(100);
        showHelpButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        hideHelpButton = new Button("Trafico");
        hideHelpButton.setPrefWidth(100);
        hideHelpButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");

        // Add action handlers for the show/hide help buttons
        if (carController != null) {
            showHelpButton.setOnAction(e -> {
                carController.setHelpButtonsVisible(true);
                returnFocusToCanvas();
            });

            hideHelpButton.setOnAction(e -> {
                carController.setHelpButtonsVisible(false);
                returnFocusToCanvas();
            });
        } else {
            // Disable buttons if car controller is not available
            showHelpButton.setDisable(true);
            hideHelpButton.setDisable(true);
        }

        // Create horizontal boxes for the buttons
        HBox algorithmButtonBox = new HBox(10);
        algorithmButtonBox.setAlignment(Pos.CENTER);
        algorithmButtonBox.getChildren().addAll(dijkstraButton, bfsButton);

        HBox helpButtonBox = new HBox(10);
        helpButtonBox.setAlignment(Pos.CENTER);
        helpButtonBox.getChildren().addAll(showHelpButton, hideHelpButton);

        // Add components to the panel
        algorithmPanel.getChildren().addAll(titleLabel, algorithmButtonBox, helpButtonBox);
        algorithmPanel.setAlignment(Pos.CENTER);

        // Position the panel in the bottom-left corner
        algorithmPanel.setTranslateX(20);
        algorithmPanel.setTranslateY(-20);
    }

    private Button createButton(String text, boolean isActive) {
        Button button = new Button(text);
        button.setPrefWidth(100);

        if (isActive) {
            button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        } else {
            button.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: black;");
        }

        return button;
    }


    public VBox getAlgorithmPanel() {
        return algorithmPanel;
    }

    public Label getPointsLabel() {
        return pointsLabel;
    }

    public static void addPoints(int amount) {
        points += amount;
        updatePointsDisplay();
        System.out.println("Points increased by " + amount + ". Total points: " + points);
    }

    private static void updatePointsDisplay() {
        // This needs to be called on the JavaFX application thread
        javafx.application.Platform.runLater(() -> {
            // Update all instances of AlgorithmController
            // Since we're using a static points variable, we need to update all instances
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window instanceof javafx.stage.Stage) {
                    javafx.scene.Scene scene = ((javafx.stage.Stage) window).getScene();
                    if (scene != null) {
                        // Find all labels with the points-label class
                        scene.getRoot().lookupAll(".points-label").forEach(node -> {
                            if (node instanceof Label) {
                                ((Label) node).setText("Points: " + points);
                            }
                        });
                    }
                }
            }
        });
    }

    private void returnFocusToCanvas() {
        if (canvas != null) {
            canvas.requestFocus();
            System.out.println("Focus returned to canvas for WASD movement");
        }
    }

    private void showIncidentStatistics() {
        if (incidentTree == null) {
            System.out.println("Incident tree is not initialized");
            return;
        }

        int totalIncidents = incidentTree.countIncidents();
        Map<TypeIncident, Integer> incidentsByType = incidentTree.countIncidentsByType();

        StringBuilder message = new StringBuilder();
        message.append("Total Incidents: ").append(totalIncidents).append("\n\n");
        message.append("Incidents by Type:\n");

        for (TypeIncident type : TypeIncident.values()) {
            int count = incidentsByType.getOrDefault(type, 0);
            message.append("- ").append(type).append(": ").append(count).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Incident Statistics");
        alert.setHeaderText("Current Incident Statistics");
        alert.setContentText(message.toString());
        alert.showAndWait();

        System.out.println("Showing incident statistics: " + message);

        // Update the button text after showing the alert
        updateIncidentStatsButton();
    }

    /**
     * Updates the incident statistics button text with current incident information
     */
    private void updateIncidentStatsButton() {
        if (incidentTree == null || incidentStatsButton == null) {
            return;
        }

        int totalIncidents = incidentTree.countIncidents();
        Map<TypeIncident, Integer> incidentsByType = incidentTree.countIncidentsByType();

        StringBuilder buttonText = new StringBuilder();
        buttonText.append("Incidents: ").append(totalIncidents).append("\n");

        for (TypeIncident type : TypeIncident.values()) {
            int count = incidentsByType.getOrDefault(type, 0);
            buttonText.append(type).append(": ").append(count).append("\n");
        }

        // Update the button text on the JavaFX application thread
        javafx.application.Platform.runLater(() -> {
            incidentStatsButton.setText(buttonText.toString());
        });
    }

    public Button getIncidentStatsButton() {
        return incidentStatsButton;
    }


    public Button getShowHelpButton() {
        return showHelpButton;
    }

    public Button getHideHelpButton() {
        return hideHelpButton;
    }

    public void setCarController(CarController carController) {
        this.carController = carController;

        // Update button handlers if they were previously disabled
        if (carController != null && (showHelpButton.isDisabled() || hideHelpButton.isDisabled())) {
            showHelpButton.setDisable(false);
            hideHelpButton.setDisable(false);

            showHelpButton.setOnAction(e -> {
                carController.setHelpButtonsVisible(true);
                returnFocusToCanvas();
            });

            hideHelpButton.setOnAction(e -> {
                carController.setHelpButtonsVisible(false);
                returnFocusToCanvas();
            });
        }

        // Update the incident stats button text
        updateIncidentStatsButton();
    }
}
