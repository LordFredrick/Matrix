import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class MatrixCode extends JFrame implements Runnable, MouseMotionListener {

	private static final int COLOR_CHANGE_RATE = 1000;
    private static final int NO_DELAYS_PER_YIELD = 16;
    private static final int NUM_BUFFERS = 2;
    private static int MAX_FRAME_SKIPS = 5;
    private long period;
    private int width;
    private int height;
    private int fontHeight;
    private int fontWidth;
    private Font font;
    private Thread animator;
    private GraphicsDevice gd;
    private Graphics g;
    private BufferStrategy bufferStrategy;
    private int columns;
    private MatrixString[] mStrings;
    private Color color;
    private int colorIndex = 0;
    private Color[] colors = {
        MatrixString.MATRIX_GREEN,
        MatrixString.PURPLE,
        MatrixString.STEEL_BLUE,
        MatrixString.GOLD,
        MatrixString.SPICY_PINK,
        MatrixString.TURQUOISE
    };
    private volatile boolean paused = false;
    private volatile boolean running = false;
    private long tick = 0;

    public MatrixCode(String title, long period, String full) {
        super(title);
        
        //this.period = period;
        initFullScreen(full);
        initResources();
        readyForTermination();
        if (full == "full")
        	addMouseMotionListener(this);
        start();
    }

    private void initFullScreen(String full) {
        System.out.println("Starting fullscreen ...");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        gd = ge.getDefaultScreenDevice();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        if (full == "full"){
        	setUndecorated(true);
        	setResizable(false);
        }
        if (full != "full"){
        	setUndecorated(false);
        	setResizable(true);
        }
    	setIgnoreRepaint(true);
        if (!gd.isFullScreenSupported()) {
            System.out.println("FSEM not supported");
            System.exit(0);
        }
        gd.setFullScreenWindow(this);
        setCursor(getToolkit().createCustomCursor(
                new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0),
                "null"));
        width = getBounds().width;
        height = getBounds().height;
        setBufferStrategy();
    }

    private void initResources() {
        System.out.println("Initializing resources ...");/*
         * try { File file = new File("fonts/matrix code nfi.ttf");
         * FileInputStream fis = new FileInputStream(file); font =
         * Font.createFont(Font.TRUETYPE_FONT, fis); } catch
         * (FontFormatException | IOException ex) {
         * Logger.getLogger(MatrixCode.class.getName()).log(Level.SEVERE, null,
         * ex); }
         */
        color = colors[colorIndex];
        font = new Font("MS Arial Unicode", Font.PLAIN, 20);
        bufferStrategy.getDrawGraphics().setFont(font);
        fontWidth = bufferStrategy.getDrawGraphics().getFontMetrics().stringWidth("\u30AA");
        fontHeight = bufferStrategy.getDrawGraphics().getFontMetrics().getHeight();
        columns = width / fontWidth + 1;
        mStrings = new MatrixString[columns];
        for (int i = 0; i < mStrings.length; i++) {
            mStrings[i] = new MatrixString(i * fontWidth, new Random().nextInt(100) * -fontHeight, color);
        }
    }

    private void restoreScreen() {
        Window w = gd.getFullScreenWindow();
        if (w != null) {
            w.dispose();
        }
        gd.setFullScreenWindow(null);
    }

    private void setBufferStrategy() {
        System.out.println("Setting buffer strategy ...");
        try {
            EventQueue.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    createBufferStrategy(NUM_BUFFERS);
                }
            });
        } catch (InterruptedException | InvocationTargetException e) {
            System.out.println("Error creating buffer strategy");
            System.exit(0);
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        bufferStrategy = getBufferStrategy();
    }

    private void start() {
        if (animator == null || !running) {
            animator = new Thread(this);
            animator.start();
        }
    }

    @Override
    public void run() {
        long beforeTime, afterTime, timeDiff, sleepTime;
        long overSleepTime = 0L;
        int noDelays = 0;
        long excess = 0L;

        beforeTime = System.nanoTime();

        running = true;
        System.out.println("Starting main loop ...");
        while (running) {
            matrixUpdate();
            screenUpdate();

            afterTime = System.nanoTime();
            timeDiff = afterTime - beforeTime;
            sleepTime = (period - timeDiff) - overSleepTime;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1000000L);
                } catch (InterruptedException e) {
                }
                overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
            } else {
                excess -= sleepTime;
                overSleepTime = 0L;

                if (++noDelays >= NO_DELAYS_PER_YIELD) {
                    Thread.yield();
                    noDelays = 0;
                }
            }

            beforeTime = System.nanoTime();

            int skips = 0;
            while ((excess > period) && (skips < MAX_FRAME_SKIPS)) {
                excess -= period;
                matrixUpdate();
                skips++;
            }
        }
        restoreScreen();
        System.exit(0);
    }

	/*public static void main(String[] args) {		
		int fps = DEFAULT_FPS;
    	int period = (int) 1000.0 / fps;
        System.out.println("fps: " + fps + "; period: " + period + " ms");
        new MatrixCode("Matrix Rain", period * 1000000L, "full");
    }*/
	
    private void matrixUpdate() {
        if (!paused) {
            tick++;
            if (tick % COLOR_CHANGE_RATE == 0) {
                colorIndex++;
                if (colorIndex >= colors.length) {
                    colorIndex = 0;
                }
                color = colors[colorIndex];
                System.out.println(colorIndex);
            }
            if (tick % Long.MAX_VALUE == 0) {
                tick = 0;
            }

            for (int i = 0; i < mStrings.length; i++) {
                mStrings[i].updateCharacters();
                mStrings[i].updateTick();
                if (mStrings[i].readyToMove()) {
                    mStrings[i].setY(mStrings[i].getY() + fontHeight);
                    MatrixCharacter[] chars = mStrings[i].getCharacters();
                    chars[1].setCharacter(chars[0].getCharacter());
                    for (int j = chars.length - 1; j > 1; j--) {
                        chars[j].setCharacter(chars[j - 1].getCharacter());
                        if (chars[j - 1].getColor().getAlpha() > 50) {
                            chars[j].setColor(chars[j - 1].getColor());
                        }
                    }
                    if (mStrings[i].getY() - (mStrings[i].getSize() - 1) * fontHeight >= height) {
                        mStrings[i] = new MatrixString(mStrings[i].getX(), 0, color);
                    }
                }
            }
        }
    }

    private void screenUpdate() {
        try {
            g = bufferStrategy.getDrawGraphics();
            render(g);
            g.dispose();
            if (!bufferStrategy.contentsLost()) {
                bufferStrategy.show();
            } else {
                System.out.println("Contents Lost");
            }
            Toolkit.getDefaultToolkit().sync();
        } catch (Exception e) {
            running = false;
        }

    }

    private void render(Graphics g) {
        //g.setFont(font);
        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);

        for (int i = 0; i < mStrings.length; i++) {
            MatrixCharacter[] charString = mStrings[i].getCharacters();
            for (int j = 0; j < charString.length; j++) {
                g.setColor(new Color(mStrings[i].getColor().getRed(),
                        mStrings[i].getColor().getGreen(),
                        mStrings[i].getColor().getBlue(),
                        charString[j].getColor().getAlpha() / 9));
                g.fillRect(mStrings[i].getX(), mStrings[i].getY() - (j + 1) * fontHeight, fontWidth, fontHeight);
                g.setColor(charString[j].getColor());
                g.drawString(charString[j].getCharacter(), mStrings[i].getX(), mStrings[i].getY() - j * fontHeight);
            }
        }
    }

    private void readyForTermination() {
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                dispose();
                running = false;
                System.exit(0);
            }
        });
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        dispose();
        running = false;
        System.exit(0);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        dispose();
        running = false;
        System.exit(0);
    }
}
