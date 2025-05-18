import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {
    //Initialize Constants
    private final int GRAVITY = 1;
    private final int MAX_DOWN_ACCELERATION = 10;
    private int GROUND_LEVEL; //is a constant but gets initialized on game start, otherwise the game breaks
    
    //Initialize Objects
    private Thread gameThread;
    private Player player;
    private ArrayList<Rectangle> platforms = new ArrayList<>();
    private Ellipse2D.Double[] coins;
    
    //Initialize Toggles
    private boolean devMode = false;
    private boolean gameOver = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean isJumping = false;
    private boolean isOnPlatform = false;
    private boolean isFalling =false;
    private boolean canDoubleJump = false;
    private boolean isDoubleJumping = false;
    
    //initialize vertical velocity
    private int verticalVelocity = 0;
    
    //get the current time to calculate coin spawn timing
    private long lastCoinTime = System.currentTimeMillis();

    //Constructor
    public GamePanel() {
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        
        //Initialize a key listener for keyboard clicks
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }

            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        });
        
        startGame();
    }

    //Initialize Game Objects and start a thread
    private void startGame(){

        //Initialize thread and player objects
        gameThread = new Thread(this);
        player = new Player(500, 200);

        //Initialize platforms with random horizontal values
        platforms.add(new Rectangle((int) (Math.random()*550)+100, 600, 200, 20));
        platforms.add(new Rectangle((int) (Math.random()*550)+100, 550, 200, 20));
        platforms.add(new Rectangle((int) (Math.random()*550)+100, 480, 200, 20));
        platforms.add(new Rectangle((int) (Math.random()*550)+100, 410, 200, 20));
        platforms.add(new Rectangle((int) (Math.random()*550)+100, 340, 200, 20));
        platforms.add(new Rectangle((int) (Math.random()*550)+100, 250, 200, 20));
        platforms.add(new Rectangle((int) (Math.random()*550)+100, 200, 200, 20));

        //Initialize coin array
        coins = new Ellipse2D.Double[platforms.size()];

        //Start thread
        gameThread.start();
    }

    //This method executes when the thread starts, this is called the Game Loop
    @Override
    public void run(){
        // Wait for the components to Initialize before starting the game
        while (getWidth() == 0 || getHeight() == 0 || !isVisible()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }

        //Set ground level
        GROUND_LEVEL = getHeight() - 70;

        //The infinite game loop! (until you lose ofc)
        //Updates 60 times a second
        while(!gameOver){
            update();
            repaint();
            try{
                Thread.sleep(1000/60);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    //a method to check for intersection between 2 rectangles (not my doing, edit at your own risk!)
    public static boolean doesTopIntersect(
            int ax1, int ay1, int ax2, int ay2,
            int bx1, int by1, int bx2, int by2) {

        // Normalize coordinates
        int aLeft = Math.min(ax1, ax2);
        int aRight = Math.max(ax1, ax2);
        int aTop = Math.min(ay1, ay2);
        int aBottom = Math.max(ay1, ay2);

        int bLeft = Math.min(bx1, bx2);
        int bRight = Math.max(bx1, bx2);
        int bTop = Math.min(by1, by2);
        int bBottom = Math.max(by1, by2);

        // Check horizontal (x-axis) overlap
        boolean horizontalOverlap = !(aRight < bLeft || aLeft > bRight);

        // Check if the top of B intersects the bottom of A
        boolean verticalTouch = bTop <= aBottom && bTop >= aTop;

        return horizontalOverlap && verticalTouch;
    }

    //This method is important for instantaneous movement on button click
    //The Key listener has a delay for long key presses which results in clunky movement, this here solves it by using booleans as states
    private void updateMovement(){
        if (leftPressed) {
            player.moveLeft();
        }
        if (rightPressed) {
            player.moveRight();
        }
    }
    
    //All the physics happen here. Gravity, Collisions
    private void applyPhysics(){
        
        //Apply gravity and check for ground collisions
        if (!isOnPlatform && player.getPlayerY() < GROUND_LEVEL - player.getPlayerHeight()) {
            if (verticalVelocity > MAX_DOWN_ACCELERATION) {
                verticalVelocity = MAX_DOWN_ACCELERATION;
            }
            verticalVelocity += GRAVITY;
        }else if (!isOnPlatform && !isJumping) {
            player.setPlayerY(GROUND_LEVEL - player.getPlayerHeight());
            verticalVelocity = 0;
        }

        //Check for platform collisions
        for (Rectangle platform : platforms) {
            if ((!isJumping || (!isDoubleJumping && isFalling))
                    && doesTopIntersect(player.getPlayerX(),
                    player.getPlayerY() + (player.getPlayerHeight()/2 + player.getPlayerHeight()/4),
                    player.getPlayerX() + player.getPlayerWidth(),
                    player.getPlayerY() + player.getPlayerHeight(),
                    platform.x, platform.y, platform.x + platform.width,
                    platform.y + platform.height)) {
                player.setPlayerY(platform.y - player.getPlayerHeight());
                verticalVelocity = 0;
                isOnPlatform = true;
                break;
            }else {
                isOnPlatform = false;
            }
        }

        //Reset Jumping check when the player starts falling
        if(verticalVelocity==0){
            isJumping = false;
        }
        
        //Reset DoubleJumping check when the player touches any ground surface
        if(isOnPlatform || player.getPlayerY() == GROUND_LEVEL - player.getPlayerHeight()){
            isDoubleJumping = false;
        }
        
        //Toggle falling check depending on the current vertical velocity
        isFalling = verticalVelocity > 0;

        //Update vertical position
        player.setPlayerY(player.getPlayerY() + verticalVelocity);

        //Keep the player within horizontal bounds
        if (player.getPlayerX() < 0) {
            player.setPlayerX(0);
        } else if (player.getPlayerX() > getWidth() - player.getPlayerWidth()) {
            player.setPlayerX(getWidth() - player.getPlayerWidth());
        }

        //Keep the player within vertical bounds
        if (player.getPlayerY() < 0) {
            player.setPlayerY(0);
        } else if (player.getPlayerY() > GROUND_LEVEL) {
            //If the player happens to fall below the ground level, the game ends
            gameOver = true;
        }

    }
    
    //This method checks if the player is colliding with any coin
    //Removes the coin and increments the player score on collision
    private void checkCoinCollisions(){
        for(int i=0;i<coins.length;i++){
            if(coins[i]==null)continue;
            if(player.getPlayerRect().intersects(coins[i].x,coins[i].y,coins[i].width,coins[i].height)){
                player.setPlayerScore(player.getPlayerScore()+1);
                coins[i]=null;
            }
        }
    }
    
    //This method respawns coins randomly every second
    private void spawnCoins(){
        int i = (int) (Math.random()*7);
            if(coins[i]==null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastCoinTime >= 1000) {
                    coins[i] = new Ellipse2D.Double(platforms.get(i).x + (double) platforms.get(i).width / 2 - 10, platforms.get(i).y + (double) platforms.get(i).height / 2 - 50, 20, 20);
                    lastCoinTime = currentTime;
                }
            }
    }
    
    //This method checks for possible power ups based on the player score
    private void checkPowerUps(){
        if(player.getPlayerScore()>=30){
            player.setplayerSpeed(6);
        }
        if(player.getPlayerScore()>=50){
            player.setjumpStrength(-18);
        }
        if(player.getPlayerScore()>=100){
            canDoubleJump=true;
        }
    }

    //The update method gets called inside the game loop
    private void update(){
        updateMovement();
        applyPhysics();
        checkCoinCollisions();
        spawnCoins();
        checkPowerUps();
    }

    //Method to handle key down events
    private void handleKeyPress(KeyEvent e){
        int key = e.getKeyCode();

        //Left and Right arrows
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }

        //Space key for jumping and double jumping
        if (key == KeyEvent.VK_SPACE && (player.getPlayerY() >= GROUND_LEVEL - player.getPlayerHeight() || isOnPlatform)) {
            verticalVelocity = player.getjumpStrength();
            isJumping = true;
        }else if(key==KeyEvent.VK_SPACE && canDoubleJump && !isDoubleJumping){
            verticalVelocity = player.getjumpStrength();
            isDoubleJumping = true;
        }

        //D key for Dev Mode toggle
        if(key==KeyEvent.VK_D){
            devMode=!devMode;
        }

    }

    //Method to handle key up events, mostly for smooth movement
    private void handleKeyRelease(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }

    //This method draws everything on the panel
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        //Draw the ground line
        g.setColor(Color.WHITE);
        g.drawLine(0,getHeight()-70,getWidth(),getHeight()-70);
        g.setColor(Color.GREEN);
        g.fillRect(player.getPlayerX(), player.getPlayerY(), player.getPlayerWidth(), player.getPlayerHeight());

        //Draw platforms
        g.setColor(Color.GRAY);
        for (Rectangle platform : platforms) {
            g.fillRect(platform.x, platform.y, platform.width, platform.height);
        }

        //Draw coins
        g.setColor(Color.YELLOW);
        for (Ellipse2D.Double coin : coins) {
            if(coin==null)continue;
            g.fillOval((int) coin.x, (int) coin.y, (int) coin.width, (int) coin.height);
        }

        //Draw score
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial",Font.ITALIC,20));
        g.drawString("Score: "+player.getPlayerScore(),50,50);

        //Draw a game over message when the game is over
        if(gameOver){
            g.setColor(Color.RED);
            g.setFont(new Font("Arial",Font.BOLD,30));
            g.drawString("GAME OVER",getWidth()/2-150,getHeight()/2);
        }

        //Draw variables on the panel for debugging AKA Dev Mode
        if(devMode){
        g.setColor(Color.RED);
        g.setFont(new Font("Arial",Font.ITALIC,14));
        g.drawString("X: "+player.getPlayerX(),getWidth()-50,getHeight()-70);
        g.setColor(Color.RED);
        g.setFont(new Font("Arial",Font.ITALIC,14));
        g.drawString("Y: "+player.getPlayerY(),getWidth()-130,getHeight()-70);
        g.setColor(Color.RED);
        g.setFont(new Font("Arial",Font.ITALIC,14));
        g.drawString("Jumping: "+isJumping,getWidth()-130,getHeight()-120);
        g.setColor(Color.RED);
        g.setFont(new Font("Arial",Font.ITALIC,14));
        g.drawString("VV: "+verticalVelocity,getWidth()-130,getHeight()-170);
        g.setColor(Color.RED);
        g.setFont(new Font("Arial",Font.ITALIC,14));
        g.drawString("Falling: "+isFalling,getWidth()-130,getHeight()-210);
        g.setColor(Color.RED);
        g.setFont(new Font("Arial",Font.ITALIC,14));
        g.drawString("DoubleJumping: "+isDoubleJumping,getWidth()-130,getHeight()-260);
    }
    }

}
