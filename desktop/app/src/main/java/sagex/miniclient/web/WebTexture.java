package sagex.miniclient.web;

import java.nio.ByteBuffer;

public class WebTexture {
    private ByteBuffer texture;

    public WebTexture() {
    }

    public WebTexture(ByteBuffer image) {
        this.texture=image;
    }

    public ByteBuffer getTexture() {
        return texture;
    }
}
