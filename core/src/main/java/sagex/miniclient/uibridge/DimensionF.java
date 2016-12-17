package sagex.miniclient.uibridge;

public class DimensionF {
    public float width;
    public float height;

    public DimensionF(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public DimensionF(DimensionF size) {
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

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void updateFrom(DimensionF otherDim) {
        update(otherDim.width, otherDim.height);
    }

    public void update(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public boolean equals(float width, float height) {
        return this.width == width && this.height == height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DimensionF dimension = (DimensionF) o;

        if (width != dimension.width) return false;
        return height == dimension.height;

    }

    @Override
    public int hashCode() {
        float result = width;
        result = 31 * result + height;
        return (int)result;
    }

    public void setSize(float width, float height) {
        this.width=width;
        this.height=height;
    }

    public Dimension asIntDimension() {
        return new Dimension((int)width, (int)height);
    }
}
