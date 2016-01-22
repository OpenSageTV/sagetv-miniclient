package sagex.miniclient.android.video;

import android.content.res.Configuration;
import android.view.ViewGroup;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.R;
import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.net.HasPushBuffer;
import sagex.miniclient.net.PushBufferDataSource;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.EventRouter;
import sagex.miniclient.uibridge.Rectangle;
import sagex.miniclient.util.VerboseLogging;

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

    // media player
    protected int mVideoWidth;
    protected int mVideoHeight;

    protected boolean createPlayerOnUI = true;
    protected boolean waitForPlayer = true;

    protected int state;

    protected String lastUri;

    protected long lastMediaTime = 0;

    public BaseMediaPlayerImpl(MiniClientGDXActivity activity, boolean createPlayerOnUI, boolean waitForPlayer) {
        this.context = activity;
        //this.mSurface = activity.getVideoView();
        this.createPlayerOnUI = createPlayerOnUI;
        this.waitForPlayer = waitForPlayer;
        state = NO_STATE;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void free() {
        if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.info("Freeing Media Player");
        releasePlayer();
    }

    @Override
    public void setPushMode(boolean b) {
        this.pushMode = b;
    }

    @Override
    public void load(byte b, byte b1, String s, final String urlString, Object o, boolean b2, int i) {
        lastUri = urlString;
        lastMediaTime = 0;
        log.debug("load(): url: {}", urlString);
        if (createPlayerOnUI) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    releasePlayer();
                    state = LOADED_STATE;

                    context.setupVideoFrame();

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

    public void message(final String msg) {
        if (context==null) {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.error("MESSAGE: {}", msg);
            return;
        }
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                } catch (Throwable t) {
                    log.error("MESSAGE: {}", msg);
                }
            }
        });
    }

    protected void playerFailed() {
        stop();
        releasePlayer();
        state = EOS_STATE;
        notifySageTVStop();
        message(context.getString(R.string.msg_player_failed, lastUri));
    }

    protected void notifySageTVStop() {
        EventRouter.post(MiniclientApplication.get().getClient(), EventRouter.MEDIA_STOP);
    }

    protected abstract long getPlayerMediaTimeMillis();

    @Override
    public long getMediaTimeMillis() {
        if (state == EOS_STATE) return lastMediaTime;
        long mt = getPlayerMediaTimeMillis();
        if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
            //log.debug("getMediaTime(): current: {}, last time: {}", mt, lastMediaTime);
        }
        lastMediaTime = mt;
        return mt;
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
        context.removeVideoFrame();
    }

    protected void clearSurface() {
//        if (mSurface != null) {
//            context.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        log.debug("Clearing Canvas");
//                        Canvas canvas = mSurface.getHolder().lockCanvas(null);
//                        canvas.drawColor(Color.BLACK);
//                        mSurface.getHolder().unlockCanvasAndPost(canvas);
//                    } catch (Throwable t) {
//                        log.debug("Failed to clear canvas");
//                    }
//                }
//            });
//        }
    }

    @Override
    public void pause() {
        state = PAUSE_STATE;
        if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("pause()");
        //if (createPlayerOnUI) waitForPlayer();
    }

    @Override
    public void play() {
        state = PLAY_STATE;
        if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("play()");
        //waitForPlayer();
    }

    @Override
    public void seek(long timeInMS) {
        if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("SEEK: {}", timeInMS);
    }

    @Override
    public void setServerEOS() {
        // Jeff says don't do this
        // tell the datasource that we have all the data
        if (dataSource != null && dataSource instanceof HasPushBuffer) {
            ((HasPushBuffer) dataSource).setEOS();
        }

        log.debug("Server sent us EOS");
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

    Rectangle lastVideoPositionUpdate = null;
    @Override
    public void setVideoRectangles(Rectangle srcRect, final Rectangle destRect, boolean b) {
        if (VerboseLogging.DETAILED_PLAYER_LOGGING)
            log.debug("setVideoRectangles(): SRC: {}, DEST: {}", srcRect, destRect);
        if (lastVideoPositionUpdate == null || !lastVideoPositionUpdate.equals(destRect)) {
            // we need an update
            lastVideoPositionUpdate = destRect;
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                        log.debug("Updating Video UI Size and Location {}", destRect);
                    // TODO: Eventually when we are screen scaling we'll need to adjust these pixels
                    context.updateVideoUI(destRect);
                }
            });
        }
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
        if (VerboseLogging.DETAILED_PLAYER_LOGGING) log.debug("flush()");
        if (dataSource instanceof HasPushBuffer) {
            ((HasPushBuffer) dataSource).flush();
        }
    }

    @Override
    public int getBufferLeft() {
        if (dataSource instanceof HasPushBuffer) {
            if (state == EOS_STATE) return -1;
            return ((HasPushBuffer) dataSource).bufferAvailable();
        } else {
            return PushBufferDataSource.PIPE_SIZE;
        }
    }

    @Override
    public void run() {

    }

    protected void setSize(final int width, final int height) {
        if (VerboseLogging.DETAILED_PLAYER_LOGGING)
            log.info("Set Size Called: {}x{}", width, height);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (VerboseLogging.DETAILED_PLAYER_LOGGING)
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
                context.getVideoView().getHolder().setFixedSize(mVideoWidth, mVideoHeight);

                // set display size
                ViewGroup.LayoutParams lp = context.getVideoView().getLayoutParams();
                lp.width = w;
                lp.height = h;
                context.getVideoView().setLayoutParams(lp);
                context.getVideoView().invalidate();
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
        state = EOS_STATE;
        context.removeVideoFrame();
    }

    protected void releaseDataSource() {
        if (dataSource instanceof HasPushBuffer) {
            ((HasPushBuffer) dataSource).release();
        }
    }
}
