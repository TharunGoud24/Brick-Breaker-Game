import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class BrickBreakerGame extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PADDLE_WIDTH = 150;
    private static final int PADDLE_HEIGHT = 20;
    private static final int BALL_SIZE = 20;
    private static final int PADDLE_SPEED = 100;
    private static final int BALL_SPEED = 5;
    private static final int MAX_CHANCES = 2;

    private JPanel paddle;
    private JPanel ball;
    private JPanel[][] bricks;
    private int score;
    private int level;
    private int remainingChances;

    private int ballXDir = BALL_SPEED;
    private int ballYDir = -BALL_SPEED;

    private JLabel scoreLabel;
    private JLabel levelLabel;
    private JLabel chancesLabel;
    private JLabel gameOverLabel;
    private JButton retryButton;

    private boolean gameOver;

    private Clip paddleSound;
    private Clip brickSound;

    public BrickBreakerGame() {
        setTitle("Brick Breaker Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        setLayout(null);

        paddle = new JPanel();
        paddle.setBackground(Color.green);
        paddle.setSize(PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setLocation(WIDTH / 2 - PADDLE_WIDTH / 2, HEIGHT - PADDLE_HEIGHT - 20);
        add(paddle);

        ball = new JPanel();
        ball.setBackground(Color.RED);
        ball.setSize(BALL_SIZE, BALL_SIZE);
        ball.setLocation(WIDTH / 2 - BALL_SIZE / 2, HEIGHT / 2 - BALL_SIZE / 2);
        add(ball);

        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setForeground(Color.BLACK);
        scoreLabel.setBounds(10, 10, 100, 20);
        add(scoreLabel);

        levelLabel = new JLabel("Level: " + level);
        levelLabel.setForeground(Color.BLACK);
        levelLabel.setBounds(WIDTH - 100, 10, 100, 20);
        add(levelLabel);

        chancesLabel = new JLabel("Chances: " + (MAX_CHANCES - remainingChances));
        chancesLabel.setForeground(Color.BLACK);
        chancesLabel.setBounds(WIDTH / 2 - 50, 10, 100, 20);
        add(chancesLabel);

        gameOverLabel = new JLabel("Game Over");
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 36));
        gameOverLabel.setBounds(WIDTH / 2 - 100, HEIGHT / 2 - 50, 200, 50);
        gameOverLabel.setVisible(false);
        add(gameOverLabel);

        retryButton = new JButton("Retry");
        retryButton.setBounds(WIDTH / 2 - 50, HEIGHT / 2 + 20, 100, 30);
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        retryButton.setVisible(false);
        add(retryButton);

        gameOver = false;
        remainingChances = MAX_CHANCES;

        startLevel(1);

        paddleSound = loadSound("bounce.wav");
        brickSound = loadSound("break.wav");

        Timer timer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameOver) {
                    moveBall();
                    checkCollisions();
                    scoreLabel.setText("Score: " + score);
                    chancesLabel.setText("Chances: " + (MAX_CHANCES - remainingChances));
                } else {
                    gameOverLabel.setVisible(true);
                    retryButton.setVisible(true);
                }
                repaint();
            }
        });
        timer.start();

        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                movePaddle(evt);
            }
        });

        setFocusable(true);
        setVisible(true);
    }

    private void startLevel(int level) {
        this.level = level;
        levelLabel.setText("Level: " + level);

        // Initialize bricks for different levels
        switch (level) {
            case 1:
                bricks = new JPanel[5][10];
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 10; j++) {
                        bricks[i][j] = new JPanel();
                        bricks[i][j].setBackground(Color.GREEN);
                        bricks[i][j].setSize(50, 20);
                        bricks[i][j].setLocation(j * 60 + 20, i * 30 + 50);
                        add(bricks[i][j]);
                    }
                }
                break;
            case 2:
                bricks = new JPanel[5][10];
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 10; j++) {
                        if ((i + j) % 2 == 0) {
                            bricks[i][j] = new JPanel();
                            bricks[i][j].setBackground(Color.GREEN);
                            bricks[i][j].setSize(50, 20);
                            bricks[i][j].setLocation(j * 60 + 20, i * 30 + 50);
                            add(bricks[i][j]);
                        }
                    }
                }
                break;
        }

        score = 0;
        gameOver = false;
        gameOverLabel.setVisible(false);
        retryButton.setVisible(false);
        remainingChances = MAX_CHANCES;
    }

    private Clip loadSound(String filename) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filename).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            return clip;
        } catch (Exception e) {
            System.out.println("Error loading sound: " + e.getMessage());
            return null;
        }
    }

    private void playSound(Clip clip) {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    private void movePaddle(java.awt.event.KeyEvent evt) {
        int paddleX = paddle.getX();
        switch (evt.getKeyCode()) {
            case java.awt.event.KeyEvent.VK_LEFT:
                if (paddleX > 0) {
                    paddle.setLocation(paddleX - PADDLE_SPEED, paddle.getY());
                }
                break;
            case java.awt.event.KeyEvent.VK_RIGHT:
                if (paddleX + PADDLE_WIDTH < WIDTH) {
                    paddle.setLocation(paddleX + PADDLE_SPEED, paddle.getY());
                }
                break;
        }
    }

    private void moveBall() {
        int ballX = ball.getX();
        int ballY = ball.getY();

        if (ballX + BALL_SIZE >= WIDTH || ballX <= 0) {
            ballXDir = -ballXDir;
        }

        if (ballY <= 0) {
            ballYDir = -ballYDir;
        }

        if (ballY + BALL_SIZE >= HEIGHT) {
            remainingChances--;
            if (remainingChances > 0) {
                ball.setLocation(WIDTH / 2 - BALL_SIZE / 2, HEIGHT / 2 - BALL_SIZE / 2);
                ballXDir = BALL_SPEED;
                ballYDir = -BALL_SPEED;
            } else {
                gameOver = true;
            }
        }

        ball.setLocation(ballX + ballXDir, ballY + ballYDir);
    }

    private void checkCollisions() {
        // Check collision with paddle
        if (ball.getBounds().intersects(paddle.getBounds())) {
            ballYDir = -ballYDir;
            playSound(paddleSound);
        }

        // Check collision with bricks
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                if (bricks[i][j] != null && bricks[i][j].isVisible() && ball.getBounds().intersects(bricks[i][j].getBounds())) {
                    bricks[i][j].setVisible(false);
                    ballYDir = -ballYDir;
                    score++;
                    playSound(brickSound);
                }
            }
        }

        // Check if all bricks are destroyed
        boolean allBricksDestroyed = true;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                if (bricks[i][j] != null && bricks[i][j].isVisible()) {
                    allBricksDestroyed = false;
                    break;
                }
            }
            if (!allBricksDestroyed) {
                break;
            }
        }

        if (allBricksDestroyed) {
            if (level < 2) {
                startLevel(level + 1);
            } else {
                gameOver = true;
            }
        }
    }

    private void resetGame() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                if (bricks[i][j] != null) {
                    remove(bricks[i][j]);
                }
            }
        }

        startLevel(1);
    }

    public static void main(String[] args) {
        new BrickBreakerGame();
    }
}