import processing.core.PApplet;

public class Matrix {
    private int rows, cols; // dimensions of matrix/array
    private float[][] matrix; // the 2d array matrix

    /**
     * constructor
     * @param rows for matrix
     * @param cols for matrix
     */
    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;

        matrix = new float[rows][cols];
    }

    /**
     * constructor for copying arrays
     * @param matrix just matrix
     */
    public Matrix(float[][] matrix) {
        this.matrix = matrix;

        this.rows = matrix.length;
        this.cols = matrix[0].length;
    }

    /**
     * dot matrix multiplication
     * @param n the matrix your multiplying
     * @return a matrix containing the result
     */
    public Matrix dot(Matrix n) {
        Matrix result = new Matrix(rows, n.cols);


        if (cols == n.rows) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < n.cols; j++) {
                    float sum = 0;
                    for (int k = 0; k < cols; k++) {
                        sum += matrix[i][k] * n.matrix[k][j];
                    }
                    result.matrix[i][j] = sum;
                }
            }
        }
        return result;



    }

    /**
     * randmoize the array
     */
    public void randomize() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = new PApplet().random(-1, 1);
            }
        }
    }

    /**
     * @return a copy of matrix in array form
     */
    public float[] toArray() {
        float[] arr = new float[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                arr[j + i * cols] = matrix[i][j];
            }
        }
        return arr;
    }

    /**
     * for making a matrix for the inputs
     * @param arr array of any length(intended for inputs)
     * @return a matrix that contains the values in arr
     */
    public Matrix singleColumnMatrixFromArray(float[] arr) {
        Matrix n = new Matrix(arr.length, 1);
        for (int i = 0; i < arr.length; i++) {
            n.matrix[i][0] = arr[i];
        }
        return n;
    }

    /**
     * add a bias to the last row of the inputs
     * @return a matrix with added biases
     */
    public Matrix addBias() {
        Matrix n = new Matrix(rows + 1, 1);
        for (int i = 0; i < rows; i++) {
            n.matrix[i][0] = matrix[i][0];
        }
        n.matrix[rows][0] = 1;
        return n;
    }

    /**
     * run the activation function on every value
     * @return a matrix of activated values
     */
    public Matrix activate() {
        Matrix n = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                n.matrix[i][j] = relu(matrix[i][j]);
            }
        }
        return n;
    }

    /**
     * runs the relu function on a value
     * @param x a value
     * @return the output of a relu function at x
     */
    private float relu(float x) {
        return PApplet.max(0, x);
    }

    /**
     * mutates the matrix so
     * @param mutationRate percentage of mutation rate per each value
     */
    public void mutate(float mutationRate) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                float rand = new PApplet().random(1);
                if (rand < mutationRate) {
                    matrix[i][j] += new PApplet().randomGaussian() / 5;

                    if (matrix[i][j] > 1) {
                        matrix[i][j] = 1;
                    }
                    if (matrix[i][j] < -1) {
                        matrix[i][j] = -1;
                    }
                }
            }
        }
    }

    /**
     * makes a child with two parents
     * @param partner matrix of a partner
     * @return a Child that is a mixture of both
     */
    public Matrix crossover(Matrix partner) {
        Matrix child = new Matrix(rows, cols);

        int randC = PApplet.floor(new PApplet().random(cols));
        int randR = PApplet.floor( new PApplet().random(rows));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if ((i < randR) || (i == randR && j <= randC)) {
                    child.matrix[i][j] = matrix[i][j];
                } else {
                    child.matrix[i][j] = partner.matrix[i][j];
                }
            }
        }
        return child;
    }

    /**
     * clone a matrix
     * @return a clone a matrix
     */
    public Matrix clone() {
        Matrix clone  =  new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                clone.matrix[i][j] = matrix[i][j];
            }
        }
        return clone;
    }

    /**
     * @return number of cols in matrix
     */
    public int getCols() {
        return cols;
    }

    /**
     * @return a number of rows in matrix
     */
    public int getRows() {
        return rows;
    }

    /**
     * @return the matrix reference
     */
    public float[][] getMatrix() {
        return matrix;
    }
}
