package integrative.task.control;

import integrative.task.model.*;
import integrative.task.structures.BinaryTreeIncident;
import integrative.task.structures.NodeGraph;
import integrative.task.structures.Graph;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Pane;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;

public class CarController {
    private static CarController instance;
    private final List<AutomataCar> cars; // Visual car representations
    private final List<Car> carThreads; // Car threads for autonomous movement
    private final Graph<Intersection> cityGraph;
    private final List<ObserverCars> observers;

    // Police cars
    private List<PoliceCar> policeCars;
    private List<AutomataPoliceCar> automataPoliceCars;
    private List<Thread> policeCarThreads;

    // Ambulance and fire truck
    private List<AmbulanceThread> ambulanceThreads;
    private List<FireTruckThread> fireTruckThreads;
    // Lista de botones de colisión activos (para no duplicar botones)
    private List<Button> activeCollisionButtons = new ArrayList<>();

    // Collision tracking
    private Map<NodeGraph<Intersection>, Button> helpButtons;
    private Map<NodeGraph<Intersection>, List<Car>> collisions;
    private Pane overlayPane; // Pane for displaying buttons over the canvas

    // Flag to track if help buttons are visible
    private boolean helpButtonsVisible = true;

    // Map position tracking
    private double mapX = 0;
    private double mapY = 0;

    // List of intersections for creating police cars
    private List<Intersection> intersectionList;

    // Thread for collision detection
    private Thread collisionDetectionThread;
    // Thread for checking completed police car missions
    private Thread missionCheckThread;
    private boolean running = true;

    private BinaryTreeIncident incidentTree;

    // Available colors for cars
    private static final String[] CAR_COLORS = {
        "red", "green", "yellow", "purple", "cyan"
    };

    private CarController(Graph<Intersection> cityGraph, BinaryTreeIncident incidentTree) {
        this.cars = new ArrayList<>();
        this.carThreads = new ArrayList<>(); // Initialize car threads list
        this.cityGraph = cityGraph;
        this.incidentTree = incidentTree;
        this.observers = new ArrayList<>();

        // Initialize police car lists
        this.policeCars = new ArrayList<>();
        this.automataPoliceCars = new ArrayList<>();
        this.policeCarThreads = new ArrayList<>();

        // Initialize ambulance and fire truck lists
        this.ambulanceThreads = new ArrayList<>();
        this.fireTruckThreads = new ArrayList<>();

        // Initialize collision tracking
        this.helpButtons = new HashMap<>();
        this.collisions = new HashMap<>();

        // Initialize intersection list
        this.intersectionList = new ArrayList<>();

    }

    public static CarController getInstance(Graph<Intersection> cityGraph, BinaryTreeIncident incidentTree) {
        if (instance == null) {
            instance = new CarController(cityGraph, incidentTree);
        }
        return instance;
    }

    public void addObserver(ObserverCars observer) {
        observers.add(observer);
    }

    public void removeObserver(ObserverCars observer) {
        observers.remove(observer);
    }

    private void notifyObservers(AutomataCar car, String message) {
        for (ObserverCars observer : observers) {
            observer.onCarEvent(car, message);
        }
    }

    public Car createAndStartCarThread(AutomataCar car, Intersection startIntersection, 
                                      String carName, String color) {
        // Add the car to the visual list
        cars.add(car);

        // Create a new Car thread
        Car carThread = new Car(car, cityGraph, startIntersection, carName, color);
        carThreads.add(carThread);

        // Start the thread
        Thread thread = new Thread(carThread);
        thread.start();

        notifyObservers(car, "Car thread " + carName + " started at intersection " + startIntersection.getName());

        // Start collision detection thread if it's not already running
        startCollisionDetection();

        return carThread;
    }

    public PoliceCar createAndInitializePoliceCar(Intersection startIntersection) {
        // Create the visual representation of the police car
        AutomataPoliceCar automataPoliceCar = new AutomataPoliceCar("PoliceCar" + (policeCars.size() + 1));
        automataPoliceCar.setWidthImage(35);
        automataPoliceCar.setHeightImage(32);

        // Add the police car to the visual list
        cars.add(automataPoliceCar);
        automataPoliceCars.add(automataPoliceCar);

        // Create the police car thread
        PoliceCar policeCar = new PoliceCar(automataPoliceCar, cityGraph, startIntersection, "PoliceCar" + (policeCars.size() + 1));
        policeCars.add(policeCar);

        // Start the police car thread
        Thread policeCarThread = new Thread(policeCar);
        policeCarThreads.add(policeCarThread);
        policeCarThread.start();

        System.out.println("Police car initialized and stationed at " + startIntersection.getName());

        // Start collision detection thread if it's not already running
        startCollisionDetection();

        return policeCar;
    }

    public void setOverlayPane(Pane overlayPane) {
        this.overlayPane = overlayPane;
    }

    public void setIntersectionList(List<Intersection> intersectionList) {
        this.intersectionList = new ArrayList<>(intersectionList);
    }

    public void updateMapPosition(double mapX, double mapY) {
        this.mapX = mapX;
        this.mapY = mapY;

        // Update positions of all existing HELP buttons
        updateHelpButtonPositions();
    }

    private void updateHelpButtonPositions() {
        if (overlayPane == null || helpButtons.isEmpty()) {
            return;
        }

        // Get the canvas dimensions for centering
        double canvasWidth = 0;
        double canvasHeight = 0;

        // Find the Canvas in the StackPane children
        for (javafx.scene.Node node : overlayPane.getChildren()) {
            if (node instanceof javafx.scene.canvas.Canvas) {
                canvasWidth = ((javafx.scene.canvas.Canvas) node).getWidth();
                canvasHeight = ((javafx.scene.canvas.Canvas) node).getHeight();
                break;
            }
        }

        // Update each button's position
        double zoomLevel = 1.0; // Match the MapController's ZOOM_LEVEL
        for (Map.Entry<NodeGraph<Intersection>, Button> entry : helpButtons.entrySet()) {
            NodeGraph<Intersection> collisionNodeGraph = entry.getKey();
            Button helpButton = entry.getValue();

            // Calculate the new position based on the current map position
            double newX = (collisionNodeGraph.getX() * zoomLevel) + mapX - (canvasWidth / 2);
            double newY = (collisionNodeGraph.getY() * zoomLevel) + mapY - (canvasHeight / 2);

            // Update the button's position
            helpButton.setTranslateX(newX);
            helpButton.setTranslateY(newY);

            // Set visibility based on the flag
            helpButton.setVisible(helpButtonsVisible);
        }
    }

    public void setHelpButtonsVisible(boolean visible) {
        this.helpButtonsVisible = visible;
        updateHelpButtonPositions();
    }

    public boolean areHelpButtonsVisible() {
        return helpButtonsVisible;
    }

    public void createReplacementCars(Car car1, Car car2) {
        if (intersectionList.size() < 2) {
            System.out.println("Not enough intersections to create replacement cars");
            return;
        }

        // Remove the collided cars from the lists
        cars.remove(car1.getAutomataCar());
        cars.remove(car2.getAutomataCar());
        carThreads.remove(car1);
        carThreads.remove(car2);

        // Create two new cars at random intersections
        Random random = new Random();

        for (int i = 0; i < 2; i++) {
            // Choose a random starting intersection
            int startIdx = random.nextInt(intersectionList.size());
            Intersection startIntersection = intersectionList.get(startIdx);

            // Create a new car
            AutomataCar car = new AutomataCar("Car" + (carThreads.size() + 1));

            // Set initial size
            car.setWidthImage(10);
            car.setHeightImage(18);

            // Create and start the car thread
            createAndStartCarThread(
                car,
                startIntersection,
                "Car" + (carThreads.size() + 1),
                CAR_COLORS[random.nextInt(CAR_COLORS.length)]
            );

            System.out.println("Created replacement car starting at " + startIntersection.getName());
        }
    }

    private void startCollisionDetection() {
        if (collisionDetectionThread == null || !collisionDetectionThread.isAlive()) {
            running = true;
            collisionDetectionThread = new Thread(this::detectCollisions);
            collisionDetectionThread.setDaemon(true);
            collisionDetectionThread.start();
            System.out.println("Collision detection thread started");
        }

        // Also start the mission check thread if it's not already running
        if (missionCheckThread == null || !missionCheckThread.isAlive()) {
            missionCheckThread = new Thread(this::checkCompletedMissions);
            missionCheckThread.setDaemon(true);
            missionCheckThread.start();
            System.out.println("Mission check thread started");
        }
    }

    private void checkCompletedMissions() {
        while (running) {
            // Check police cars
            // Create a copy of the list to avoid ConcurrentModificationException
            List<PoliceCar> policeCarsCopy = new ArrayList<>(policeCars);

            for (PoliceCar policeCar : policeCarsCopy) {
                if (policeCar.hasMissionCompleted()) {
                    System.out.println("Police car " + policeCar.getName() + " has completed its mission");

                    // Get the collided cars
                    Car[] collidedCars = policeCar.getCollidedCars();
                    if (collidedCars != null && collidedCars.length == 2) {
                        Car car1 = collidedCars[0];
                        Car car2 = collidedCars[1];

                        // Create replacement cars
                        Platform.runLater(() -> {
                            // Remove the police car from the lists
                            cars.remove(policeCar.getAutomataPoliceCar());
                            automataPoliceCars.remove(policeCar.getAutomataPoliceCar());
                            policeCars.remove(policeCar);

                            // Create replacement cars for the collided cars
                            createReplacementCars(car1, car2);
                        });
                    }
                }
            }

            // Check ambulances
            // Create a copy of the list to avoid ConcurrentModificationException
            List<AmbulanceThread> ambulanceThreadsCopy = new ArrayList<>(ambulanceThreads);

            for (AmbulanceThread ambulanceThread : ambulanceThreadsCopy) {
                if (ambulanceThread.hasMissionCompleted()) {
                    System.out.println("Ambulance " + ambulanceThread.getName() + " has completed its mission");

                    // Remove the ambulance from the lists
                    Platform.runLater(() -> {
                        cars.remove(ambulanceThread.getAmbulanceCar());
                        ambulanceThreads.remove(ambulanceThread);
                    });
                }
            }

            // Check fire trucks
            // Create a copy of the list to avoid ConcurrentModificationException
            List<FireTruckThread> fireTruckThreadsCopy = new ArrayList<>(fireTruckThreads);

            for (FireTruckThread fireTruckThread : fireTruckThreadsCopy) {
                if (fireTruckThread.hasMissionCompleted()) {
                    System.out.println("Fire truck " + fireTruckThread.getName() + " has completed its mission");

                    // Remove the fire truck from the lists
                    Platform.runLater(() -> {
                        cars.remove(fireTruckThread.getFireTruckCar());
                        fireTruckThreads.remove(fireTruckThread);
                    });
                }
            }

            // Sleep to avoid high CPU usage
            try {
                Thread.sleep(100); // Check for completed missions every 100ms
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    private void detectCollisions() {
        System.out.println("Collision detection thread started");

        while (running) {
            try {
                // Crear copia de la lista para evitar ConcurrentModificationException
                List<Car> carsCopy = new ArrayList<>(carThreads);

                // Check for collisions between all pairs of cars
                for (int i = 0; i < carsCopy.size(); i++) {
                    Car car1 = carsCopy.get(i);
                    if (car1.hasCollided()) continue;

                    NodeGraph<Intersection> nodeGraph1 = car1.getCurrentNode();
                    if (nodeGraph1 == null) continue;

                    for (int j = i + 1; j < carsCopy.size(); j++) {
                        Car car2 = carsCopy.get(j);
                        if (car2.hasCollided()) continue;

                        NodeGraph<Intersection> nodeGraph2 = car2.getCurrentNode();
                        if (nodeGraph2 == null) continue;

                        // Check if cars are at the same node
                        if (nodeGraph1.equals(nodeGraph2)) {
                            System.out.println("COLLISION DETECTED between " + car1.getName() +
                                    " and " + car2.getName() + " at " + nodeGraph1.getData().getName());

                            // Mark cars as collided
                            car1.markAsCollided(car2);
                            car2.markAsCollided(car1);

                            // Create incident in JavaFX thread
                            Platform.runLater(() -> {
                                try {
                                    Incident incident = new Incident(Priority.LOW, TypeIncident.ACCIDENT, LocalDateTime.now());
                                    incidentTree.insert(incident);
                                    System.out.println("Accident incident created: " + incident);

                                    // Create HELP button
                                    createHelpButton(nodeGraph1, car1, car2);
                                } catch (Exception e) {
                                    System.err.println("Error creating accident incident: " + e.getMessage());
                                }
                            });
                        }
                    }
                }

                Thread.sleep(500); // Reducir frecuencia para mejor performance

            } catch (InterruptedException e) {
                System.out.println("Collision detection interrupted");
                running = false;
            } catch (Exception e) {
                System.err.println("Error in collision detection: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Collision detection thread stopped");
    }

    private void createHelpButton(NodeGraph<Intersection> collisionNodeGraph, Car car1, Car car2) {
        if (overlayPane == null) {
            System.out.println("Overlay pane not set, cannot create HELP button");
            return;
        }

        // Check if a button already exists for this collision
        if (helpButtons.containsKey(collisionNodeGraph)) {
            System.out.println("HELP button already exists for collision at " + collisionNodeGraph.getData().getName());
            return;
        }

        // Create a list of cars involved in this collision
        List<Car> collidedCars = new ArrayList<>();
        collidedCars.add(car1);
        collidedCars.add(car2);
        collisions.put(collisionNodeGraph, collidedCars);

        // Create the HELP button
        Button helpButton = new Button("HELP");
        helpButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;");

        double zoomLevel = 1.0;

        // Get the canvas dimensions for centering (if available)
        double canvasWidth = 0;
        double canvasHeight = 0;

        // Find the Canvas in the StackPane children
        for (javafx.scene.Node node : overlayPane.getChildren()) {
            if (node instanceof javafx.scene.canvas.Canvas) {
                canvasWidth = ((javafx.scene.canvas.Canvas) node).getWidth();
                canvasHeight = ((javafx.scene.canvas.Canvas) node).getHeight();
                break;
            }
        }

        // Position the button, accounting for StackPane centering behavior
        helpButton.setTranslateX((collisionNodeGraph.getX() * zoomLevel) + mapX - (canvasWidth / 2));
        helpButton.setTranslateY((collisionNodeGraph.getY() * zoomLevel) + mapY - (canvasHeight / 2));

        // Add debug output to verify button position
        System.out.println("Positioning HELP button at: " + 
                          ((collisionNodeGraph.getX() * zoomLevel) + mapX - (canvasWidth / 2)) + ", " +
                          ((collisionNodeGraph.getY() * zoomLevel) + mapY - (canvasHeight / 2)) +
                          " (node: " + collisionNodeGraph.getX() + ", " + collisionNodeGraph.getY() +
                          ", map: " + mapX + ", " + mapY + 
                          ", canvas: " + canvasWidth + "x" + canvasHeight +
                          ", zoom: " + zoomLevel + ")");

        // Add action to the button
        helpButton.setOnAction(event -> {
            // Remove the button when clicked
            overlayPane.getChildren().remove(helpButton);
            helpButtons.remove(collisionNodeGraph);

            // Create an incident object for the accident
            Incident incident = new Incident(Priority.LOW, TypeIncident.ACCIDENT, LocalDateTime.now());

            // Show vehicle selection dialog
            showVehicleSelectionDialog(collisionNodeGraph, incident);

            // Store the collided cars for later use
            storeCollidedCars(collisionNodeGraph, car1, car2);
        });

        // Add the button to the overlay pane
        overlayPane.getChildren().add(helpButton);
        helpButtons.put(collisionNodeGraph, helpButton);

        System.out.println("HELP button created for collision at " + collisionNodeGraph.getData().getName());
    }


    private void storeCollidedCars(NodeGraph<Intersection> nodeGraph, Car car1, Car car2) {
        // Store the collided cars in the collisions map
        List<Car> collidedCars = new ArrayList<>();
        collidedCars.add(car1);
        collidedCars.add(car2);
        collisions.put(nodeGraph, collidedCars);
    }

    public void createBombHelpButton(NodeGraph<Intersection> bombNodeGraph, Incident bombIncident) {
        if (overlayPane == null) {
            System.out.println("Overlay pane not set, cannot create HELP button for bomb");
            return;
        }

        // Check if a button already exists for this bomb
        if (helpButtons.containsKey(bombNodeGraph)) {
            System.out.println("HELP button already exists for bomb at " + bombNodeGraph.getData().getName());
            return;
        }

        // Create the HELP button
        Button helpButton = new Button("HELP");
        helpButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;");

        // Position the button over the bomb, accounting for map position and zoom level
        double zoomLevel = 1.0;

        // Get the canvas dimensions for centering (if available)
        double canvasWidth = 0;
        double canvasHeight = 0;

        // Find the Canvas in the StackPane children
        for (javafx.scene.Node node : overlayPane.getChildren()) {
            if (node instanceof javafx.scene.canvas.Canvas) {
                canvasWidth = ((javafx.scene.canvas.Canvas) node).getWidth();
                canvasHeight = ((javafx.scene.canvas.Canvas) node).getHeight();
                break;
            }
        }

        // Position the button, accounting for StackPane centering behavior
        helpButton.setTranslateX((bombNodeGraph.getX() * zoomLevel) + mapX - (canvasWidth / 2));
        helpButton.setTranslateY((bombNodeGraph.getY() * zoomLevel) + mapY - (canvasHeight / 2));

        System.out.println("Positioning HELP button for bomb at: " + 
                          ((bombNodeGraph.getX() * zoomLevel) + mapX - (canvasWidth / 2)) + ", " +
                          ((bombNodeGraph.getY() * zoomLevel) + mapY - (canvasHeight / 2)));

        // Add action to the button
        helpButton.setOnAction(event -> {
            // Remove the button when clicked
            overlayPane.getChildren().remove(helpButton);
            helpButtons.remove(bombNodeGraph);

            // Show vehicle selection dialog
            showVehicleSelectionDialog(bombNodeGraph, bombIncident);
        });

        // Add the button to the overlay pane
        overlayPane.getChildren().add(helpButton);
        helpButtons.put(bombNodeGraph, helpButton);

        System.out.println("HELP button created for bomb at " + bombNodeGraph.getData().getName());
    }

    /**
     * Creates a help button for a fire at the specified node
     * @param fireNodeGraph The node where the fire is located
     * @param fireIncident The fire incident
     */
    public void createFireHelpButton(NodeGraph<Intersection> fireNodeGraph, Incident fireIncident) {
        if (overlayPane == null) {
            System.out.println("Overlay pane not set, cannot create HELP button for fire");
            return;
        }

        // Check if a button already exists for this fire
        if (helpButtons.containsKey(fireNodeGraph)) {
            System.out.println("HELP button already exists for fire at " + fireNodeGraph.getData().getName());
            return;
        }

        // Create the HELP button
        Button helpButton = new Button("HELP");
        helpButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;");

        // Position the button over the fire, accounting for map position and zoom level
        double zoomLevel = 1.0;

        // Get the canvas dimensions for centering (if available)
        double canvasWidth = 0;
        double canvasHeight = 0;

        // Find the Canvas in the StackPane children
        for (javafx.scene.Node node : overlayPane.getChildren()) {
            if (node instanceof javafx.scene.canvas.Canvas) {
                canvasWidth = ((javafx.scene.canvas.Canvas) node).getWidth();
                canvasHeight = ((javafx.scene.canvas.Canvas) node).getHeight();
                break;
            }
        }

        // Position the button, accounting for StackPane centering behavior
        helpButton.setTranslateX((fireNodeGraph.getX() * zoomLevel) + mapX - (canvasWidth / 2));
        helpButton.setTranslateY((fireNodeGraph.getY() * zoomLevel) + mapY - (canvasHeight / 2));

        System.out.println("Positioning HELP button for fire at: " + 
                          ((fireNodeGraph.getX() * zoomLevel) + mapX - (canvasWidth / 2)) + ", " +
                          ((fireNodeGraph.getY() * zoomLevel) + mapY - (canvasHeight / 2)));

        // Add action to the button
        helpButton.setOnAction(event -> {
            // Remove the button when clicked
            overlayPane.getChildren().remove(helpButton);
            helpButtons.remove(fireNodeGraph);

            // Show vehicle selection dialog
            showVehicleSelectionDialog(fireNodeGraph, fireIncident);
        });

        // Add the button to the overlay pane
        overlayPane.getChildren().add(helpButton);
        helpButtons.put(fireNodeGraph, helpButton);

        System.out.println("HELP button created for fire at " + fireNodeGraph.getData().getName());
    }

    private void showVehicleSelectionDialog(NodeGraph<Intersection> nodeGraph, Incident incident) {
        System.out.println("[DEBUG] Showing vehicle selection dialog");
        System.out.println("[DEBUG] Incident type: " + incident.getTypeIncident());
        System.out.println("[DEBUG] Node graph: " + nodeGraph);

        // Create a dialog for vehicle selection
        javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Select Emergency Vehicle");
        dialog.setHeaderText("Choose the appropriate emergency vehicle for this incident");

        // Set the button types
        ButtonType policeButtonType = new ButtonType("Police Car", ButtonBar.ButtonData.LEFT);
        ButtonType ambulanceButtonType = new ButtonType("Ambulance", ButtonBar.ButtonData.OTHER);
        ButtonType firetruckButtonType = new ButtonType("Fire Truck", ButtonBar.ButtonData.RIGHT);

        dialog.getDialogPane().getButtonTypes().addAll(policeButtonType, ambulanceButtonType, firetruckButtonType);

        System.out.println("[DEBUG] Dialog created with button types: Police Car, Ambulance, Fire Truck");

        // Show the dialog and wait for a response
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent()) {
            ButtonType buttonType = result.get();
            String vehicleType = "";

            if (buttonType == policeButtonType) {
                vehicleType = "police";
                System.out.println("[DEBUG] Police Car selected");
            } else if (buttonType == ambulanceButtonType) {
                vehicleType = "ambulance";
                System.out.println("[DEBUG] Ambulance selected");
            } else if (buttonType == firetruckButtonType) {
                vehicleType = "firetruck";
                System.out.println("[DEBUG] Fire Truck selected");
            }

            System.out.println("[DEBUG] Vehicle type selected: " + vehicleType);

            if (!vehicleType.isEmpty()) {
                System.out.println("[DEBUG] Dispatching emergency vehicle of type: " + vehicleType);
                dispatchEmergencyVehicle(nodeGraph, incident, vehicleType);
            } else {
                System.out.println("[DEBUG] No vehicle type selected");
            }
        } else {
            System.out.println("[DEBUG] Dialog closed without selection");
        }
    }

    private void dispatchEmergencyVehicle(NodeGraph<Intersection> nodeGraph, Incident incident, String vehicleType) {
        // Create a new emergency vehicle at the first intersection
        if (!intersectionList.isEmpty()) {
            Intersection startIntersection = intersectionList.get(0);
            System.out.println("Creating " + vehicleType + " at intersection: " + startIntersection.getName());

            // Check if the correct vehicle was selected for the incident type
            boolean correctVehicle = false;

            switch (incident.getTypeIncident()) {
                case ACCIDENT:
                    correctVehicle = vehicleType.equals("ambulance");
                    break;
                case FIRE:
                    correctVehicle = vehicleType.equals("firetruck");
                    break;
                case BOMBS:
                    correctVehicle = vehicleType.equals("police");
                    break;
            }

            // Award points only if the correct vehicle was selected
            if (correctVehicle) {
                AlgorithmController.addPoints(10);
                System.out.println("Correct vehicle selected! +10 points");
            } else {
                System.out.println("Incorrect vehicle selected. No points awarded.");
            }

            // Create the appropriate vehicle type based on selection
            if (vehicleType.equals("ambulance")) {
                // Create an ambulance
                AmbulanceCar ambulanceCar = new AmbulanceCar("Ambulance" + (policeCars.size() + 1));
                ambulanceCar.setWidthImage(35);
                ambulanceCar.setHeightImage(32);

                // Add the ambulance to the visual list
                cars.add(ambulanceCar);

                // Create the ambulance thread
                AmbulanceThread ambulanceThread = new AmbulanceThread(
                    ambulanceCar, 
                    cityGraph, 
                    startIntersection, 
                    "Ambulance" + (policeCars.size() + 1)
                );

                // Add to the ambulance threads list
                ambulanceThreads.add(ambulanceThread);

                // Start the ambulance thread
                Thread thread = new Thread(ambulanceThread);
                thread.start();

                // Set the destination
                ambulanceThread.setDestination(nodeGraph);

                System.out.println("Dispatching ambulance to incident at " + nodeGraph.getData().getName());

            } else if (vehicleType.equals("firetruck")) {
                // Create a fire truck
                FireTruckCar fireTruckCar = new FireTruckCar("FireTruck" + (policeCars.size() + 1));
                fireTruckCar.setWidthImage(35);
                fireTruckCar.setHeightImage(32);

                // Add the fire truck to the visual list
                cars.add(fireTruckCar);

                // Create the fire truck thread
                FireTruckThread fireTruckThread = new FireTruckThread(
                    fireTruckCar, 
                    cityGraph, 
                    startIntersection, 
                    "FireTruck" + (policeCars.size() + 1)
                );

                // Add to the fire truck threads list
                fireTruckThreads.add(fireTruckThread);

                // Start the fire truck thread
                Thread thread = new Thread(fireTruckThread);
                thread.start();

                // Set the destination
                fireTruckThread.setDestination(nodeGraph);

                System.out.println("Dispatching fire truck to incident at " + nodeGraph.getData().getName());

            } else {
                // Create a police car (default)
                System.out.println("[DEBUG] Creating police car for vehicle type: " + vehicleType);
                PoliceCar policeCar = createAndInitializePoliceCar(startIntersection);

                // Set the destination
                policeCar.setDestination(nodeGraph);

                // Debug output to verify police car creation
                System.out.println("[DEBUG] Police car created: " + policeCar);
                System.out.println("[DEBUG] AutomataPoliceCar: " + policeCar.getAutomataPoliceCar());
                System.out.println("[DEBUG] Current position: (" + policeCar.getAutomataPoliceCar().getxImage() + ", " + policeCar.getAutomataPoliceCar().getyImage() + ")");
                System.out.println("[DEBUG] Image: " + policeCar.getAutomataPoliceCar().getImage());

                System.out.println("Dispatching police car to incident at " + nodeGraph.getData().getName());
            }
        } else {
            System.out.println("No intersections available to create emergency vehicle");
        }
    }

    private void dispatchPoliceCar(NodeGraph<Intersection> collisionNodeGraph, Car car1, Car car2) {
        // Create a new police car at the first intersection
        if (!intersectionList.isEmpty()) {
            Intersection startIntersection = intersectionList.get(0);
            System.out.println("Creating police car at intersection: " + startIntersection.getName());
            System.out.println("Collision node: " + collisionNodeGraph.getData().getName() + " at position (" + collisionNodeGraph.getX() + ", " + collisionNodeGraph.getY() + ")");

            PoliceCar policeCar = createAndInitializePoliceCar(startIntersection);

            // Set the collided cars and destination
            policeCar.setCollidedCars(car1, car2);
            policeCar.setDestination(collisionNodeGraph);

            System.out.println("Dispatching police car to collision at " + collisionNodeGraph.getData().getName());
        } else {
            System.out.println("No intersections available to create police car");
        }
    }

    public List<AutomataCar> getCars() {
        return cars;
    }

    public List<Car> getCarThreads() {
        return carThreads;
    }

    public void shutdown() {
        // Stop collision detection and mission check
        running = false;
        if (collisionDetectionThread != null) {
            collisionDetectionThread.interrupt();
        }
        if (missionCheckThread != null) {
            missionCheckThread.interrupt();
        }

        // Stop all visual car animations
        for (AutomataCar car : cars) {
            car.stop();
        }

        // Stop all car threads
        for (Car carThread : carThreads) {
            carThread.stop();
        }

        // Stop all police car threads
        for (PoliceCar policeCar : policeCars) {
            policeCar.stop();
        }

        // Interrupt all police car threads
        for (Thread policeCarThread : policeCarThreads) {
            policeCarThread.interrupt();
        }

        System.out.println("All police car threads stopped");
        System.out.println("Mission check thread stopped");
        System.out.println("All cars and car threads stopped");
    }
}
