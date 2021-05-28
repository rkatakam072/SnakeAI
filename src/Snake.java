import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PApplet.floor;
import static processing.core.PApplet.pow;

class Snake {

    int score = 1;
    int lifeLeft = 200;
    int lifetime = 0;
    int xVel, yVel;
    int foodItterate = 0;  //used for replay

    float fitness = 0;

    boolean dead = false;
    boolean replay = false;

    float[] vision;
    float[] decision;

    PVector head;

    ArrayList<PVector> body;
    ArrayList<Food> foodList;  // used to replay the best snake

    Food food;
    NeuralNet brain;

    Snake() {
        this(SnakeAI.hidden_layers);
    }

    Snake(int layers) {
        head = new PVector(800, SnakeAI.height / 2f);
        food = new Food();
        body = new ArrayList<PVector>();
        if (!SnakeAI.humanPlaying) {
            vision = new float[24];
            decision = new float[4];
            foodList = new ArrayList<Food>();
            foodList.add(food.clone());
            brain = new NeuralNet(24, SnakeAI.hidden_nodes, 4, layers);
            body.add(new PVector(800, (SnakeAI.height / 2f) + SnakeAI.SIZE));
            body.add(new PVector(800, (SnakeAI.height / 2f) + (2 * SnakeAI.SIZE)));
            score += 2;
        }
    }

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

    boolean bodyCollide(float x, float y) {
        for (int i = 0; i < body.size(); i++) {
            if (x == body.get(i).x && y == body.get(i).y) {
                return true;
            }
        }
        return false;
    }

    boolean foodCollide(float x, float y) {
        if (x == food.pos.x && y == food.pos.y) {
            return true;
        }
        return false;
    }

    boolean wallCollide(float x, float y) {  //check if a position collides with the wall
        if (x >= SnakeAI.width - (SnakeAI.SIZE) || x < 400 + SnakeAI.SIZE || y >= SnakeAI.height - (SnakeAI.SIZE) || y < SnakeAI.SIZE) {
            return true;
        }
        return false;
    }

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

    void move() {
        if (!dead) {
            if (!SnakeAI.humanPlaying && !SnakeAI.modelLoaded) {
                lifetime++;
                lifeLeft--;
            }
            if (foodCollide(head.x, head.y)) {
                eat();
            }
            shiftBody();
            if (wallCollide(head.x, head.y)) {
                dead = true;
            } else if (bodyCollide(head.x, head.y)) {
                dead = true;
            } else if (lifeLeft <= 0 && !SnakeAI.humanPlaying) {
                dead = true;
            }
        }
    }

    void eat() {
        int len = body.size() - 1;
        score++;
        if (!SnakeAI.humanPlaying && !SnakeAI.modelLoaded) {
            if (lifeLeft < 500) {
                if (lifeLeft > 400) {
                    lifeLeft = 500;
                } else {
                    lifeLeft += 100;
                }
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
            if (!SnakeAI.humanPlaying) {
                foodList.add(food);
            }
        } else {
            food = foodList.get(foodItterate);
            foodItterate++;
        }
    }

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

    Snake cloneForReplay() {
        Snake clone = new Snake(foodList);
        clone.brain = brain.clone();
        return clone;
    }

    public Snake clone() {
        Snake clone = new Snake(SnakeAI.hidden_layers);
        clone.brain = brain.clone();
        return clone;
    }

    Snake crossover(Snake parent) {
        Snake child = new Snake(SnakeAI.hidden_layers);
        child.brain = brain.crossover(parent.brain);
        return child;
    }

    void mutate() {  //mutate the snakes brain
        brain.mutate(SnakeAI.mutationRate);
    }

    void calculateFitness() {
        if (score < 10) {
            fitness = floor(lifetime * lifetime) * pow(2, score);
        } else {
            fitness = floor(lifetime * lifetime);
            fitness *= pow(2, 10);
            fitness *= (score - 9);
        }
    }

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

    float[] lookInDirection(PVector direction) {
        PApplet window = SnakeAI.instance;
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

    void think() {
        decision = brain.output(vision);
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

    void moveUp() {
        if (yVel != SnakeAI.SIZE) {
            xVel = 0;
            yVel = -SnakeAI.SIZE;
        }
    }

    void moveDown() {
        if (yVel != -SnakeAI.SIZE) {
            xVel = 0;
            yVel = SnakeAI.SIZE;
        }
    }

    void moveLeft() {
        if (xVel != SnakeAI.SIZE) {
            xVel = -SnakeAI.SIZE;
            yVel = 0;
        }
    }

    void moveRight() {
        if (xVel != -SnakeAI.SIZE) {
            xVel = SnakeAI.SIZE;
            yVel = 0;
        }
    }
}