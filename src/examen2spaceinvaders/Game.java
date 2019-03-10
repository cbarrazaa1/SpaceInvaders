package examen2spaceinvaders;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Game Handles everything that the game needs to function correctly.
 *
 * @author César Barraza A01176786 Date 30/Jan/2019
 * @version 1.0
 */
public class Game implements Runnable {
    /**
     * Constants
     */
    public static final int MAX_STARS = 200;
    public static final int MAX_ENEMIES_ROW = 5;
    public static final int MAX_ENEMIES_COLUMN = 7;
    
    /**
     * The display's properties.
     */
    private String title;
    private int width;
    private int height;
    private int direction;

    /**
     * The display that represents the game's window.
     */
    private Display display;

    /**
     * The game's input managers.
     */
    private KeyManager keyManager;
    private MouseManager mouseManager;

    /**
     * The main game loop thread.
     */
    private Thread thread;

    /**
     * Denotes whether or not the game is running.
     */
    private boolean isRunning;

    /**
     * The game items.
     */
    private Bullet playerBullet;
    private Bullet enemyBullet;
    private Player player;
    private Enemy enemy;
    private ArrayList<Star> stars;
    private Enemy enemies[][];
    
    /**
     * The game timers
     */
    
    /**
     * Game lives
     */

    /**
     * Initializes the game object with the desired display properties.
     *
     * @param title
     * @param width
     * @param height
     */
    public Game(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.isRunning = false;
        this.keyManager = new KeyManager();
        this.mouseManager = new MouseManager();
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the keyManager
     */
    public KeyManager getKeyManager() {
        return keyManager;
    }

    /**
     * @return the mouseManager
     */
    public MouseManager getMouseManager() {
        return mouseManager;
    }

    /**
     * Starts all initializations needed to start the game.
     */
    private void init() {
        // create the display
        display = new Display(getTitle(), getWidth(), getHeight());
        display.getWindow().addKeyListener(getKeyManager());
        display.getWindow().addMouseListener(getMouseManager());
        display.getWindow().addMouseMotionListener(getMouseManager());
        display.getCanvas().addMouseListener(getMouseManager());
        display.getCanvas().addMouseMotionListener(getMouseManager());

        // initialize the game's assets
        Assets.init();

        // start the game
        initItems();
    }

    /**
     * Creates the items that will be used in the game.
     */
    private void initItems() {
        //playerBullet = new Bullet(400, 400, 5, 5, -5);
        //enemyBullet = new Bullet(200, 100, 3, 16, 5);
        player = new Player((getWidth() / 2) - 24, 540, 48, 48, this);
        enemy = new Enemy(200, 80, 75, 75, this);
        stars = new ArrayList();
        enemies = new Enemy[MAX_ENEMIES_ROW][MAX_ENEMIES_COLUMN];
        
        // create stars
        for(int i = 0; i < MAX_STARS; i++) {
            int size = Util.randNum(1, 2);
            stars.add(new Star(Util.randNum(0, getWidth()), Util.randNum(0, getHeight()), size, size, size));
        }
        //create aliens
        for (int i = 0; i < MAX_ENEMIES_ROW; i++) {
            for (int j = 0; j < MAX_ENEMIES_COLUMN; j++) {
                int w = 50;
                int h = 45;
                int posX = w * j + 20;
                int posY =  h * i + 20;
                enemies[i][j] = new Enemy(posX, posY, w, h, this);
            }
        }
    }

    /**
     * Starts the main game thread.
     */
    public synchronized void start() {
        if (!isRunning) {
            isRunning = true;
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * Stops the game thread execution.
     */
    public synchronized void stop() {
        if (isRunning) {
            isRunning = false;
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the game every frame.
     */
    private void update() {
        //playerBullet.update();
        //enemyBullet.update();
        player.update();
        //enemy.update();
        
        for(int i = 0; i < stars.size(); i++) {
            Star star = stars.get(i);
            if(star.getY() >= getHeight()) {
                star.setY(0);
                star.setX(Util.randNum(0, getWidth()));
            }
            star.update();
        }
        
        for(int i = 0; i < MAX_ENEMIES_ROW; i++) {
            for(int j = 0; j < MAX_ENEMIES_COLUMN; j++) {
                Enemy enemy = enemies[i][j];
                enemy.update();
                // collission with borders
                if(enemy.getX() + enemy.getWidth() > getWidth()) {
                    for(int k = 0; k < MAX_ENEMIES_ROW; k++) {
                        for(int l = 0; l < MAX_ENEMIES_COLUMN; l++) {
                            Enemy newEnemy = enemies[k][l];
                            newEnemy.setX(newEnemy.getX() - 5);
                            newEnemy.setVelX(newEnemy.getVelX() * -1);
                        }
                    }
                }
                else if(enemy.getX() <= 0) {
                    for(int k = 0; k < MAX_ENEMIES_ROW; k++) {
                        for(int l = 0; l < MAX_ENEMIES_COLUMN; l++) {
                            Enemy newEnemy = enemies[k][l];
                            newEnemy.setX(newEnemy.getX() + 5);
                            newEnemy.setVelX(newEnemy.getVelX() * -1);
                        }
                    }
                }
            }
        }
        
       // update input
        getKeyManager().update();
        getMouseManager().update();
    }

    /**
     * Renders the game every frame.
     */
    private void render() {
        BufferStrategy bs = display.getCanvas().getBufferStrategy();

        // if there's no buffer strategy yet, create it
        if (bs == null) {
            display.getCanvas().createBufferStrategy(3);
        } else {
            // clear screen
            Graphics g = bs.getDrawGraphics();
            g.clearRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            for(int i = 0; i < stars.size(); i++) {
                stars.get(i).render(g);
            }
            
            player.render(g);
            //playerBullet.render(g);
            //enemyBullet.render(g);
            
            for(int i = 0; i < MAX_ENEMIES_ROW; i++) {
                for (int j = 0; j < MAX_ENEMIES_COLUMN; j++) {
                    enemies[i][j].render(g);
                }
            }            
            // actually render the whole scene
            bs.show();
            g.dispose();
        }
    }

    /**
     * The main game loop.
     */
    @Override
    public void run() {
        init();

        // set up timing constants
        final int maxFPS = 60;
        final double timeTick = 1000000000 / maxFPS;

        // set up timing variables
        double delta = 0.0d;
        long now = 0;
        long lastTime = System.nanoTime();

        // start game loop
        while (isRunning) {
            // calculate delta
            now = System.nanoTime();
            delta += (now - lastTime) / timeTick;
            lastTime = now;

            // make sure game always plays at desired fps
            if (delta >= 1.0d) {
                update();
                render();
                delta--;
            }
        }

        // once game loop is over, close the game
        stop();
    }
}
