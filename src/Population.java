import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import processing.core.PApplet;

import java.io.*;
import java.util.Arrays;
import java.util.stream.IntStream;

class Population {

    Snake[] snakes; // list of snakes in the species
    Snake bestSnake; // best snake for replay

    int bestSnakeScore = 0; // the score of best snake
    int gen = 0; // generation # of the snake

    float bestFitness = 0; // the fitness of the best snake
    float fitnessSum = 0; // the sum of fitness for a generation

    /**
     * constructor
     * @param size of the population/snakes.length
     */
    Population(int size) {
        snakes = new Snake[size];
        for (int i = 0; i < snakes.length; i++) {
            snakes[i] = new Snake();
        }
        bestSnake = snakes[0].clone();
        bestSnake.replay = true;
    }

    /**
     * @return the population form a file
     */
    public synchronized static Population loadPopulation() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        try {
            return gsonBuilder.create().fromJson(new JsonReader(new FileReader(new File("C:\\Users\\kittu\\IdeaProjects\\SnakeAI\\SavedAI.json"))), Population.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * checks if this generation is done/all dead
     * @return
     */
    boolean done() {
        if (Arrays.stream(snakes).parallel().anyMatch(snake -> !snake.dead)) {
            return false;
        }

        return bestSnake.dead;
    }

    // save population ot a file
    public synchronized void savePopulation() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("C:\\Users\\kittu\\IdeaProjects\\SnakeAI\\SavedAI.json")));
            bufferedWriter.write(gsonBuilder.create().toJson(this));

            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * updates all of the snakes, including the replay snake
     */
    void update() {
        if (!bestSnake.dead) {  // this snake is a replay of the best from the past generation
            bestSnake.look();
            bestSnake.think();
            bestSnake.move();
        }


        /*IntStream.range(0, snakes.length).parallel().forEach(i -> {
            if (!snakes[i].dead) {
                snakes[i].look();
                snakes[i].think();
                snakes[i].move();
            }
        });*/


        Arrays.stream(snakes).parallel().forEach(snake -> {

            if (!snake.dead){
                snake.look();
                snake.think();
                snake.move();
            }
        });
    }

    /**
     * shows the snakes
     */
    void show() {  //show best snake or all the snakes
        if (SnakeAI.replayBest) {
            bestSnake.show();
            bestSnake.brain.show(0, 0, 360, 790, bestSnake.vision, bestSnake.decision);  //show the brain of the best snake
        } else {
            for (int i = 0; i < snakes.length; i++) {
                snakes[i].show();
            }
        }
    }

    /**
     * sets the best snake for this generation
     */
    void setBestSnake() {
        float max = 0;
        int maxIndex = 0;
        for (int i = 0; i < snakes.length; i++) {
            if (snakes[i].fitness > max) {
                max = snakes[i].fitness;
                maxIndex = i;
            }
        }
        if (max > bestFitness) {
            bestFitness = max;
            bestSnake = snakes[maxIndex].cloneForReplay();
            bestSnakeScore = snakes[maxIndex].getScore();
        } else {
            bestSnake = bestSnake.cloneForReplay();
        }
    }

    /**
     * selects a parent where snakes can reproduce
     * @return a snake
     */
    Snake selectParent() {
        float rand = new PApplet().random(fitnessSum);
        float summation = 0;
        for (int i = 0; i < snakes.length; i++) {
            summation += snakes[i].fitness;
            if (summation > rand) {
                return snakes[i];
            }
        }
        return snakes[0];
    }

    /**
     * runs the general logic for the BestSnake picking
     */
    void naturalSelection() {
        Snake[] newSnakes = new Snake[snakes.length];

        setBestSnake();
        calculateFitnessSum();

        newSnakes[0] = bestSnake.clone();

        IntStream.range(1, snakes.length).parallel().forEach(i -> {
            Snake child = selectParent().crossover(selectParent());
            child.mutate();
            newSnakes[i] = child;
        });


        snakes = newSnakes.clone();
        gen += 1;
    }

    /**
     * loop over all the snakes and calculates there fitness
     */
    void calculateFitness() {
        Arrays.stream(snakes).parallel().forEach(Snake::calculateFitness);
    }

    /**
     * loop over all snakes and add there fitness to fitnessSum
     */
    void calculateFitnessSum() {
        fitnessSum = 0;
       /* for (Snake snake : snakes) {
            fitnessSum += snake.fitness;
        }*/
        Arrays.stream(snakes).parallel().forEach(snake -> {

            fitnessSum += snake.fitness;
        });
    }
}
