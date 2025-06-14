package integrative.task.model;

import javafx.scene.image.Image;
import javafx.animation.AnimationTimer;

import java.util.HashMap;
import java.util.Map;

public class AutomataCar extends GameObject {
    protected Map<String, Animation> animations;
    private Image currentImage;

    // Animation properties
    private double startX, startY;
    private double targetX, targetY;
    private long startTime;
    private long duration = 800; // milliseconds
    private boolean isMoving = false;
    private String currentDirection = "right";
    private Runnable onMoveFinished;
    private AnimationTimer moveTimer;

    public AutomataCar(String nameObject) {
        super(nameObject, "/integrative/car/CarRight.png"); // Imagen inicial
        this.currentImage = new Image(getClass().getResourceAsStream(frameRight()));
        // Use the inherited fields from GameObject
        this.widthImage = 35;
        this.heightImage = 32;

        animations = new HashMap<>();
        animations.put("right", new Animation("/integrative/car/CarRight.png", 50, 0, 35, 32));
        animations.put("left", new Animation("/integrative/car/CarLeft.png", 50, 0, 35, 32));
        animations.put("up", new Animation("/integrative/car/CarUp.png", 50, 0, 20, 35));
        animations.put("down", new Animation("/integrative/car/CarDown.png", 50, 0, 20, 35));

        // Initialize the animation timer
        moveTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isMoving) {
                    updateMovement(now);
                }
            }
        };
        moveTimer.start();
    }

    public void changeDirection(String dir) {
        // Ensure UI updates happen on the JavaFX application thread
        javafx.application.Platform.runLater(() -> {
            currentDirection = dir;
            Animation anim = animations.get(dir);
            if (anim != null) {
                currentImage = anim.spriteAnimation();
            }
        });
    }

    public void moveTo(double x, double y, String dir, Runnable onFinish) {
        // Debug output for movement
        System.out.println("Moving car to position: (" + x + ", " + y + ") in direction: " + dir);
        System.out.println("Current position: (" + this.xImage + ", " + this.yImage + ")");

        // Ensure UI updates happen on the JavaFX application thread
        javafx.application.Platform.runLater(() -> {
            // Cambiar dirección y actualizar imagen
            changeDirection(dir);

            // Ajustar la posición para centrar el carro en el nodo
            double adjustedX = x - (widthImage / 2);
            double adjustedY = y - (heightImage / 2);

            // Debug output for adjusted position
            System.out.println("Adjusted target position: (" + adjustedX + ", " + adjustedY + ")");

            // Configurar propiedades de animación
            this.startX = this.xImage;
            this.startY = this.yImage;
            this.targetX = adjustedX;
            this.targetY = adjustedY;
            this.startTime = System.currentTimeMillis();
            this.onMoveFinished = onFinish;
            this.isMoving = true;
        });
    }

    private void updateMovement(long now) {
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Ensure UI updates happen on the JavaFX application thread
        javafx.application.Platform.runLater(() -> {
            if (elapsedTime >= duration) {
                // Animation complete
                xImage = targetX;
                yImage = targetY;
                imageView.setTranslateX(targetX);
                imageView.setTranslateY(targetY);
                isMoving = false;

                // Debug output for animation completion
                System.out.println("Animation complete. Final position: (" + xImage + ", " + yImage + ")");

                // Additional debug for police car
                if (this instanceof AutomataPoliceCar) {
                    System.out.println("[POLICE CAR] Animation complete. Final position: (" + xImage + ", " + yImage + ")");
                }

                // Call the completion callback
                if (onMoveFinished != null) {
                    onMoveFinished.run();
                }
            } else {
                // Calculate interpolated position
                double progress = (double) elapsedTime / duration;
                double currentX = startX + (targetX - startX) * progress;
                double currentY = startY + (targetY - startY) * progress;

                // Update both logical and visual positions
                xImage = currentX;
                yImage = currentY;
                imageView.setTranslateX(currentX);
                imageView.setTranslateY(currentY);

                // Debug output for animation progress (less frequent to avoid flooding console)
                if (elapsedTime % 200 == 0) {
                    System.out.println("Animation progress: " + (int)(progress * 100) + "%. Position: (" + xImage + ", " + yImage + ")");

                    // Additional debug for police car
                    if (this instanceof AutomataPoliceCar) {
                        System.out.println("[POLICE CAR] Animation progress: " + (int)(progress * 100) + "%. Position: (" + xImage + ", " + yImage + ")");
                    }
                }
            }
        });
    }


    public String frameUp() {
        return "/integrative/car/CarUp.png";
    }

    public String frameDown() {
        return "/integrative/car/CarDown.png";
    }

    public String frameLeft() {
        return "/integrative/car/CarLeft.png";
    }

    public String frameRight() {
        return "/integrative/car/CarRight.png";
    }

    public double getxImage() {
        return xImage;
    }

    public void setxImage(double xImage) {
        this.xImage = xImage;
    }

    public double getyImage() {
        return yImage;
    }

    public void setyImage(double yImage) {
        this.yImage = yImage;
    }

    public double getWidthImage() {
        return widthImage;
    }

    public void setWidthImage(double widthImage) {
        this.widthImage = widthImage;
    }

    public double getHeightImage() {
        return heightImage;
    }

    public Image getImage() {
        if (this instanceof AutomataPoliceCar) {
            System.out.println("[DEBUG] Getting image for police car: " + currentImage);
            if (currentImage == null) {
                System.out.println("[DEBUG] WARNING: Police car image is null!");
            }
        }
        return currentImage;
    }

    public void setHeightImage(double heightImage) {
        this.heightImage = heightImage;
    }

    public void stop() {
        // Ensure UI updates happen on the JavaFX application thread
        javafx.application.Platform.runLater(() -> {
            if (moveTimer != null) {
                moveTimer.stop();
            }
        });
    }
}
