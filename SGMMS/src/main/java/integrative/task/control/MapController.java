package integrative.task.control;

import integrative.task.model.*;
import integrative.task.structures.BinaryTreeIncident;
import integrative.task.structures.Graph;
import integrative.task.structures.NodeGraph;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MapController {
    @FXML
    private Canvas cDraw;
    @FXML
    private StackPane spWindow;

    private GraphicsContext gc;
    private Image mapImage;
    private Graph<Intersection> graph;
    private BinaryTreeIncident tree;
    private List<Intersection> intersectionList;
    private CarController carController;
    private AlgorithmController algorithmController;
    private FireController fireController;
    private BombController bombController;

    private double mapX = 0, mapY = 0; // Position of the map (camera)
    private final double MAP_SPEED = 10.0; // Speed of camera movement
    private final double ZOOM_LEVEL = 1;
    private double mapWidth, mapHeight;

    @FXML
    public void initialize() {
        intersectionList = new ArrayList<>();
        graph = new Graph<>();
        tree = new BinaryTreeIncident(Incident.getComparator());
        mapImage = new Image(getClass().getResourceAsStream("/integrative/map/mapaPNG.png"));

        mapWidth = mapImage.getWidth();
        mapHeight = mapImage.getHeight();

        initializeIntersectionsAndGraph();

        gc = cDraw.getGraphicsContext2D();
        cDraw.setFocusTraversable(true);
        cDraw.widthProperty().bind(spWindow.widthProperty());
        cDraw.heightProperty().bind(spWindow.heightProperty());
        cDraw.setOnKeyPressed(this::handleKeyPress);

        setUpAnimationTimer();
        initializeCarController();
        initializeAlgorithmController();
        initializeFireController();
        initializeBombController();
    }

    private void initializeCarController() {
        carController = CarController.getInstance(graph, tree);
        carController.setOverlayPane(spWindow);
        carController.setIntersectionList(intersectionList);

        if (intersectionList.size() >= 3) {
            // Create multiple cars with different routes
            createMultipleCars();
        }
    }

    private void initializeAlgorithmController() {
        // Create the algorithm controller with a reference to the canvas, incident tree, and car controller
        algorithmController = new AlgorithmController(cDraw, tree, carController);

        // Get the algorithm panel
        javafx.scene.Node algorithmPanel = algorithmController.getAlgorithmPanel();

        // Position the panel in the bottom-left corner
        StackPane.setAlignment(algorithmPanel, Pos.BOTTOM_LEFT);

        // Get the points label
        javafx.scene.Node pointsLabel = algorithmController.getPointsLabel();

        // Position the points label in the top-left corner
        StackPane.setAlignment(pointsLabel, Pos.TOP_LEFT);

        // Get the incident statistics button
        javafx.scene.Node incidentStatsButton = algorithmController.getIncidentStatsButton();

        // Position the incident statistics button in the bottom-right corner
        StackPane.setAlignment(incidentStatsButton, Pos.BOTTOM_RIGHT);

        // Get the show/hide help buttons
        javafx.scene.Node showHelpButton = algorithmController.getShowHelpButton();
        javafx.scene.Node hideHelpButton = algorithmController.getHideHelpButton();

        // Position the show/hide help buttons in the top-right corner
        StackPane.setAlignment(showHelpButton, Pos.TOP_RIGHT);
        showHelpButton.setTranslateY(50); // Offset from the top

        StackPane.setAlignment(hideHelpButton, Pos.TOP_RIGHT);
        hideHelpButton.setTranslateY(90); // Offset from the top and from the show button

        // Add the panel, points label, incident stats button, and show/hide help buttons to the main view
        spWindow.getChildren().addAll(algorithmPanel, pointsLabel, incidentStatsButton, 
                                     showHelpButton, hideHelpButton);

        System.out.println("Algorithm selection panel, points system, incident statistics button, and show/hide help buttons initialized");
    }

    private void initializeFireController() {
        fireController = new FireController(tree, intersectionList, carController);
        fireController.startFireThread();
    }

    private void initializeBombController() {
        bombController = new BombController(tree, intersectionList, carController);
        bombController.startBombThread();
    }

    private void createMultipleCars() {
        try {
            // Create 13 cars with different starting points for random movement
            int numCars = 13;
            String[] carColors = {"red", "blue", "green", "yellow", "purple", "cyan", "magenta", "orange", "pink", "brown", "gray", "white", "black"};

            for (int i = 0; i < numCars; i++) {
                // Create a new car
                AutomataCar car = new AutomataCar("Car" + (i + 1));

                // Set initial size
                car.setWidthImage(10);
                car.setHeightImage(18);

                // Choose a random starting intersection
                int startIdx = i % intersectionList.size();
                Intersection startIntersection = intersectionList.get(startIdx);

                // Position the car at the starting intersection
                NodeGraph<Intersection> startNodeGraph = startIntersection.getTopLeft();
                if (startNodeGraph != null) {
                    car.setPosition(startNodeGraph.getX(), startNodeGraph.getY());
                }

                // Create and start the car thread
                carController.createAndStartCarThread(
                    car,
                    startIntersection,
                    "Car" + (i + 1),
                    carColors[i % carColors.length]
                );

                System.out.println("Created Car" + (i + 1) + " starting at " +
                                  startIntersection.getName());
            }
        } catch (Exception e) {
            System.err.println("Error creating cars: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (carController != null) {
            carController.shutdown();
        }
    }

    private void initializeIntersectionsAndGraph() {
        // Parámetros ajustables
        int rows = 5;
        int cols = 10;

        double spacingInicialX = 190; // spacing entre columnas inicial
        double spacingInicialY = 190; // spacing entre filas inicial

        double factorReduccionX = -0.459;  // cuánto se reduce el spacing horizontal por cada columna
        double factorReduccionY = -1;   // cuánto se reduce el spacing vertical por cada fila

        double minSpacingX = 50; // mínimo spacing horizontal
        double minSpacingY = 50; // mínimo spacing vertical

        int startX = 60;
        int startY = 73;

        // Calcular las posiciones fijas de las columnas (X) y las filas (Y)
        int[] xPositions = new int[cols];
        int[] yPositions = new int[rows];

        // Posiciones X (columnas)
        xPositions[0] = startX;
        double currentSpacingX = spacingInicialX;
        for (int col = 1; col < cols; col++) {
            currentSpacingX = Math.max(currentSpacingX - factorReduccionX, minSpacingX);
            xPositions[col] = xPositions[col - 1] + (int) currentSpacingX;
        }

        // Posiciones Y (filas)
        yPositions[0] = startY;
        double currentSpacingY = spacingInicialY;
        for (int row = 1; row < rows; row++) {
            currentSpacingY = Math.max(currentSpacingY - factorReduccionY, minSpacingY);
            yPositions[row] = yPositions[row - 1] + (int) currentSpacingY;
        }

        // Crear matriz de intersecciones
        Intersection[][] grid = new Intersection[rows][cols];

        // Crear todas las intersecciones con las posiciones pre-calculadas
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String name = "I" + (row * cols + col + 1);
                int x = xPositions[col];
                int y = yPositions[row];
                grid[row][col] = createIntersection(name, x, y);
            }
        }

        // Conectar las intersecciones
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Intersection current = grid[row][col];

                // Conectar a la derecha
                if (col < cols - 1) {
                    Intersection right = grid[row][col + 1];
                    connectIntersections(current, right, "right");
                }

                // Conectar hacia abajo
                if (row < rows - 1) {
                    Intersection down = grid[row + 1][col];
                    connectIntersections(current, down, "down");
                }

                // Conectar a la izquierda
                if (col > 0) {
                    Intersection left = grid[row][col - 1];
                    connectIntersections(current, left, "left");
                }

                // Conectar hacia arriba
                if (row > 0) {
                    Intersection up = grid[row - 1][col];
                    connectIntersections(current, up, "up");
                }
            }
        }
    }

    private void connectIntersections(Intersection from, Intersection to, String direction) {
        switch (direction) {
            case "right" -> graph.connect(from.getBottomRight(), "right", to.getBottomLeft());
            case "down" -> graph.connect(from.getBottomLeft(), "down", to.getTopLeft());
            case "left" -> graph.connect(from.getTopLeft(), "left", to.getTopRight());
            case "up" -> graph.connect(from.getTopRight(), "up", to.getBottomRight());
        }
    }

    private Intersection createIntersection(String name, double x, double y) {
        Intersection intersection = new Intersection(name);
        intersection.defineReferences(x, y, 30, 30, graph);
        intersectionList.add(intersection);
        return intersection;
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();
        switch (code) {
            case W -> mapY += MAP_SPEED;
            case A -> mapX += MAP_SPEED;
            case S -> mapY -= MAP_SPEED;
            case D -> mapX -= MAP_SPEED;
        }
        applyBoundaryConstraints();

        // Update car controller with current map position
        if (carController != null) {
            carController.updateMapPosition(mapX, mapY);
        }
    }

    private void applyBoundaryConstraints() {
        double effectiveWidth = mapWidth * ZOOM_LEVEL;
        double effectiveHeight = mapHeight * ZOOM_LEVEL;

        double minX = -(effectiveWidth - cDraw.getWidth());
        double minY = -(effectiveHeight - cDraw.getHeight());

        if (mapX > 0) mapX = 0;
        if (mapY > 0) mapY = 0;
        if (mapX < minX) mapX = minX;
        if (mapY < minY) mapY = minY;
    }

    private void setUpAnimationTimer() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, cDraw.getWidth(), cDraw.getHeight());
                drawMap();
                drawIntersections();
                drawEmergencyPaths(); // Draw emergency vehicle paths first (under the vehicles)
                drawCar();
                drawFire();
                drawBomb();

                // Update car controller with current map position
                if (carController != null) {
                    carController.updateMapPosition(mapX, mapY);
                }
            }
        };
        timer.start();
    }

    private void drawMap() {
        gc.drawImage(mapImage, 0, 0, mapWidth, mapHeight,
                mapX, mapY,
                mapWidth * ZOOM_LEVEL, mapHeight * ZOOM_LEVEL);
    }

    private void drawIntersections() {
        for (Intersection intersection : intersectionList) {
            intersection.draw(gc, mapX, mapY);
        }
    }

    private void drawCar() {
        // Draw all cars from the car controller (including the police car)
        if (carController != null) {
            List<AutomataCar> cars = carController.getCars();
            System.out.println("[DEBUG] Drawing " + cars.size() + " cars");

            // Count police cars
            int policeCarCount = 0;
            for (AutomataCar car : cars) {
                if (car instanceof AutomataPoliceCar) {
                    policeCarCount++;
                }
            }
            System.out.println("[DEBUG] Found " + policeCarCount + " police cars in the list");

            for (AutomataCar car : cars) {
                double x = (car.getxImage() * ZOOM_LEVEL) + mapX;
                double y = (car.getyImage() * ZOOM_LEVEL) + mapY;
                double width = car.getWidthImage() * ZOOM_LEVEL;
                double height = car.getHeightImage() * ZOOM_LEVEL;

                // Add debug output to verify police car position
                if (car instanceof AutomataPoliceCar) {
                    System.out.println("[DEBUG] Drawing police car at: " + x + ", " + y);
                    System.out.println("[DEBUG] Police car image: " + car.getImage());
                    System.out.println("[DEBUG] Police car dimensions: " + width + "x" + height);
                }

                gc.drawImage(car.getImage(), x, y, width, height);
            }
        } else {
            System.out.println("[DEBUG] CarController is null, cannot draw cars");
        }
    }

    private void drawFire() {
        if (fireController != null) {
            List<Fire> fires = fireController.getFires();

            // Debug: mostrar cuántos fuegos hay
            if (!fires.isEmpty()) {
                System.out.println("Drawing " + fires.size() + " fires");
            }

            for (Fire fire : fires) {
                try {
                    // Verificar que el fuego tenga imagen
                    if (fire.getImage() == null) {
                        System.err.println("Fire image is null!");
                        continue;
                    }

                    int currentFrame = fire.getCurrentFrame();
                    Rectangle[] animationFrames = fire.getAnimationFire();

                    if (animationFrames != null && currentFrame < animationFrames.length) {
                        Rectangle frame = animationFrames[currentFrame];

                        // Calcular posición en pantalla
                        double screenX = fire.getxImage() * ZOOM_LEVEL + mapX;
                        double screenY = fire.getyImage() * ZOOM_LEVEL + mapY;
                        double screenWidth = fire.getWidthImage() * ZOOM_LEVEL;
                        double screenHeight = fire.getHeightImage() * ZOOM_LEVEL;

                        // Debug: mostrar posición del fuego
                        System.out.println("Fire at: (" + fire.getxImage() + ", " + fire.getyImage() +
                                ") Screen: (" + screenX + ", " + screenY + ")");

                        // Dibujar el fuego
                        gc.drawImage(
                                fire.getImage(),
                                frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight(), // source
                                screenX, screenY, screenWidth, screenHeight // destination
                        );
                    } else {
                        System.err.println("Invalid animation frame for fire");
                    }
                } catch (Exception e) {
                    System.err.println("Error drawing fire: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void drawBomb() {
        if (bombController != null) {
            List<Bomb> bombs = bombController.getBombs();

            // Debug: show how many bombs there are
            if (!bombs.isEmpty()) {
                System.out.println("Drawing " + bombs.size() + " bombs");
            }

            for (Bomb bomb : bombs) {
                try {
                    // Calculate screen position
                    double screenX = bomb.getxImage() * ZOOM_LEVEL + mapX;
                    double screenY = bomb.getyImage() * ZOOM_LEVEL + mapY;
                    double screenWidth = bomb.getWidthImage() * ZOOM_LEVEL;
                    double screenHeight = bomb.getHeightImage() * ZOOM_LEVEL;

                    // Debug: show bomb position
                    System.out.println("Bomb at: (" + bomb.getxImage() + ", " + bomb.getyImage() +
                            ") Screen: (" + screenX + ", " + screenY + ")");

                    // Draw the bomb
                    Image bombImage = new Image(getClass().getResourceAsStream("/integrative/bomb/bomb.png"));
                    gc.drawImage(bombImage, screenX, screenY, screenWidth, screenHeight);
                } catch (Exception e) {
                    System.err.println("Error drawing bomb: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void drawEmergencyPaths() {
        if (carController == null) {
            return;
        }

        // Get all cars from the car controller
        List<AutomataCar> cars = carController.getCars();

        // Draw paths for emergency vehicles
        for (AutomataCar car : cars) {
            // Check if the car is an emergency vehicle
            if (car instanceof AmbulanceCar) {
                // Draw path for ambulance

            } else if (car instanceof FireTruckCar) {
                // Draw path for fire truck

            }
        }
    }

    private void drawEmergencyPath(AutomataCar car, javafx.scene.paint.Color color) {
        // Get the current position of the car
        double carX = car.getxImage();
        double carY = car.getyImage();

        // Get the destination (if any)
        NodeGraph<Intersection> destination = null;

        // Find the closest intersection to the car's current position
        Intersection closestIntersection = null;
        NodeGraph<Intersection> closestNode = null;
        double minDistance = Double.MAX_VALUE;

        for (Intersection intersection : intersectionList) {
            NodeGraph<Intersection>[] nodes = new NodeGraph[]{
                intersection.getTopLeft(),
                intersection.getTopRight(),
                intersection.getBottomLeft(),
                intersection.getBottomRight()
            };

            for (NodeGraph<Intersection> node : nodes) {
                if (node != null) {
                    double dx = node.getX() - carX;
                    double dy = node.getY() - carY;
                    double distance = Math.sqrt(dx*dx + dy*dy);

                    if (distance < minDistance) {
                        minDistance = distance;
                        closestIntersection = intersection;
                        closestNode = node;
                    }
                }
            }
        }

        // If we found a closest node, draw a line from the car to it
        if (closestNode != null) {
            // Skip drawing for ambulances when the closest node is a topLeft node
            // This prevents the unwanted drawing when an ambulance enters an intersection
            if (car instanceof AmbulanceCar && closestNode == closestIntersection.getTopLeft()) {
                return;
            }

            // Set line properties
            gc.setStroke(color);
            gc.setLineWidth(3);

            // Calculate screen positions
            double x1 = carX * ZOOM_LEVEL + mapX;
            double y1 = carY * ZOOM_LEVEL + mapY;
            double x2 = closestNode.getX() * ZOOM_LEVEL + mapX;
            double y2 = closestNode.getY() * ZOOM_LEVEL + mapY;

            // Draw the line
            gc.strokeLine(x1, y1, x2, y2);

            // Draw a circle at the destination
            gc.setFill(color);
            gc.fillOval(x2 - 5, y2 - 5, 10, 10);
        }
    }
}
