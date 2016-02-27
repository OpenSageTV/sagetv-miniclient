package sagex.miniclient.android.events;

/**
 * Created by seans on 13/02/16.
 */
public class SetAspectRatioEvent {
    public static final SetAspectRatioEvent NEXT_ASPECT_RATIO = new SetAspectRatioEvent(Integer.MAX_VALUE);

    private final int aspectRatio;

    public SetAspectRatioEvent(int ar) {
        this.aspectRatio = ar;
    }

    public int getAspectRatio() {
        return aspectRatio;
    }

    public boolean isToggleNext() {
        return aspectRatio == Integer.MAX_VALUE;
    }
}
