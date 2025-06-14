package integrative.task.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class GameObject {
    protected double xImage, yImage, widthImage, heightImage;
    protected String nameObject;
    protected ImageView imageView;

    public GameObject(String nameObject, String imagePath) {
        this.nameObject = nameObject;
        this.imageView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
        this.imageView.setFitWidth(35);
        this.imageView.setFitHeight(32);
        this.setPosition(0, 0);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImage(String imagePath) {
        // Ensure UI updates happen on the JavaFX application thread
        javafx.application.Platform.runLater(() -> {
            this.imageView.setImage(new Image(getClass().getResourceAsStream(imagePath)));
        });
    }

    public void setPosition(double x, double y) {
        // Ensure UI updates happen on the JavaFX application thread
        javafx.application.Platform.runLater(() -> {
            // Ajustar la posición para centrar el carro en el nodo
            this.xImage = x - (widthImage / 2);
            this.yImage = y - (heightImage / 2);
            this.imageView.setTranslateX(this.xImage);
            this.imageView.setTranslateY(this.yImage);
        });
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

    public void setHeightImage(double heightImage) {
        this.heightImage = heightImage;
    }

    public void move(String direction) {
        // Ensure UI updates happen on the JavaFX application thread
        javafx.application.Platform.runLater(() -> {
            switch (direction) {
                case "down": yImage += 2; break;
                case "up": yImage -= 2; break;
                case "left": xImage -= 2; break;
                case "right": xImage += 2; break;
            }
            imageView.setTranslateX(xImage);
            imageView.setTranslateY(yImage);
        });
    }
}
