package sagex.miniclient.android.video;

import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceView;
import android.view.ViewGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.net.HasPushBuffer;
import sagex.miniclient.net.PushBufferDataSource;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Rectangle;

//import org.videolan.libvlc.LibVLC;

/**
 * Created by seans on 06/10/15.
 */
public abstract class BaseMediaPlayerImpl<Player, DataSource> implements MiniPlayerPlugin {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final MiniClientGDXActivity context;

    protected boolean pushMode;
    protected boolean playerReady;

    protected Player player;
    protected DataSource dataSource;
    protected SurfaceView mSurface;

    // media player
    protected int mVideoWidth;
    protected int mVideoHeight;

    protected boolean createPlayerOnUI = true;
    protected boolean waitForPlayer = true;

    protected int state;

    public BaseMediaPlayerImpl(MiniClientGDXActivity activity, boolean createPlayerOnUI, boolean waitForPlayer) {
        this.context = activity;
        this.mSurface = activity.getVideoView();
        this.createPlayerOnUI = createPlayerOnUI;
        this.waitForPlayer = waitForPlayer;
        state = this.NO_STATE;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void free() {
        log.info("Freeing Media Player");
        releasePlayer();
    }

    @Override
    public void setPushMode(boolean b) {
        this.pushMode = b;
    }

    @Override
    public void load(byte b, byte b1, String s, final String urlString, Object o, boolean b2, int i) {
        if (createPlayerOnUI) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    releasePlayer();
                    state = LOADED_STATE;
                    setupPlayer(urlString);
                    if (dataSource == null)
                        throw new RuntimeException("setupPlayer must create a datasource");
                }
            });
        } else {
            releasePlayer();
            state = LOADED_STATE;
            setupPlayer(urlString);
            if (dataSource == null)
                throw new RuntimeException("setupPlayer must create a datasource");
        }
    }

    protected abstract void setupPlayer(String sageTVurl);

    /**
     * Waits until the player has been contructed
     */
    void waitForPlayer() {
        if (!waitForPlayer) return;

        log.debug("wait for player");
        while (!playerReady) {
            try {
                Thread.sleep(50);
                log.debug("wait for player...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public long getMediaTimeMillis() {
        return 0;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void setMute(boolean b) {

    }

    @Override
    public void stop() {
        state = STOPPED_STATE;
    }

    protected void clearSurface() {
        if (mSurface != null) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.debug("Clearing Canvas");
                        Canvas canvas = mSurface.getHolder().lockCanvas(null);
                        canvas.drawColor(Color.BLACK);
                        mSurface.getHolder().unlockCanvasAndPost(canvas);
                    } catch (Throwable t) {
                        log.debug("Failed to clear canvas");
                    }
                }
            });
        }
    }

    @Override
    public void pause() {
        state = PAUSE_STATE;
        log.debug("pause()");
        if (createPlayerOnUI) waitForPlayer();
    }

    @Override
    public void play() {
        state = PLAY_STATE;
        log.debug("play()");
        waitForPlayer();
    }

    @Override
    public void seek(long timeInMS) {
        log.debug("SEEK: {}", timeInMS);
    }

    @Override
    public void inactiveFile() {
        log.debug("INACTIVEFILE Called??");
    }

    @Override
    public long getLastFileReadPos() {
        if (dataSource instanceof HasPushBuffer) {
            return ((HasPushBuffer) dataSource).getBytesRead();
        } else {
            // return (long)player.getPosition();
            return 0;
        }
    }

    @Override
    public int getVolume() {
        return 0;
    }

    @Override
    public int setVolume(float v) {
        return 0;
    }

    @Override
    public void setVideoRectangles(Rectangle srcRect, Rectangle destRect, boolean b) {

    }

    @Override
    public Dimension getVideoDimensions() {
        return null;
    }

    @Override
    public void pushData(byte[] cmddata, int bufDataOffset, int buffSize) throws IOException {
        //log.debug("pushData()");
        if (dataSource instanceof HasPushBuffer) {
            ((HasPushBuffer) dataSource).pushBytes(cmddata, bufDataOffset, buffSize);
        }
    }

    @Override
    public void flush() {
        log.debug("flush()");
        if (dataSource instanceof HasPushBuffer) {
            ((HasPushBuffer) dataSource).flush();
        }
    }

    @Override
    public int getBufferLeft() {
        if (dataSource instanceof HasPushBuffer) {
            return ((HasPushBuffer) dataSource).bufferAvailable();
        } else {
            return PushBufferDataSource.PIPE_SIZE;
        }
    }

    @Override
    public void run() {

    }

    protected void setSize(final int width, final int height) {
        log.info("Set Size Called: {}x{}", width, height);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log.info("Set Size Called On UI Thread: {}x{}", width, height);
                mVideoWidth = width;
                mVideoHeight = height;
                if (mVideoWidth * mVideoHeight <= 1)
                    return;

                // get screen size
                int w = context.getWindow().getDecorView().getWidth();
                int h = context.getWindow().getDecorView().getHeight();

                // getWindow().getDecorView() doesn't always take orientation into
                // account, we have to correct the values
                boolean isPortrait = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
                if (w > h && isPortrait || w < h && !isPortrait) {
                    int i = w;
                    w = h;
                    h = i;
                }

                float videoAR = (float) mVideoWidth / (float) mVideoHeight;
                float screenAR = (float) w / (float) h;

                if (screenAR < videoAR)
                    h = (int) (w / videoAR);
                else
                    w = (int) (h * videoAR);

                // force surface buffer size
                mSurface.getHolder().setFixedSize(mVideoWidth, mVideoHeight);

                // set display size
                ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
                lp.width = w;
                lp.height = h;
                mSurface.setLayoutParams(lp);
                mSurface.invalidate();
            }
        });
    }

    protected void releasePlayer() {
        log.debug("Releasing Player");
        player = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
        releaseDataSource();
        dataSource = null;
        state = NO_STATE;
    }

    protected void releaseDataSource() {
        if (dataSource instanceof HasPushBuffer) {
            ((HasPushBuffer) dataSource).release();
        }
    }
}
