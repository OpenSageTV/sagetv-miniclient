package sagex.miniclient.android.video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.AppUtil;
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
    protected Dimension videoSize = new Dimension(0, 0);

    protected boolean createPlayerOnUI = true;
    protected boolean waitForPlayer = true;

    protected int state;

    protected String lastUri;

    protected long lastMediaTime = 0;

    protected boolean flushed = false;

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
    public void load(byte majorHint, byte minorHint, String encodingHint, final String urlString, String hostname, boolean timeshifted, long buffersize) {
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
        AppUtil.message(msg);
    }

    protected void playerFailed() {
        stop();
        releasePlayer();
        state = EOS_STATE;
        notifySageTVStop();
        message(context.getString(R.string.msg_player_failed, lastUri));
    }

    protected void notifySageTVStop() {
        // This causes queded up items to fail to play.. so we can't really do this.
        // EventRouter.post(MiniclientApplication.get().getClient(), EventRouter.MEDIA_STOP);
    }

    /**
     * Delegatest the media time to the actual player implementation.  lastServerTime is passed
     * so that if the player needs to adjust the time based on the last time the buffer had flush
     * then it can use this value.
     *
     * @param lastServerTime
     * @return
     */
    protected abstract long getPlayerMediaTimeMillis(long lastServerTime);

    @Override
    public long getMediaTimeMillis(long lastServerTime) {
        if (!playerReady) {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                log.debug("getMediaTimeMillis(): Player not ready, returning 0");
            return 0;
        }
        if (state == EOS_STATE || state == NO_STATE || state == LOADED_STATE) {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                log.debug("getMediaTimeMillis(): Player State Not Ready {} returning last time {}", state, lastMediaTime);
            return lastMediaTime;
        }
        long mt = getPlayerMediaTimeMillis(lastServerTime);
        if (mt <= 0 && state == PAUSE_STATE) {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                log.debug("getMediaTimeMillis(): Player is paused returingin last time: {}", lastMediaTime);
            return lastMediaTime;
        }
        if (flushed && mt < 0) {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING) {
                log.debug("getMediaTimeMillis() is {} after a flush.  Using lastMediaTime: {}, until data shows up.", mt, lastMediaTime);
            }
            return lastMediaTime;
        }
        if (flushed) {
            if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                log.debug("getMediaTimeMillis(): current: {}, last time: {}", mt, lastMediaTime);
        }
        flushed = false;
        if (mt < 0) return lastMediaTime;
        lastMediaTime = mt;
        // log.debug("getMediaTimeMillis(): current: {}, last time: {}", mt, lastMediaTime);
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
            log.debug("setVideoRectangles: SRC: {}, DEST: {}", srcRect, destRect);
        if (lastVideoPositionUpdate == null || !lastVideoPositionUpdate.equals(destRect) || !videoSize.equals(srcRect.width, srcRect.height)) {
            // we need an update
            lastVideoPositionUpdate = destRect;
            videoSize.update(srcRect.width, srcRect.height);
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
                        log.debug("setVideoRectangles: Updating Video Location {}", destRect);

//                    // need to convert destRect based on scale
//                    Scale scale = context.getClient().getUIRenderer().getScale();
//                    Rectangle pos = destRect.copy();
//                    pos.width=(int)scale.xCanvasToScreen(pos.width);
//                    pos.height=(int)scale.xCanvasToScreen(pos.height);
//                    pos.x=(int)scale.xCanvasToScreen(pos.x);
//                    pos.y=(int)scale.xCanvasToScreen(pos.y)-pos.width;
//
//                    if (VerboseLogging.DETAILED_PLAYER_LOGGING)
//                        log.debug("setVideoRectangles: Updating Video Location based on scale {}", pos);
//
//                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) context.getVideoViewParent().getLayoutParams();
//                    lp.width = pos.width;
//                    lp.height = pos.height;
//                    lp.topMargin = pos.y;
//                    lp.leftMargin = pos.x;
//                    context.getVideoViewParent().setLayoutParams(lp);
//                    context.getVideoViewParent().requestLayout();
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
        flushed = true;
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

    protected void setVideoSize(int width, int height) {
        context.getVideoView().setVideoSize(width, height);
        context.getVideoView().requestLayout();
    }

    protected void releasePlayer() {
        log.debug("Releasing Player");
        player = null;
        videoSize.update(0, 0);
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
