package sagex.miniclient.desktop.swing;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BufferedImageTexture {
    BufferedImage image;
    Graphics2D graphics;

    public BufferedImageTexture(int width, int height) {
        setImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setComposite(AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER));
    }

    public BufferedImageTexture(BufferedImage image) {
        setImage(image);
    }

    public void setImage(BufferedImage image) {
        if (image==null) {
            this.image=null;
            this.graphics=null;
        } else {
            this.image = image;
            this.graphics = image.createGraphics();
        }
    }

    public Graphics2D getGraphics() {
        return graphics;
    }

    public BufferedImage getImage() {
        return image;
    }
}
