package integrative.task.model;

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

public class Animation {
    String sourceImage;
    double x, y, width, height;

    public Animation(String sourceImage, double x, double y, double width, double height) {
        this.sourceImage = sourceImage;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Image spriteAnimation() {
        return new Image(getClass().getResourceAsStream(sourceImage));
    }

    public Rectangle calculateFrame() {
        return new Rectangle(x, y, width, height);
    }
}
