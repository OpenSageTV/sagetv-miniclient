package sagex.miniclient.android.gl.shapes;

/**
 * A very inefficient rectangle...fortunately MiniClient doesn't use it often.  I hope no-one see this :(
 */
public class Rect {
    Line[] lines = new Line[]{new Line(),new Line(), new Line(), new Line()};

    public Rect() {
    }

    public void SetRectXY(float x, float y, float w, float h, int argb, float tw, float th) {
        lines[0].SetLineXY(x, y, x, y+h, argb,tw,th);
        lines[1].SetLineXY(x, y + h, x + w, y + h, argb, tw, th);
        lines[2].SetLineXY(x + w, y + h, x+w, y, argb, tw,th);
        lines[3].SetLineXY(x+w, y, x, y, argb, tw,th);
    }

    public void draw(float[] mvpMatrix) {
        for (Line l: lines) {
            l.draw(mvpMatrix);
        }
    }
}