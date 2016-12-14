package sagex.miniclient.uibridge;

/**
 * Created by seans on 20/09/15.
 */
public class Scale {
    private float xs = 1;
    private float ys = 1;

    public Scale(float xs, float ys) {
        setScale(xs,ys);
    }

    public Scale(Dimension src, Dimension dest) {
        setScale(src,dest);
    }

    public void setScale(float xs, float ys) {
        this.xs = xs;
        this.ys = ys;
    }

    public void setScale(Dimension src, Dimension dest) {
        setScale(((float) dest.width) / (float) src.width, ((float) dest.height) / (float) src.height);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Scale{");
        sb.append("xs=").append(xs);
        sb.append(", ys=").append(ys);
        sb.append('}');
        return sb.toString();
    }

    public float getXScale() {
        return xs;
    }

    public float getYScale() {
        return ys;
    }

    public float xCanvasToScreen(float x) {
        return x * xs;
    }

    public float yCanvasToScreen(float y) {
        return y * ys;
    }

    public float xScreenToCanvas(float x) {
        return x / xs;
    }

    public float yScreenToCanvas(float y) {
        return y / ys;
    }

    public Scale copy() {
        return new Scale(xs, ys);
    }
}
