package sagex.miniclient.uibridge;

public class Rectangle {
    public int x;
    public int y;
    public int width;
    public int height;

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }

    public boolean update(int x, int y, int w, int h) {
        if (this.x==x && this.y==y && this.width==w && this.height==h ) {
            return false;
        }

        this.x=x;
        this.y=y;
        this.width=w;
        this.height=h;
        return true;
    }

    public Rectangle copy() {
        return new Rectangle(x, y, width, height);
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

    public Dimension getDimension() {
        return new Dimension(width,height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rectangle rectangle = (Rectangle) o;

        if (x != rectangle.x) return false;
        if (y != rectangle.y) return false;
        if (width != rectangle.width) return false;
        return height == rectangle.height;

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }

    public boolean update(Rectangle srcRect) {
        return this.update(srcRect.x, srcRect.y, srcRect.width, srcRect.height);
    }
}
