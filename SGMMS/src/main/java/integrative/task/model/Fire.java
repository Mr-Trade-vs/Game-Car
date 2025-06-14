package integrative.task.model;

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;

public class Fire extends GameObject{

    double x, y, widht, height;
    private int currentFrame;
    private long lastFrameTime;
    private static final long FRAME_DURATION = 200;// milliseconds between frame changes
    private Image currentImage;

    private HashMap<String, Animation []> animationFire;

    public Fire(String nameObject) {
        super(nameObject, "/integrative/fire/Fire_Spreadsheet.png");
        this.currentImage = new Image(getClass().getResourceAsStream("/integrative/fire/Fire_Spreadsheet.png"));
        this.animationFire = new HashMap<>();
        Animation[] move = new Animation[4];
        move[0] = new Animation("/integrative/fire/Fire_Spreadsheet.png", 0, 0, 512, 512);
        move[1] = new Animation("/integrative/fire/Fire_Spreadsheet.png", 512, 0, 512, 512);
        move[2] = new Animation("/integrative/fire/Fire_Spreadsheet.png", 0, 512, 512, 512);
        move[3] = new Animation("/integrative/fire/Fire_Spreadsheet.png", 512, 512, 512, 512);
        this.x = move[0].x;
        this.y = move[0].y;
        this.widht = move[0].width;
        this.height = move[0].height;
        animationFire.put("fire", move);
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
    }

    public Rectangle[] getAnimationFire(){
        Animation[] fire = animationFire.get("fire");
        Rectangle[] animationFire = new Rectangle[fire.length];

        for(int i = 0; i < fire.length; i++){
            animationFire[i] = fire[i].calculateFrame();
        }
        return animationFire;
    }

    public String spriteAnimationFire(){
        Animation[] fire = animationFire.get("fire");
        return fire[0].sourceImage;
    }

    public void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > FRAME_DURATION) {
            currentFrame = (currentFrame + 1) % 4;
            lastFrameTime = currentTime;
        }
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public Image getImage(){
        return currentImage;
    }
}
