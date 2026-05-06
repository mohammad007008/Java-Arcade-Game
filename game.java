import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferStrategy;
import java.awt.geom.Path2D;
import java.util.Random;

class Game extends Canvas implements Runnable {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int FPS = 60;

    private final Random random = new Random();

    // Variables for scoreboard
    private int time = 3000;
    private int score = 0;
    private int health = 3;
    private final int maxHealth = 3;
    private final int maxScore = 1000;

    // Variables for color changing when collision happens
    private int colorDuration = 0;
    private int colorType = 0;

    // Variables for the character
    private float characterX = WIDTH / 2.0f;
    private float characterY = HEIGHT / 2.0f;
    private final float characterSize = 56;

    // Variables for elemental orbs
    private final int orbCount = 8;
    private final float orbSize = 25;
    private final float[] orbX = new float[orbCount];
    private final float[] orbY = new float[orbCount];
    private final int[] orbType = new int[orbCount]; // 0 = Fire, 1 = Water, 2 = Earth, 3 = Air

    // Variables for corrupted energy
    private final int darkEnergyCount = 8;
    private final float darkEnergySize = 15;
    private final float darkEnergySpeed = 3.5f;
    private final float[] darkEnergyX = new float[darkEnergyCount];
    private final float[] darkEnergyY = new float[darkEnergyCount];

    // Helps control game flow
    private boolean gameOver = false;
    private boolean running = false;
    private boolean mousePressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private Thread gameThread;

    Game() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(20, 40, 60));
        setFocusable(true);

        for (int i = 0; i < orbCount; i++) {
            resetOrb(i);
        }

        for (int i = 0; i < darkEnergyCount; i++) {
            resetDarkEnergy(i);
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                setMovementKey(event.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent event) {
                setMovementKey(event.getKeyCode(), false);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                mousePressed = true;
                moveCharacterToMouse(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                mousePressed = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                if (mousePressed) {
                    moveCharacterToMouse(event);
                }
            }
        });
    }

    void start() {
        if (running) {
            return;
        }

        running = true;
        gameThread = new Thread(this, "game-loop");
        gameThread.start();
        requestFocusInWindow();
    }

    @Override
    public void run() {
        final double timePerUpdate = 1_000_000_000.0 / FPS;
        long previousTime = System.nanoTime();
        double unprocessedTime = 0;

        while (running) {
            long currentTime = System.nanoTime();
            unprocessedTime += (currentTime - previousTime) / timePerUpdate;
            previousTime = currentTime;

            while (unprocessedTime >= 1) {
                updateGame();
                unprocessedTime--;
            }

            drawGame();
            sleepBriefly();
        }
    }

    private void updateGame() {
        if (!gameOver) {
            time--;
            if (time <= 0 || score >= maxScore || health <= 0) {
                gameOver = true;
            }

            moveCharacter();
            moveDarkEnergy();
            checkCollisions();
        }

        if (colorDuration > 0) {
            colorDuration--;
        }
    }

    private void drawGame() {
        BufferStrategy bufferStrategy = getBufferStrategy();
        if (bufferStrategy == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics graphics = bufferStrategy.getDrawGraphics();
        try {
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawBackground(g);
            drawCharacter(g);
            drawOrbs(g);
            drawDarkEnergy(g);
            drawScoreBoard(g);
            drawGameOver(g);
        } finally {
            graphics.dispose();
        }

        bufferStrategy.show();
    }

    private void drawBackground(Graphics2D g) {
        g.setColor(new Color(20, 40, 60));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Moon glow
        g.setColor(new Color(200, 220, 255, 80));
        fillOval(g, 700, 80, 80, 80);

        // Moon
        g.setColor(new Color(240, 240, 255));
        fillOval(g, 700, 80, 40, 40);

        // Ground
        g.setColor(new Color(30, 80, 50));
        g.fillRect(0, HEIGHT - 80, WIDTH, 80);
    }

    private void moveCharacter() {
        if (upPressed) {
            characterY -= 5;
        }
        if (downPressed) {
            characterY += 5;
        }
        if (leftPressed) {
            characterX -= 5;
        }
        if (rightPressed) {
            characterX += 5;
        }

        float half = characterSize / 2.0f;
        characterX = clamp(characterX, half, WIDTH - half);
        characterY = clamp(characterY, half, HEIGHT - half);
    }

    private void moveDarkEnergy() {
        for (int i = 0; i < darkEnergyCount; i++) {
            darkEnergyY[i] += darkEnergySpeed;
            if (darkEnergyY[i] > HEIGHT + darkEnergySize) {
                resetDarkEnergy(i);
            }
        }
    }

    private void drawCharacter(Graphics2D g) {
        if (colorDuration > 0) {
            if (colorType == 1) {
                g.setColor(new Color(100, 255, 100));
            } else if (colorType == -1) {
                g.setColor(new Color(255, 100, 100));
            }
        } else {
            g.setColor(new Color(180, 180, 180));
        }

        // Main drone body
        g.setStroke(new java.awt.BasicStroke(2));
        g.setColor(g.getColor());
        g.fillRoundRect(Math.round(characterX - 20), Math.round(characterY - 12), 40, 24, 6, 6);
        g.setColor(new Color(100, 100, 100));
        g.drawRoundRect(Math.round(characterX - 20), Math.round(characterY - 12), 40, 24, 6, 6);

        // Sensor eye
        g.setColor(new Color(0, 255, 255));
        fillOval(g, characterX, characterY, 18, 18);

        // Top head fin
        g.setColor(new Color(200, 200, 200));
        Polygon fin = new Polygon();
        fin.addPoint(Math.round(characterX), Math.round(characterY - 24));
        fin.addPoint(Math.round(characterX - 10), Math.round(characterY - 10));
        fin.addPoint(Math.round(characterX + 10), Math.round(characterY - 10));
        g.fillPolygon(fin);
        g.setColor(new Color(120, 120, 120));
        g.drawPolygon(fin);

        // Bottom hover engine
        g.setColor(new Color(80, 80, 80));
        g.fillArc(Math.round(characterX - 14), Math.round(characterY + 5), 28, 18, 0, -180);

        // Antenna
        g.setColor(new Color(0, 255, 255));
        g.setStroke(new java.awt.BasicStroke(2));
        g.drawLine(Math.round(characterX), Math.round(characterY - 24), Math.round(characterX), Math.round(characterY - 38));

        // Side thrusters
        g.setColor(new Color(90, 90, 90));
        g.fillRoundRect(Math.round(characterX - 30), Math.round(characterY - 8), 8, 20, 3, 3);
        g.fillRoundRect(Math.round(characterX + 22), Math.round(characterY - 8), 8, 20, 3, 3);
    }

    private void drawOrbs(Graphics2D g) {
        for (int i = 0; i < orbCount; i++) {
            if (orbType[i] == 0) {
                g.setColor(new Color(255, 80, 0)); // Fire
            } else if (orbType[i] == 1) {
                g.setColor(new Color(0, 150, 255)); // Water
            } else if (orbType[i] == 2) {
                g.setColor(new Color(100, 200, 100)); // Earth
            } else {
                g.setColor(new Color(230, 230, 255)); // Air
            }

            fillOval(g, orbX[i], orbY[i], orbSize, orbSize);

            // Glowing inner orb
            g.setColor(new Color(255, 255, 255, 120));
            fillOval(g, orbX[i], orbY[i], orbSize / 2, orbSize / 2);
        }
    }

    private void drawDarkEnergy(Graphics2D g) {
        for (int i = 0; i < darkEnergyCount; i++) {
            // Outer shape
            g.setColor(new Color(120, 0, 200));
            fillOval(g, darkEnergyX[i], darkEnergyY[i], darkEnergySize * 2, darkEnergySize * 2);
            g.setColor(Color.BLACK);
            drawOval(g, darkEnergyX[i], darkEnergyY[i], darkEnergySize * 2, darkEnergySize * 2);

            // Triangle in the middle
            g.setColor(new Color(255, 0, 255));
            Polygon triangle = new Polygon();
            triangle.addPoint(Math.round(darkEnergyX[i]), Math.round(darkEnergyY[i] - 6));
            triangle.addPoint(Math.round(darkEnergyX[i] - 6), Math.round(darkEnergyY[i] + 6));
            triangle.addPoint(Math.round(darkEnergyX[i] + 6), Math.round(darkEnergyY[i] + 6));
            g.fillPolygon(triangle);
        }
    }

    private void checkCollisions() {
        for (int i = 0; i < darkEnergyCount; i++) {
            float distance = distance(characterX, characterY, darkEnergyX[i], darkEnergyY[i]);
            if (distance < characterSize / 2 + darkEnergySize) {
                health--;
                colorType = -1;
                colorDuration = 15;
                resetDarkEnergy(i);
            }
        }

        for (int i = 0; i < orbCount; i++) {
            float distance = distance(characterX, characterY, orbX[i], orbY[i]);
            if (distance < characterSize / 2 + orbSize / 2) {
                score += 10;
                colorType = 1;
                colorDuration = 15;
                resetOrb(i);
            }
        }
    }

    private void resetDarkEnergy(int i) {
        darkEnergyX[i] = randomRange(100, WIDTH - 100);
        darkEnergyY[i] = -darkEnergySize - randomRange(0, 200);
    }

    private void resetOrb(int i) {
        orbX[i] = randomRange(100, WIDTH - 100);
        orbY[i] = randomRange(100, HEIGHT - 100);
        orbType[i] = random.nextInt(4);
    }

    private int timeInSeconds() {
        return time / FPS;
    }

    private void drawScoreBoard(Graphics2D g) {
        final int panelX = 10;
        final int panelY = 10;
        final int visibleHealth = Math.max(0, Math.min(health, maxHealth));

        // Compact panel
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRoundRect(panelX, panelY, 165, 70, 10, 10);

        // Title
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(new Color(0, 255, 255));
        g.drawString("HUD", panelX + 8, panelY + 17);

        // Score
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, panelX + 8, panelY + 32);

        // Hearts stay in fixed slots so the HUD does not shift as lives change.
        g.drawString("Lives", panelX + 8, panelY + 49);
        for (int i = 0; i < maxHealth; i++) {
            drawHeart(g, panelX + 75 + i * 20, panelY + 40, i < visibleHealth);
        }

        // Time
        g.drawString("Time: " + timeInSeconds(), panelX + 8, panelY + 63);
    }

    private void drawHeart(Graphics2D g, int x, int y, boolean filled) {
        Path2D heart = new Path2D.Float();
        heart.moveTo(x + 8, y + 15);
        heart.curveTo(x + 6, y + 13, x + 1, y + 10, x + 1, y + 5);
        heart.curveTo(x + 1, y + 2, x + 3, y, x + 5, y);
        heart.curveTo(x + 7, y, x + 8, y + 2, x + 8, y + 3);
        heart.curveTo(x + 8, y + 2, x + 9, y, x + 11, y);
        heart.curveTo(x + 13, y, x + 15, y + 2, x + 15, y + 5);
        heart.curveTo(x + 15, y + 10, x + 10, y + 13, x + 8, y + 15);
        heart.closePath();

        if (filled) {
            g.setColor(new Color(255, 80, 95));
            g.fill(heart);
        } else {
            g.setColor(new Color(255, 255, 255, 35));
            g.fill(heart);
        }

        g.setColor(new Color(160, 20, 35));
        g.draw(heart);
    }

    private void drawGameOver(Graphics2D g) {
        if (!gameOver) {
            return;
        }

        String message;
        if (health <= 0) {
            message = "You Lost!";
        } else if (score >= maxScore) {
            message = "You Win!";
        } else {
            message = "No Time! Final Score: " + score;
        }

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        int textWidth = g.getFontMetrics().stringWidth(message);
        g.setColor(Color.WHITE);
        g.drawString(message, (WIDTH - textWidth) / 2, HEIGHT / 2);
    }

    private void setMovementKey(int keyCode, boolean pressed) {
        if (keyCode == KeyEvent.VK_W) {
            upPressed = pressed;
        } else if (keyCode == KeyEvent.VK_S) {
            downPressed = pressed;
        } else if (keyCode == KeyEvent.VK_A) {
            leftPressed = pressed;
        } else if (keyCode == KeyEvent.VK_D) {
            rightPressed = pressed;
        }
    }

    private void moveCharacterToMouse(MouseEvent event) {
        characterX = event.getX();
        characterY = event.getY();
    }

    private void fillOval(Graphics2D g, float centerX, float centerY, float width, float height) {
        g.fillOval(Math.round(centerX - width / 2), Math.round(centerY - height / 2), Math.round(width), Math.round(height));
    }

    private void drawOval(Graphics2D g, float centerX, float centerY, float width, float height) {
        g.drawOval(Math.round(centerX - width / 2), Math.round(centerY - height / 2), Math.round(width), Math.round(height));
    }

    private float randomRange(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(2);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }
}
