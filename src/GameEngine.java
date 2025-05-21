import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

public class GameEngine {
    private final Player player;
    private final ArrayList<Rectangle> platforms;
    private final Ellipse2D.Double[] coins;
    private final ArrayList<Turret> turrets;

    private int groundLevel;
    private int verticalVelocity = 0;

    private boolean isJumping = false;
    private boolean isOnPlatform = false;
    private boolean isFalling = false;
    private boolean isDoubleJumping = false;
    private boolean canDoubleJump = false;
    private boolean gameOver = false;
    private boolean canRandomizePlatforms = false;
    private boolean isHit = false;

    private long lastCoinTime = System.currentTimeMillis();
    private long lastPlatformTime = System.currentTimeMillis();

    //Constructor
    public GameEngine(Player player, ArrayList<Rectangle> platforms, Ellipse2D.Double[] coins, ArrayList<Turret> turrets) {
        this.player = player;
        this.platforms = platforms;
        this.coins = coins;
        this.turrets = turrets;
        this.groundLevel = 0;
    }

    //Getters Setters
    public int getVerticalVelocity() {
        return verticalVelocity;
    }

    public boolean isHit() {
        return isHit;
    }
    public void setHit(boolean hit) {
        isHit = hit;
    }

    public void setGroundLevel(int groundLevel) {
        this.groundLevel = groundLevel;
    }

    public boolean isDoubleJumping() {
        return isDoubleJumping;
    }

    public boolean isFalling() {
        return isFalling;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Turret> getTurrets() {
        return turrets;
    }

    //Update
    public void update() {
        applyPhysics();
        checkCoinCollisions();
        spawnCoins();
        checkPowerUps();
        updateTurrets();
        CheckProjectileCollisions();
        randomizePlatforms();

        if(player.getPlayerHealth()<=0){
            gameOver=true;
        }

    }

    // Returns true if the player has fallen below ground level
    public boolean isGameOver() {
        return gameOver;
    }

    // Handles player jumping logic, including double jump mechanics
    public void jump() {
        if (player.getPlayerY() >= groundLevel - player.getPlayerHeight() || isOnPlatform) {
            verticalVelocity = player.getjumpStrength();
            isJumping = true;
        } else if (canDoubleJump && !isDoubleJumping) {
            verticalVelocity = player.getjumpStrength();
            isDoubleJumping = true;
        }
    }

    // Moves the player to the left
    public void moveLeft() {
        player.moveLeft();
    }

    // Moves the player to the right
    public void moveRight() {
        player.moveRight();
    }

    public void moveDown(){
        player.moveDown();
    }

    // Handles game physics including gravity, platform collision, and boundaries
    private void applyPhysics() {
        if (!isOnPlatform && player.getPlayerY() < groundLevel - player.getPlayerHeight()) {
            if (verticalVelocity > GameConfig.MAX_DOWN_ACCELERATION) {
                verticalVelocity = GameConfig.MAX_DOWN_ACCELERATION;
            }
            verticalVelocity += GameConfig.GRAVITY;
        } else if (!isOnPlatform && !isJumping) {
            player.setPlayerY(groundLevel - player.getPlayerHeight());
            verticalVelocity = 0;
        }

        for (Rectangle platform : platforms) {
            if ((!isJumping && (!isDoubleJumping || isFalling))
                    && GamePanel.doesTopIntersect(
                    player.getPlayerX(),
                    player.getPlayerY() + (player.getPlayerHeight() * 3 / 4),
                    player.getPlayerX() + player.getPlayerWidth(),
                    player.getPlayerY() + player.getPlayerHeight(),
                    platform.x, platform.y, platform.x + platform.width,
                    platform.y + platform.height)) {
                player.setPlayerY(platform.y - player.getPlayerHeight());
                verticalVelocity = 0;
                isOnPlatform = true;
                break;
            } else {
                isOnPlatform = false;
            }
        }

        if (verticalVelocity == 0) {
            isJumping = false;
        }

        if (isOnPlatform || player.getPlayerY() == groundLevel - player.getPlayerHeight()) {
            isDoubleJumping = false;
        }

        isFalling = verticalVelocity > 0;
        player.setPlayerY(player.getPlayerY() + verticalVelocity);

        if (player.getPlayerX() < 0) {
            player.setPlayerX(0);
        } else if (player.getPlayerX() > 1000 - player.getPlayerWidth()) {
            player.setPlayerX(1000 - player.getPlayerWidth());
        }

        if (player.getPlayerY() > groundLevel) {
            gameOver = true;
        }
    }

    // Checks for collisions between player and coins, updates score accordingly
    private void checkCoinCollisions() {
        for (int i = 0; i < coins.length; i++) {
            if (coins[i] == null) continue;
            if (player.getPlayerRect().intersects(coins[i].x, coins[i].y, coins[i].width, coins[i].height)) {
                player.setPlayerScore(player.getPlayerScore() + 1);
                coins[i] = null;
            }
        }
    }

    // Checks for collisions between player and projectiles, updates player health accordingly
    private void CheckProjectileCollisions(){
        for (Turret turret : turrets) {
            if (turret.getProjectile() == null) continue;
            if (player.getPlayerRect().intersects(turret.getProjectile().self())) {
                this.isHit = true;
                player.setPlayerHealth(player.getPlayerHealth() - 15);
                turret.setNullProjectile();
                turret.setNextFireTime(System.currentTimeMillis()+(long)(Math.random()*GameConfig.PROJECTILE_FIRE_COOLDOWN_INTERVAL_MS/2)+GameConfig.PROJECTILE_FIRE_COOLDOWN_INTERVAL_MS/2);
            }
        }
    }

    // Spawns new coins on platforms at regular intervals
    private void spawnCoins() {
        int i = (int) (Math.random() * coins.length);
        if (coins[i] == null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCoinTime >= GameConfig.COIN_SPAWN_DELAY) {
                coins[i] = new Ellipse2D.Double(platforms.get(i).x + (double) platforms.get(i).width / 2 - GameConfig.COIN_OFFSET_X,
                        platforms.get(i).y + (double) platforms.get(i).height / 2 - GameConfig.COIN_OFFSET_Y, GameConfig.COIN_SIZE, GameConfig.COIN_SIZE);
                lastCoinTime = currentTime;
            }
        }
    }

    // Activates power-ups based on player's score
    private void checkPowerUps() {
        if (player.getPlayerScore() >= GameConfig.POWER_UP_SPEED_SCORE) {
            player.setPlayerSpeed(GameConfig.POWER_UP_SPEED);
        }
        if (player.getPlayerScore() >= GameConfig.POWER_UP_JUMP_SCORE) {
            player.setJumpStrength(GameConfig.POWER_UP_JUMP_STRENGTH);
        }
        if (player.getPlayerScore() >= GameConfig.POWER_UP_DOUBLE_JUMP_SCORE) {
            canDoubleJump = true;
        }
        if(player.getPlayerScore() >= GameConfig.PENALTY_RANDOM_PLATFORMS){
            canRandomizePlatforms=true;
        }
    }

    // Updates turrets, including their projectile positions and firing intervals
    private void updateTurrets(){
        for(Turret turret : turrets){
            turret.update(System.currentTimeMillis());
        }
    }

    // Randomizes platforms positions at regular intervals after a score milestone
    private void randomizePlatforms(){
        if(canRandomizePlatforms) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastPlatformTime >= GameConfig.PENALTY_RANDOM_PLATFORMS_INTERVAL) {
                platforms.clear();
                for (int y = GameConfig.PLATFORM_START_Y; y >= GameConfig.PLATFORM_END_Y; y -= GameConfig.PLATFORM_SPACING) {
                    platforms.add(new Rectangle((int) (Math.random() * GameConfig.PLATFORM_MAX_X) + GameConfig.PLATFORM_MIN_X, y, GameConfig.PLATFORM_WIDTH, GameConfig.PLATFORM_HEIGHT));
                }
                lastPlatformTime = currentTime;
            }
        }
    }
}
