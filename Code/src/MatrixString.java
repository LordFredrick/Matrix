import java.awt.Color;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatrixString {

    public static final Color MATRIX_GREEN;
    public static final Color SPICY_PINK;
    public static final Color PURPLE;
    public static final Color TURQUOISE;
    public static final Color GOLD;
    public static final Color STEEL_BLUE;
    private static final int MIN_FADE_RATE = 1;
    private static final int MAX_FADE_RATE = 50;
    private static final int MIN_LENGTH = 20;
    private static final int MAX_LENGTH = 60;
    private static final int MIN_CHANGE_RATE = 50;
    private static final int MAX_CHANGE_RATE = 120;
    private static final int MIN_DROP_RATE = 6;
    private static final int MAX_DROP_RATE = 13;
    private int x, y;
    private int size;
    private int speed;
    private Color color;
    private long tick;
    private Random rand;
    private MatrixCharacter[] charString;

    static {
        MATRIX_GREEN = new Color(0, 255, 0);
        SPICY_PINK = new Color(255, 28, 174);
        PURPLE = new Color(160, 32, 240);
        TURQUOISE = new Color(173, 234, 234);
        GOLD = new Color(255, 215, 0);
        STEEL_BLUE = new Color(35, 107, 142);
    }

    public MatrixString(int x, int y, Color c) {
        this.x = x;
        this.y = y;
        color = c;
        rand = new Random();
        size = rand.nextInt(MAX_LENGTH - MIN_LENGTH + 1) + MIN_LENGTH;
        speed = rand.nextInt(MAX_DROP_RATE - MIN_DROP_RATE + 1) + MIN_DROP_RATE;
        charString = new MatrixCharacter[size];
        int alpha = 255;
        charString[0] = new MatrixCharacter(nextRandomCharacter(), 0, 5,
                new Color(255, 255, 255, alpha));
        for (int i = 1; i < charString.length; i++) {
            charString[i] = new MatrixCharacter("",
                    rand.nextInt(MAX_FADE_RATE - MIN_FADE_RATE + 1) + MIN_FADE_RATE,
                    rand.nextInt(MAX_CHANGE_RATE - MIN_CHANGE_RATE + 1) + MIN_CHANGE_RATE,
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            alpha -= 255 / size;
        }
        tick = 0;
    }

    public void updateCharacters() {
        for (int i = 0; i < charString.length; i++) {
            charString[i].updateTick();
            if (charString[i].readyToChange()) {
                charString[i].setCharacter(nextRandomCharacter());
            }
            if (charString[i].readyToFade()) {
                Color c = charString[i].getColor();
                charString[i].setColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() - 3);
            }
        }
    }

    public void resetTick() {
        tick = 0;
    }

    public void updateTick() {
        tick++;
        if (tick == Long.MAX_VALUE) {
            resetTick();
        }
    }

    public boolean readyToMove() {
        if (tick % speed == 0) {
            return true;
        }
        return false;
    }
    

    private String nextRandomCharacter() {
        // Random katakana character
        String orig = "\\u30" + Integer.toHexString(rand.nextInt(96) + 160);

        Pattern p = Pattern.compile("\\\\u[0-9a-fA-F]{4}");

        Matcher m = p.matcher(orig);
        StringBuilder buf = null;

        int pos;
        for (pos = 0; m.find(pos); pos = m.end()) {
            if (null == buf) {
                buf = new StringBuilder(orig.length());
            }

            buf.append(orig.substring(pos, m.start()));

            String hex = m.group().substring(2);
            int c = Integer.valueOf(hex, 16);

            buf.append((char) c);
        }

        if (null == buf) {
            return orig;
        } else {
            buf.append(orig.substring(pos));
            return buf.toString();
        }
    }

    public MatrixCharacter[] getCharacters() {
        return charString;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getSpeed() {
        return speed;
    }

    public int getSize() {
        return size;
    }

    public Color getColor() {
        return color;
    }
}
