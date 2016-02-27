package sagex.miniclient.uibridge;

/**
 * Created by seans on 20/09/15.
 */
public class Scale {
    private float xs = 1;
    private float ys = 1;

    public Scale(float xs, float ys) {
        this.xs = xs;
        this.ys = ys;
    }

    public void setScale(float xs, float ys) {
        this.xs = xs;
        this.ys = ys;
    }

    public void setScale(Dimension uiSize, Dimension screenSize) {
        setScale(((float) screenSize.width) / (float) uiSize.width, ((float) screenSize.height) / (float) uiSize.height);
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
}
