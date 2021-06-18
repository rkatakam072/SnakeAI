import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Objects;

import static processing.core.PApplet.floor;
import static processing.core.PApplet.pow;

class Snake {

    private int score = 1; // score of the snake/snake-length
    int lifeLeft = 200; // # of moves left for the snake
    int lifetime = 0; // # of moves taken by the snake
    int xVel, yVel; // speed in x and y direction
    int foodItterate = 0;  //used for replay

    float fitness = 0; // the value of how good the snake did

    boolean dead = false; // used to check if the snake is alive
    boolean replay = false; // if this snake is going to be used for replay

    float[] vision; // what the snake/neural-net sees
    float[] decision;// the move the snake decided to do

    PVector head; // the head of the snake

    ArrayList<PVector> body; // the locations of all of the body squares
    ArrayList<Food> foodList;  // used to replay the best snake

    Food food; // food for snake
    NeuralNet brain; // neural net of the snake(used to make a move)

    /**
     * constructor of snake
     */
    Snake() {
        this(SnakeAI.hidden_layers);
    }

    /**
     * constructor
     * @param layers how many hidden layers in the neural network
     */
    Snake(int layers) {
        head = new PVector(800, SnakeAI.height / 2f);
        food = new Food();
        body = new ArrayList<PVector>();

        vision = new float[24];
        decision = new float[4];
        foodList = new ArrayList<Food>();
        foodList.add(food.clone());
        brain = new NeuralNet(24, SnakeAI.hidden_nodes, 4, layers);
        body.add(new PVector(800, (SnakeAI.height / 2f) + SnakeAI.SIZE));
        body.add(new PVector(800, (SnakeAI.height / 2f) + (2 * SnakeAI.SIZE)));
        score += 2;

    }

    /**
     * this constructor is to setup the replay
     * @param foods
     */
    Snake(ArrayList<Food> foods) {
        replay = true;
        vision = new float[24];
        decision = new float[4];
        body = new ArrayList<PVector>();
        foodList = new ArrayList<Food>(foods.size());
        for (Food f : foods) {
            foodList.add(f.clone());
        }
        food = foodList.get(foodItterate);
        foodItterate++;
        head = new PVector(800, SnakeAI.height / 2f);
        body.add(new PVector(800, (SnakeAI.height / 2f) + SnakeAI.SIZE));
        body.add(new PVector(800, (SnakeAI.height / 2f) + (2 * SnakeAI.SIZE)));
        score += 2;
    }

    /**
     * checks if the coordinate colide with the food
     */
    boolean bodyCollide(float x, float y) {
        for (int i = 0; i < body.size(); i++) {
            if (x == body.get(i).x && y == body.get(i).y) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks if a coordinate is on the food
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if it collides with food top left coordinate
     */
    boolean foodCollide(float x, float y) {
        if (x == food.pos.x && y == food.pos.y) {
            return true;
        }
        return false;
    }

    /**
     * check if a position collides with the wall
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if it collides with the wall
     */
    boolean wallCollide(float x, float y) {
        if (x >= SnakeAI.width - (SnakeAI.SIZE) || x < 400 + SnakeAI.SIZE || y >= SnakeAI.height - (SnakeAI.SIZE) || y < SnakeAI.SIZE) {
            return true;
        }
        return false;
    }

    /**
     * displays the snake on the window
     */
    void show() {
        PApplet window = SnakeAI.instance;
        food.show();
        window.fill(255);
        window.stroke(0);
        for (int i = 0; i < body.size(); i++) {
            window.rect(body.get(i).x, body.get(i).y, SnakeAI.SIZE, SnakeAI.SIZE);
        }
        if (dead) {
            window.fill(150);
        } else {
            window.fill(0, 0, 255);
        }
        window.rect(head.x, head.y, SnakeAI.SIZE, SnakeAI.SIZE);
    }

    /**
     * moves the sake if its not dead
     * also updates necessary variables.
     */
    void move() {
        if (!dead) {

            lifetime++;
            lifeLeft--;

            if (foodCollide(head.x, head.y)) {
                eat();
            }
            shiftBody();
            if (wallCollide(head.x, head.y)) {
                dead = true;
            } else if (bodyCollide(head.x, head.y)) {
                dead = true;
            } else if (lifeLeft <= 0) {
                dead = true;
            }
        }
    }

    /**
     * eats the food
     * also increases timeLeft
     */
    void eat() {
        int len = body.size() - 1;
        score++;
            if (lifeLeft < 500) {
                if (lifeLeft > 400) {
                    lifeLeft = 500;
                } else {
                    lifeLeft += 100;
                }
            }

        if (len >= 0) {
            body.add(new PVector(body.get(len).x, body.get(len).y));
        } else {
            body.add(new PVector(head.x, head.y));
        }
        if (!replay) {
            food = new Food();
            while (bodyCollide(food.pos.x, food.pos.y)) {
                food = new Food();
            }
                foodList.add(food);

        } else {
            food = foodList.get(foodItterate);
            foodItterate++;
        }
    }

    /**
     * shift the body of the snake(used to show how to move)
     */
    void shiftBody() {
        float tempx = head.x;
        float tempy = head.y;
        head.x += xVel;
        head.y += yVel;
        float temp2x;
        float temp2y;
        for (int i = 0; i < body.size(); i++) {
            temp2x = body.get(i).x;
            temp2y = body.get(i).y;
            body.get(i).x = tempx;
            body.get(i).y = tempy;
            tempx = temp2x;
            tempy = temp2y;
        }
    }

    /**
     * @return a clone of snake used in replay
     */
    Snake cloneForReplay() {
        Snake clone = new Snake(foodList);
        clone.brain = brain.clone();
        return clone;
    }

    /**
     * @return a clone of this instance
     */
    public Snake clone() {
        Snake clone = new Snake(SnakeAI.hidden_layers);
        clone.brain = brain.clone();
        return clone;
    }

    /**
     * crossover a snake with the parent
     * @param parent a partner
     * @return a child snake
     */
    Snake crossover(Snake parent) {
        Snake child = new Snake(SnakeAI.hidden_layers);
        child.brain = brain.crossover(parent.brain);
        return child;
    }

    /**
     * mutate the brain
     */
    void mutate() {  //mutate the snakes brain
        brain.mutate(SnakeAI.mutationRate);
    }

    /**
     * this is the function to determine how good this snake is(fitness)
     */
    void calculateFitness() {
        if (score < 10) {
            fitness = floor(lifetime * lifetime) * pow(2, score);
        } else {
            fitness = floor(lifetime * lifetime);
            fitness *= pow(2, 10);
            fitness *= (score - 9);
        }
    }

    /**
     * sets the snakes vision
     */
    void look() {
        vision = new float[24];
        float[] temp = lookInDirection(new PVector(-SnakeAI.SIZE, 0));
        vision[0] = temp[0];
        vision[1] = temp[1];
        vision[2] = temp[2];
        temp = lookInDirection(new PVector(-SnakeAI.SIZE, -SnakeAI.SIZE));
        vision[3] = temp[0];
        vision[4] = temp[1];
        vision[5] = temp[2];
        temp = lookInDirection(new PVector(0, -SnakeAI.SIZE));
        vision[6] = temp[0];
        vision[7] = temp[1];
        vision[8] = temp[2];
        temp = lookInDirection(new PVector(SnakeAI.SIZE, -SnakeAI.SIZE));
        vision[9] = temp[0];
        vision[10] = temp[1];
        vision[11] = temp[2];
        temp = lookInDirection(new PVector(SnakeAI.SIZE, 0));
        vision[12] = temp[0];
        vision[13] = temp[1];
        vision[14] = temp[2];
        temp = lookInDirection(new PVector(SnakeAI.SIZE, SnakeAI.SIZE));
        vision[15] = temp[0];
        vision[16] = temp[1];
        vision[17] = temp[2];
        temp = lookInDirection(new PVector(0, SnakeAI.SIZE));
        vision[18] = temp[0];
        vision[19] = temp[1];
        vision[20] = temp[2];
        temp = lookInDirection(new PVector(-SnakeAI.SIZE, SnakeAI.SIZE));
        vision[21] = temp[0];
        vision[22] = temp[1];
        vision[23] = temp[2];
    }

    /**
     * @param direction the direction to be looking in
     * @return a float of the values in the direction
     *         if theres food, if theres a body, and distance to wall
     */
    float[] lookInDirection(PVector direction) {
        PApplet window;
        window = Objects.requireNonNullElseGet(SnakeAI.instance, PApplet::new);

        float look[] = new float[3];
        PVector pos = new PVector(head.x, head.y);
        float distance = 0;
        boolean foodFound = false;
        boolean bodyFound = false;
        pos.add(direction);
        distance += 1;
        while (!wallCollide(pos.x, pos.y)) {
            if (!foodFound && foodCollide(pos.x, pos.y)) {
                foodFound = true;
                look[0] = 1;
            }
            if (!bodyFound && bodyCollide(pos.x, pos.y)) {
                bodyFound = true;
                look[1] = 1;
            }
            if (replay && SnakeAI.seeVision) {
                window.stroke(0, 255, 0);
                window.point(pos.x, pos.y);
                if (foodFound) {
                    window.noStroke();
                    window.fill(255, 255, 51);
                    window.ellipseMode(window.CENTER);
                    window.ellipse(pos.x, pos.y, 5, 5);
                }
                if (bodyFound) {
                    window.noStroke();
                    window.fill(102, 0, 102);
                    window.ellipseMode(window.CENTER);
                    window.ellipse(pos.x, pos.y, 5, 5);
                }
            }
            pos.add(direction);
            distance += 1;
        }
        if (replay && SnakeAI.seeVision) {
            window.noStroke();
            window.fill(0, 255, 0);
            window.ellipseMode(window.CENTER);
            window.ellipse(pos.x, pos.y, 5, 5);
        }
        look[2] = 1 / distance;
        return look;
    }

    /**
     * let the snake think and set decision
     */
    void think() {
        decision = brain.getOutput(vision);
        int maxIndex = 0;
        float max = 0;
        for (int i = 0; i < decision.length; i++) {
            if (decision[i] > max) {
                max = decision[i];
                maxIndex = i;
            }
        }

        switch (maxIndex) {
            case 0:
                moveUp();
                break;
            case 1:
                moveDown();
                break;
            case 2:
                moveLeft();
                break;
            case 3:
                moveRight();
                break;
        }
    }

    /**
     * moves the snake up
     */
    void moveUp() {
        if (yVel != SnakeAI.SIZE) {
            xVel = 0;
            yVel = -SnakeAI.SIZE;
        }
    }
    /**
     * moves the snake down
     */
    void moveDown() {
        if (yVel != -SnakeAI.SIZE) {
            xVel = 0;
            yVel = SnakeAI.SIZE;
        }
    }

    /**
     * moves snake to the left
     */
    void moveLeft() {
        if (xVel != SnakeAI.SIZE) {
            xVel = -SnakeAI.SIZE;
            yVel = 0;
        }
    }

    /**
     * moves snake to the right
     */
    void moveRight() {
        if (xVel != -SnakeAI.SIZE) {
            xVel = SnakeAI.SIZE;
            yVel = 0;
        }
    }

    /**
     * @return score of this snake
     */
    public int getScore() {
        return score;
    }
}