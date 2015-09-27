package sagex.miniclient.uibridge;

public class Rectangle {
	public int x;
	public int y;
	public int width;
	public int height;

	public Rectangle(int x, int y, int height, int width) {
		this.x=x;
		this.y=y;
		this.height=height;
		this.width=width;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Rectangle{");
		sb.append("height=").append(height);
		sb.append(", x=").append(x);
		sb.append(", y=").append(y);
		sb.append(", width=").append(width);
		sb.append('}');
		return sb.toString();
	}
}
