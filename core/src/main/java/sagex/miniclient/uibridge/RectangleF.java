package sagex.miniclient.uibridge;

public class RectangleF {
    public float x;
    public float y;
    public float width;
    public float height;

    public RectangleF(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }

    public RectangleF() {
        this(0,0,0,0);
    }

    public boolean update(float x, float y, float w, float h) {
        if (this.x==x && this.y==y && this.width==w && this.height==h ) {
            return false;
        }

        this.x=x;
        this.y=y;
        this.width=w;
        this.height=h;
        return true;
    }

    public RectangleF copy() {
        return new RectangleF(x, y, width, height);
    }
    public Rectangle asIntRect() {
        return new Rectangle(Math.round(x), Math.round(y), Math.round(width), Math.round(height));
    }

    /**
     * Given this rectangle in the Source Coordinate system, translateImmutable to the destination Coordinate System.
     * This returns a new Rectangle with the source translated to the destination.
     * @param src
     * @param dest
     */
    public RectangleF translateImmutable(RectangleF src, RectangleF dest) {
        return this.copy().translate(src,dest);
    }

    /**
     * Given this rectangle in the Source Coordinate system, translateImmutable to the destination Coordinate System.
     * This modifies the current rectangle and returns it
     * @see http://gamedev.stackexchange.com/questions/32555/how-do-i-convert-between-two-different-2d-coordinate-systems
     * @param src
     * @param dest
     */
    public RectangleF translate(RectangleF src, RectangleF dest) {
        update(
                translate(x, src.x, src.x+src.width, dest.x, dest.x+dest.width),
                translate(y, src.y, src.y+src.height, dest.y, dest.y+dest.height),
                translate(width, src.x, src.x+src.width, dest.x, dest.x+dest.width),
                translate(height, src.y, src.y+src.height, dest.y, dest.y+dest.height));
        return this;
    }


    float translate(float src, float src_min, float src_max, float res_min, float res_max) {
        return ( src - src_min ) / ( src_max - src_min ) * ( res_max - res_min ) + res_min;
    }

    /**
     * Scales THIS rectangle to dest rectangle (w = dest.w * dest.w / w and h = dest.h * dest.h / h)
     * such that our rectangle will be larger than the dest rectangle,
     * and dest generally fits inside of our rectangle
     * @param dest
     * @return
     */
    public RectangleF aspectRatioScale(RectangleF dest) {
        width = dest.width * dest.width / width;
        height = dest.height * dest.height / height;
        return this;
    }

    /**
     * Creates a new rectangle that scales THIS rectangle to dest rectangle (w = dest.w * dest.w / w) such that our rectangle
     * will be larger than the dest rectangle, and dest generally fits inside of our rectangle
     * @param dest
     * @return
     */
    public RectangleF aspectRatioScaleImmutable(RectangleF dest) {
        return this.copy().aspectRatioScale(dest);
    }

    /**
     * Apply the scale to THIS rectangle.
     * @param scale
     * @return
     */
    public RectangleF scale(float scale) {
        this.width =  this.width * scale;
        this.height = this.height * scale;
        return this;
    }

    /**
     * Creates a new rectangle with the new scale
     *
     * @param scale
     * @return
     */
    public RectangleF scaleImmutable(float scale) {
        return this.copy().scale(scale);
    }

    /**
     * Returns a new Rectangle, centered in dest.
     *
     * @param dest
     * @return
     */
    public RectangleF centerImmutable(RectangleF dest) {
        return this.copy().center(dest);
    }

    /**
     * Center THIS rectangle in dest.
     * @param dest
     * @return
     */
    public RectangleF center(RectangleF dest) {
        x = (dest.width - width)/2;
        y = (dest.height - height)/2;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Rectangle{");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append('}');
        return sb.toString();
    }

    public DimensionF getDimension() {
        return new DimensionF(width,height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RectangleF rectangle = (RectangleF) o;

        //if (x != rectangle.x) return false;
        float epsilon = 0.00000001f;
        if (!(Math.abs(x-rectangle.x) < epsilon)) return false;
        //if (y != rectangle.y) return false;
        if (!(Math.abs(y-rectangle.y) < epsilon)) return false;
        //if (width != rectangle.width) return false;
        if (!(Math.abs(width-rectangle.width) < epsilon)) return false;
        //return height == rectangle.height;
        return Math.abs(height - rectangle.height) < epsilon;
    }

    @Override
    public int hashCode() {
        float result = x;
        result = 31 * result + y;
        result = 31 * result + width;
        result = 31 * result + height;
        return (int)result;
    }

    public boolean update(RectangleF srcRect) {
        return this.update(srcRect.x, srcRect.y, srcRect.width, srcRect.height);
    }

    public boolean update(Rectangle srcRect) {
        return this.update(srcRect.x, srcRect.y, srcRect.width, srcRect.height);
    }


    public float getAR() {
        if (height<0) return 1;
        return width/height;
    }

    public RectangleF position(int x, int y) {
        this.x=x;
        this.y=y;
        return this;
    }

    /**
     * Keeps the width, and changes the height due to the new aspect ratio.
     * @param ar
     * @return
     */
    public RectangleF updateHeightUsingAspectRatio(float ar) {
        this.height=this.width/ar;
        return this;
    }

    public RectangleF updateWidthUsingAspectRatio(float ar) {
        this.width=this.height*ar;
        return this;
    }
}
