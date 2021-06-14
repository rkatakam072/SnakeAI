import processing.core.PApplet;

import java.util.Arrays;

public class NeuralNet {
    private int iNodes, hNodes, oNodes, hLayers;
    private Matrix[] weights;

    NeuralNet(int input, int hidden, int output, int hiddenLayers) {
        iNodes = input;
        hNodes = hidden;
        oNodes = output;
        hLayers = hiddenLayers;

        initializeWeights();
    }

    private void initializeWeights() {
        weights = new Matrix[hLayers + 1];
        weights[0] = new Matrix(hNodes, iNodes + 1);
        for (int i = 1; i < hLayers; i++) {
            weights[i] = new Matrix(hNodes, hNodes + 1);
        }
        weights[weights.length - 1] = new Matrix(oNodes, hNodes + 1);

        for (Matrix w : weights) {
            w.randomize();
        }
    }

    public void mutate(float mr) {
        Arrays.stream(weights).parallel().forEach(w -> {
            w.mutate(mr);
        });
    }

    public float[] getOutput(float[] inputsArr){
        Matrix inputs = weights[0].singleColumnMatrixFromArray(inputsArr);

        Matrix curr_bias = inputs.addBias();

        for(int i=0; i<hLayers; i++) {
            Matrix hidden_ip = weights[i].dot(curr_bias);
            Matrix hidden_op = hidden_ip.activate();
            curr_bias = hidden_op.addBias();
        }

        Matrix output_ip = weights[weights.length-1].dot(curr_bias);
        Matrix output = output_ip.activate();

        return output.toArray();
    }

    public NeuralNet crossover(NeuralNet partner) {
        NeuralNet child = new NeuralNet(iNodes,hNodes,oNodes,hLayers);
        for(int i=0; i<weights.length; i++) {
            child.weights[i] = weights[i].crossover(partner.weights[i]);
        }
        return child;
    }

    public NeuralNet clone() {
        NeuralNet clone = new NeuralNet(iNodes,hNodes,oNodes,hLayers);
        for(int i=0; i<weights.length; i++) {
            clone.weights[i] = weights[i].clone();
        }

        return clone;
    }

   public void show(float x, float y, float w, float h, float[] vision, float[] decision) {
        PApplet window = SnakeAI.instance;
        float space = 5;
        float nSize = (h - (space*(iNodes-2))) / iNodes;
        float nSpace = (w - (weights.length*nSize)) / weights.length;
        float hBuff = (h - (space*(hNodes-1)) - (nSize*hNodes))/2;
        float oBuff = (h - (space*(oNodes-1)) - (nSize*oNodes))/2;

        int maxIndex = 0;
        for(int i = 1; i < decision.length; i++) {
            if(decision[i] > decision[maxIndex]) {
                maxIndex = i;
            }
        }

        int lc = 0;  //Layer Count

        //DRAW NODES
        for(int i = 0; i < iNodes; i++) {  //DRAW INPUTS
            if(vision[i] != 0) {
                window.fill(0,255,0);
            } else {
                window.fill(255);
            }
            window.stroke(0);
            window.ellipseMode(window.CORNER);
            window.ellipse(x,y+(i*(nSize+space)),nSize,nSize);
            window.textSize(nSize/2);
            window.textAlign(window.CENTER,window.CENTER);
            window.fill(0);
            window.text(i,x+(nSize/2),y+(nSize/2)+(i*(nSize+space)));
        }

        lc++;

        for(int a = 0; a < hLayers; a++) {
            for(int i = 0; i < hNodes; i++) {  //DRAW HIDDEN
                window.fill(255);

                if (this.weights[a].toArray()[i] > 0){
                    window.fill(0, 255, 0);
                }
                window.stroke(0);
                window.ellipseMode(window.CORNER);
                window.ellipse(x+(lc*nSize)+(lc*nSpace),y+hBuff+(i*(nSize+space)),nSize,nSize);
            }
            lc++;
        }

        for(int i = 0; i < oNodes; i++) {  //DRAW OUTPUTS
            if(i == maxIndex) {
                window.fill(0,255,0);
            } else {
                window.fill(255);
            }
            window.stroke(0);
            window.ellipseMode(window.CORNER);
            window.ellipse(x+(lc*nSpace)+(lc*nSize),y+oBuff+(i*(nSize+space)),nSize,nSize);
        }

        lc = 1;

        //DRAW WEIGHTS
        for(int i = 0; i < weights[0].getRows(); i++) {  //INPUT TO HIDDEN
            for(int j = 0; j < weights[0].getCols()-1; j++) {
                if(weights[0].getMatrix()[i][j] < 0) {
                    window.stroke(255,255,0);
                } else {
                    window.stroke(0,255,255);
                }
                window.line(x+nSize,y+(nSize/2)+(j*(space+nSize)),x+nSize+nSpace,y+hBuff+(nSize/2)+(i*(space+nSize)));
            }
        }

        lc++;

        for(int a = 1; a < hLayers; a++) {
            for(int i = 0; i < weights[a].getRows(); i++) {  //HIDDEN TO HIDDEN
                for(int j = 0; j < weights[a].getCols()-1; j++) {
                    if(weights[a].getMatrix()[i][j] < 0) {
                        window.stroke(255,255,0);
                    } else {
                        window.stroke(0,255,255);
                    }
                    window.line(x+(lc*nSize)+((lc-1)*nSpace),y+hBuff+(nSize/2)+(j*(space+nSize)),x+(lc*nSize)+(lc*nSpace),y+hBuff+(nSize/2)+(i*(space+nSize)));
                }
            }
            lc++;
        }

        for(int i = 0; i < weights[weights.length-1].getRows(); i++) {  //HIDDEN TO OUTPUT
            for(int j = 0; j < weights[weights.length-1].getCols()-1; j++) {
                if(weights[weights.length-1].getMatrix()[i][j] < 0) {
                    window.stroke(255,255,0);
                } else {
                    window.stroke(0,255,255);
                }
                window.line(x+(lc*nSize)+((lc-1)*nSpace),y+hBuff+(nSize/2)+(j*(space+nSize)),x+(lc*nSize)+(lc*nSpace),y+oBuff+(nSize/2)+(i*(space+nSize)));
            }
        }

        window.fill(0);
        window.textSize(15);
        window.textAlign(window.CENTER,window.CENTER);
        window.text("U",x+(lc*nSize)+(lc*nSpace)+nSize/2,y+oBuff+(nSize/2));
        window.text("D",x+(lc*nSize)+(lc*nSpace)+nSize/2,y+oBuff+space+nSize+(nSize/2));
        window.text("L",x+(lc*nSize)+(lc*nSpace)+nSize/2,y+oBuff+(2*space)+(2*nSize)+(nSize/2));
        window.text("R",x+(lc*nSize)+(lc*nSpace)+nSize/2,y+oBuff+(3*space)+(3*nSize)+(nSize/2));
    }

}
