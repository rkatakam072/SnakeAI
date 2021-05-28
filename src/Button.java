import processing.core.PApplet;

class Button {
    float X, Y, W, H;
    String text;
    Button(float x, float y, float w, float h, String t) {
        X = x;
        Y = y;
        W = w;
        H = h;
        text = t;
    }

    boolean collide(float x, float y) {
        if(x >= X-W/2 && x <= X+W/2 && y >= Y-H/2 && y <= Y+H/2) {
            return true;
        }
        return false;
    }

    void show() {
        PApplet window = SnakeAI.instance;
        window.fill(255);
        window.stroke(0);
        window.rectMode(window.CENTER);
        window.rect(X, Y, W, H);
        window.textSize(22);
        window.textAlign(window.CENTER,window.CENTER);
        window.fill(0);
        window.noStroke();
        window.text(text,X,Y-3);
    }
}