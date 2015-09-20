package sagex.miniclient.android.gl;

/**
 * Created by seans on 19/09/15.
 */
public class EGLTexture {
    private final int[] texture;

    public EGLTexture(int texture[]) {
        this.texture=texture;
    }

    public int[] get() {
        return texture;
    }
}
