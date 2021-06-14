
import processing.core.PApplet;

public class SnakeAI extends PApplet {
    public static final int SIZE = 20;
    public static final int hidden_nodes = 16;
    public static final int hidden_layers = 2;
    final int fps = 100;

    int highscore = 0;

    public static float mutationRate = 0.10f;

    public static boolean replayBest = true;
    public static boolean seeVision = false;


    public static int height = 800, width = 1200;

    Population pop;

    public static PApplet instance;

    public static void main(String[] args) {
        PApplet.main("SnakeAI");
    }


    public void setup() {
        instance = this;

        size(width, height);

        frameRate(fps);
        pop = Population.loadPopulation();
    }

    public void draw() {
        background(130);
        noFill();
        stroke(255);
        line(400, 0, 400, height);
        rectMode(CORNER);
        rect(400 + SIZE, SIZE, width - 400 - 40, height - 40);
        if (pop.done()) {
            highscore = pop.bestSnake.getScore();
            pop.calculateFitness();
            pop.naturalSelection();
            pop.savePopulation();
        } else {
            pop.update();
            pop.show();
        }
        fill(255);
        textSize(25);
        textAlign(LEFT);
        text("GEN : " + pop.gen, 120, 60);
        text("MUTATION RATE : " + mutationRate * 100 + "%", 120, 90);
        text("SCORE : " + pop.bestSnake.getScore(), 120, height - 45);
        text("HIGHSCORE : " + highscore, 120, height - 15);

        textAlign(LEFT);
        textSize(18);
        fill(255, 255, 0);
        text("Teal < 0", 120, height - 75);
        fill(0, 255, 255);
        text("Yellow > 0", 200, height - 75);
    }
}
