package sagex.miniclient.uibridge;

public class ImageHolder<T> extends Holder<T> {
    private int handle=-1;
    private int width;
    private int height;

    public ImageHolder() {
    }

    public ImageHolder(T img, int width, int height) {
        super(img);
        this.width = width;
        this.height = height;
        this.handle = -1;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getHandle() {
        return handle;
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
        this.handle=-1;
        this.width=0;
        this.height=0;
    }

    public void setHandle(int handle) {
        this.handle = handle;
    }
}
