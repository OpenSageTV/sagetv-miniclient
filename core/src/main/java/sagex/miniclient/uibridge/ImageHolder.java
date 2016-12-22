package sagex.miniclient.uibridge;

public class ImageHolder<T> extends Holder<T> {
    private int width;
    private int height;

    public ImageHolder() {
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
    public void dispose() {
        if (get() instanceof  Disposable) {
            try {
                ((Disposable) get()).dispose();
            } catch (Throwable t) {
            }
        }
        set(null);
    }
}
