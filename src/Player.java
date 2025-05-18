import java.awt.*;

public class Player {
    private final int PLAYER_HEIGHT = 40;
    private final int PLAYER_WIDTH = 40;
    private int jumpStrength = -15;
    private int playerSpeed = 4;

    private final Rectangle playerRect;

    private int score = 0;
    private int playerHealth = 100;
    private int playerX;
    private int playerY;

    public Player(int x, int y) {
        this.playerX = x;
        this.playerY = y;
        this.playerRect = new Rectangle(x, y, PLAYER_WIDTH, PLAYER_HEIGHT);
    }

    public void moveLeft() {
        this.playerX -= playerSpeed;
        this.playerRect.x = this.playerX;
    }

    public void moveRight() {
        this.playerX += playerSpeed;
        this.playerRect.x = this.playerX;
    }

    public void jump() {
        this.playerY += jumpStrength;
        this.playerRect.y = this.playerY;
    }

    public void loseHealth(int damage) {
        this.playerHealth -= damage;
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public void setPlayerY(int playerY) {
        this.playerY = playerY;
        this.playerRect.y = playerY;
    }

    public void setPlayerX(int playerX) {
        this.playerX = playerX;
        this.playerRect.x = playerX;
    }

    public int getPlayerHeight() {
        return PLAYER_HEIGHT;
    }

    public int getPlayerWidth() {
        return PLAYER_WIDTH;
    }

    public Rectangle getPlayerRect() {
        return playerRect;
    }

    public void setPlayerScore(int score) {
        this.score = score;
    }

    public int getPlayerScore() {
        return score;
    }

    public void setjumpStrength(int jumpStrength) {
        this.jumpStrength = jumpStrength;
    }

    public void setplayerSpeed(int playerSpeed) {
        this.playerSpeed = playerSpeed;
    }

    public int getplayerSpeed() {
        return playerSpeed;
    }

    public int getjumpStrength() {
        return jumpStrength;
    }

    public int getPlayerHealth() {
        return playerHealth;
    }

    public void setPlayerHealth(int playerHealth) {
        this.playerHealth = playerHealth;
    }


}
