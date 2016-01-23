package sagex.miniclient.android.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

import sagex.miniclient.android.AppUtil;

/**
 * Created by seans on 23/01/16.
 */
public class PlayerSurfaceView extends SurfaceView {
    AspectHelper aspectHelper = null;

    public PlayerSurfaceView(Context context) {
        super(context);
        aspectHelper = new AspectHelper();
    }

    public PlayerSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        aspectHelper = new AspectHelper();
    }

    public PlayerSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        aspectHelper = new AspectHelper();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        aspectHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(aspectHelper.getMeasuredWidth(), aspectHelper.getMeasuredHeight());
        AppUtil.log.debug("PlayerSurfaceView.onMeasure({},{}): Video Size:{}x{}, Measure: {}x{}", widthMeasureSpec, heightMeasureSpec, aspectHelper.getVideoWidth(), aspectHelper.getVideoHeight(), aspectHelper.getMeasuredWidth(), aspectHelper.getMeasuredHeight());
    }

    public void setVideoSize(int width, int height) {
        aspectHelper.setVideoSize(width, height);
    }

    public void setAspectRatioMethod(int method) {
        aspectHelper.setAspectRatio(method);
    }
}
