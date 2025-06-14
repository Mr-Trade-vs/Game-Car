package integrative.task.model;

public class Bomb extends GameObject {
    private boolean active;
    private int explosionRadius;

    public Bomb(String nameObject) {
        super(nameObject, "/integrative/bomb/bomb.png");
        this.active = true;

        this.widthImage = 20;
        this.heightImage = 30;
    }

    public boolean isActive() {
        return active;
    }

    public int getExplosionRadius() {
        return explosionRadius;
    }

    public double getxImage() {
        return xImage;
    }

    public double getyImage() {
        return yImage;
    }

    public double getWidthImage() {
        return widthImage;
    }

    public double getHeightImage() {
        return heightImage;
    }
}
