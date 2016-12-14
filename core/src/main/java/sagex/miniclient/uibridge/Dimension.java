package sagex.miniclient.uibridge;

public class Dimension {
    public int width;
    public int height;

    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Dimension(Dimension size) {
        updateFrom(size);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Dimension{");
        sb.append("height=").append(height);
        sb.append(", width=").append(width);
        sb.append('}');
        return sb.toString();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void updateFrom(Dimension otherDim) {
        update(otherDim.width, otherDim.height);
    }

    public void update(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean equals(int width, int height) {
        return this.width == width && this.height == height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dimension dimension = (Dimension) o;

        if (width != dimension.width) return false;
        return height == dimension.height;

    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }

    public void setSize(int width, int height) {
        this.width=width;
        this.height=height;
    }
}
