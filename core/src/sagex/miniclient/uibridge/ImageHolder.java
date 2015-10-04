package sagex.miniclient.uibridge;

public class ImageHolder<T> extends Holder<T> {
    private int width;
    private int height;

    public ImageHolder() {
    }

    public ImageHolder(T val) {
        this(val, 0, 0);
    }

    public ImageHolder(T img, int width, int height) {
        super(img);
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // release resources for this image
    public void flush() {
    }
}
