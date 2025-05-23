import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

public class GameRenderer {

    private static int timer = 12;

    public void render(Graphics g, GameEngine engine, Player player, ArrayList<Rectangle> platforms, Ellipse2D.Double[] coins, boolean devMode, boolean gameOver, int groundLevel, GamePanel panel) {
        // Cast to Graphics2D for better control
        Graphics2D g2d = (Graphics2D) g;

        // Clear background
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        // Draw ground
        g2d.setColor(Color.WHITE);
        g2d.drawLine(0, groundLevel, panel.getWidth(), groundLevel);

        if(timer>0 && engine.isHit()){
            timer--;
        }else{
            timer=12;
            engine.setHit(false);
        }
        // Draw player
        if(engine.isHit()){
        g2d.setColor(Color.RED);
        }else{
        g2d.setColor(Color.GREEN);
        }
        g2d.fillRect(player.getPlayerX(), player.getPlayerY(), player.getPlayerWidth(), player.getPlayerHeight());

        // Draw platforms
        g2d.setColor(Color.GRAY);
        for (Rectangle platform : platforms) {
            g2d.fill(platform);
        }

        // Draw coins
        g2d.setColor(Color.YELLOW);
        for (Ellipse2D.Double coin : coins) {
            if (coin != null) {
                g2d.fill(coin);
            }
        }

        // Draw projectiles
        g2d.setColor(Color.RED);
        for(Turret turret : engine.getTurrets()) {
            if (turret.getProjectile() != null) {
                g2d.fill(new Ellipse2D.Double(turret.getProjectile().getX(), turret.getProjectile().getY(), GameConfig.PROJECTILE_WIDTH, GameConfig.PROJECTILE_HEIGHT));
            }
        }

        // Draw score
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("Arial", Font.ITALIC, 20));
        g2d.drawString("Score: " + player.getPlayerScore(), 50, 50);
        // Draw health
        g2d.drawString("Health: " + player.getPlayerHealth(), 50, 80);

        // Game over message
        if (gameOver) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.drawString("GAME OVER", GameConfig.SCREEN_WIDTH / 2 - 150, GameConfig.SCREEN_HEIGHT / 2);
        }

        // Dev mode variables
        if (devMode) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.ITALIC, 14));
            g.drawString("X: "+player.getPlayerX(),panel.getWidth()-50,panel.getHeight()-90);
            g.drawString("Y: "+player.getPlayerY(),panel.getWidth()-130,panel.getHeight()-90);
            g.drawString("Jumping: "+engine.isJumping(),panel.getWidth()-130,panel.getHeight()-140);
            g.drawString("VV: "+engine.getVerticalVelocity(),panel.getWidth()-130,panel.getHeight()-210);
            g.drawString("Falling: "+engine.isFalling(),panel.getWidth()-130,panel.getHeight()-280);
            g.drawString("DoubleJumping: "+engine.isDoubleJumping(),panel.getWidth()-160,panel.getHeight()-350);
        }
    }
}