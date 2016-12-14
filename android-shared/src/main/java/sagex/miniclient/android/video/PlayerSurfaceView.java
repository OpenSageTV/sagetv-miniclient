package sagex.miniclient.android.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;


/**
 * Created by seans on 23/01/16.
 */
public class PlayerSurfaceView extends SurfaceView {
    public PlayerSurfaceView(Context context) {
        super(context);
    }

    public PlayerSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
