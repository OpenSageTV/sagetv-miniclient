package sagex.miniclient.gl;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import sagex.miniclient.ImageHolder;

/**
 * Created by seans on 14/09/15.
 */
public class FrameBufferHolder extends ImageHolder<Object> {
    public FrameBufferHolder() {
        super(null);
    }

    @Override
    public Object get() {
        while (super.get()==null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.interrupted();
                throw new RuntimeException("Failed to get() FrameBuffer");
            }
        }
        return super.get();
    }
}
