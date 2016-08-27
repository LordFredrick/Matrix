import java.awt.Color;

public class MatrixCharacter {

    private int fadeRate;
    private int speed;
    private long tick;
    private Color color;
    private String unicode;

    public MatrixCharacter(String unicode, int fadeRate, int speed, Color color) {
        this.unicode = unicode;
        this.fadeRate = fadeRate;
        this.speed = speed;
        this.color = color;
        tick = 0;
    }

    public void setCharacter(String unicode) {
        this.unicode = unicode;
    }

    public String getCharacter() {
        return unicode;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
    public void setColor(int r, int g, int b, int a) {
        if (a < 0)
            a = 0;
        this.color = new Color(r, g, b, a);
    }

    public Color getColor() {
        return color;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
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

    public boolean readyToFade() {
        if (fadeRate != 0) {
            if (tick % fadeRate == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean readyToChange() {
        if (tick % speed == 0) {
            return true;
        }
        return false;
    }
}
