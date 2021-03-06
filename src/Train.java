public class Train {
    public static void main(String[] args) {
        Population pop = Population.loadPopulation();
        int highscore = 0;

        while (true) {

            if (pop.done()) {
                highscore = pop.bestSnake.getScore();
                pop.calculateFitness();
                pop.naturalSelection();
                pop.savePopulation();
                System.out.println("gen:" + pop.gen + " score:" + pop.bestSnakeScore);
            } else {
                pop.update();
            }
        }
    }

}
