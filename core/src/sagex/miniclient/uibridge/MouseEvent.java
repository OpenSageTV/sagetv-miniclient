package sagex.miniclient.uibridge;

public class MouseEvent {

	public static final int MOUSE_CLICKED = 500;
	public static final int MOUSE_PRESSED = 500+1;
	public static final int MOUSE_RELEASED = 500+2;
	public static final int MOUSE_MOVED = 500+3;
	public static final int MOUSE_DRAGGED = 500+6;
	public static final int MOUSE_WHEEL = 500+7;
	
	private int id;
	private Object source;
	private long when;
	private int modifiers;
	private int x;
	private int y;
	private int clickCount;
	private int button;
	private int wheelRotation;

	public MouseEvent() {
	}

	public MouseEvent(Object source, int id, long when, int newModifiers, int x, int y, int clickCount, int button, int wheelRotation) {
		this.id=id;
		this.source=source;
		this.when=when;
		this.modifiers=newModifiers;
		this.x=x;
		this.y=y;
		this.clickCount=clickCount;
		this.button=button;
		this.wheelRotation=wheelRotation;
	}

	public int getButton() {
		return button;
	}

	public int getID() {
		return id;
	}

	public int getModifiers() {
		return modifiers;
	}

	public Object getSource() {
		return source;
	}

	public long getWhen() {
		return when;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getClickCount() {
		return clickCount;
	}

	public int getWheelRotation() {
		return wheelRotation;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("MouseEvent{");
		sb.append("button=").append(button);
		sb.append(", id=").append(id);
		sb.append(", source=").append(source);
		sb.append(", when=").append(when);
		sb.append(", modifiers=").append(modifiers);
		sb.append(", x=").append(x);
		sb.append(", y=").append(y);
		sb.append(", clickCount=").append(clickCount);
		sb.append(", wheelRotation=").append(wheelRotation);
		sb.append('}');
		return sb.toString();
	}
}
