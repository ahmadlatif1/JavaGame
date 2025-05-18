import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {
    //Initialize Constants
    private final int PLATFORM_SPACING = 70;
    private final int PLATFORM_START_Y = 620;
    private final int PLATFORM_MIN_Y = 200;
    private final int PLATFORM_MIN_X = 100;
    private final int PLATFORM_RANDOM_X = 550;
    private final int PLATFORM_WIDTH = 200;
    private final int PLATFORM_HEIGHT = 20;
    private final int UPDATES_PER_SECOND = 60;
    private final int COIN_SPAWN_DELAY = 1000;
    private final int INITIAL_PLAYER_X = 500;
    private final int INITIAL_PLAYER_Y = 200;
    private final int GROUND_OFFSET = 70;
    private int GROUND_LEVEL; //is a constant but gets initialized on game start, otherwise the game breaks
    
    //Initialize Objects
    private Thread gameThread;
    private Player player;
    private ArrayList<Rectangle> platforms = new ArrayList<>();
    private Ellipse2D.Double[] coins;
    private GameEngine engine;
    private GameRenderer renderer;
    
    //Initialize Toggles
    private boolean devMode = false;
    private boolean gameOver = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

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
        player = new Player(INITIAL_PLAYER_X, INITIAL_PLAYER_Y);
        renderer = new GameRenderer();

        //Initialize platforms with random horizontal values
        for (int y = PLATFORM_START_Y; y >= PLATFORM_MIN_Y; y -= PLATFORM_SPACING) {
            platforms.add(new Rectangle((int) (Math.random() * PLATFORM_RANDOM_X) + PLATFORM_MIN_X, y, PLATFORM_WIDTH, PLATFORM_HEIGHT));
        }

        //Initialize coin array
        coins = new Ellipse2D.Double[platforms.size()];

        //Initialize the game engine and start the game
        engine = new GameEngine(player, platforms, coins);

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
        GROUND_LEVEL = getHeight() - GROUND_OFFSET;
        engine.setGroundLevel(GROUND_LEVEL);

        //The infinite game loop! (until you lose ofc)
        //Updates 60 times a second
        while(!gameOver){
            update();
            repaint();
            try{
                Thread.sleep(COIN_SPAWN_DELAY / UPDATES_PER_SECOND);
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
            engine.moveLeft();
        }
        if (rightPressed) {
            engine.moveRight();
        }
    }

    //The update method gets called inside the game loop
    private void update(){
        updateMovement();
        engine.update();
        gameOver=engine.isGameOver();
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
        if (key == KeyEvent.VK_SPACE ){
            engine.jump();
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
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Render graphics
        renderer.render(g, engine, engine.getPlayer(), platforms, coins, devMode, gameOver, GROUND_LEVEL, this);
    }

}
