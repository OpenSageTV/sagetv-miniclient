package sagex.miniclient.android.opengl;

import sagex.miniclient.uibridge.ImageHolder;

public class SurfaceHolder extends ImageHolder<OpenGLSurface> {

    public SurfaceHolder() {
    }

    public SurfaceHolder(OpenGLSurface img, int width, int height) {
        super(img, width, height);
    }

    @Override
    public OpenGLSurface get() {
        return super.get();
    }
}
