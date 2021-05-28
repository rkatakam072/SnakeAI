import processing.core.PApplet;
import processing.core.PVector;

public class Food {
    PVector pos;

    Food() {
        int x = 400 + SnakeAI.SIZE + PApplet.floor(new PApplet().random(38)) * SnakeAI.SIZE;
        int y = SnakeAI.SIZE + PApplet.floor(new PApplet().random(38)) * SnakeAI.SIZE;
        pos = new PVector(x, y);
    }

    void show() {
        PApplet window = SnakeAI.instance;
        window.stroke(0);
        window.fill(255, 0, 0);
        window.rect(pos.x, pos.y, SnakeAI.SIZE, SnakeAI.SIZE);
    }

    public Food clone() {
        Food clone = new Food();
        clone.pos.x = pos.x;
        clone.pos.y = pos.y;

        return clone;
    }
}