package sagex.miniclient.android.video;

import android.content.res.Configuration;
import android.view.SurfaceView;
import android.view.ViewGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.MiniClient;
import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.android.gdx.MiniClientGDXActivity;
import sagex.miniclient.httpbridge.DataSource;
import sagex.miniclient.httpbridge.PushBufferDataSource;
import sagex.miniclient.httpbridge.SageTVHttpMediaServerBridge;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Rectangle;

//import org.videolan.libvlc.LibVLC;

/**
 * Created by seans on 06/10/15.
 */
public abstract class DataSourceMediaPlayerImpl<Player> implements MiniPlayerPlugin {
    protected static final Logger log = LoggerFactory.getLogger(DataSourceMediaPlayerImpl.class);

    private final static int VideoSizeChanged = -1;

    final MiniClientGDXActivity context;

    boolean pushMode;
    boolean playerReady;

    Player player;
    SurfaceView mSurface;

    // media player
    int mVideoWidth;
    int mVideoHeight;

    boolean createPlayerOnUI = true;

    public DataSourceMediaPlayerImpl(MiniClientGDXActivity activity, boolean createPlayerOnUI) {
        this.context = activity;
        this.mSurface = activity.getVideoView();
        this.createPlayerOnUI = createPlayerOnUI;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void free() {
        log.info("Freeing Media Player");
        MiniClient.get().getHttpBridge().closeSessions();
        releasePlayer();
    }

    @Override
    public void setPushMode(boolean b) {
        this.pushMode = b;
    }

    @Override
    public void load(byte b, byte b1, String s, final String urlString, Object o, boolean b2, int i) {
        // we need to use the http bridge
        SageTVHttpMediaServerBridge bridge = MiniClient.get().getHttpBridge();
        final String bridgeUrl = bridge.getVideoURI(urlString);

        if (createPlayerOnUI) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    releasePlayer();
                    setupPlayer(bridgeUrl);
                }
            });
        } else {
            releasePlayer();
            setupPlayer(bridgeUrl);
        }
    }

    protected abstract void setupPlayer(String url);

    /**
     * Waits until the player has been contructed
     */
    void waitForPlayer() {
        log.debug("wait for player");
        while (!playerReady) {
            try {
                Thread.sleep(50);
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
        return 0;
    }

    @Override
    public void setMute(boolean b) {

    }

    @Override
    public void stop() {
    }

    @Override
    public void pause() {
        log.debug("pause()");
        if (createPlayerOnUI) waitForPlayer();
    }

    @Override
    public void play() {
        log.debug("play()");
        if (createPlayerOnUI) waitForPlayer();
        waitForPlayer();
    }

    @Override
    public void seek(long maxValue) {
        log.debug("SEEK: {}", maxValue);
    }

    @Override
    public void inactiveFile() {

    }

    @Override
    public long getLastFileReadPos() {
        DataSource dataSource = getDataSource();
        if (dataSource instanceof PushBufferDataSource) {
            return ((PushBufferDataSource) dataSource).getBytesRead();
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
        DataSource dataSource = getDataSource();
        //log.debug("pushData()");
        if (dataSource instanceof PushBufferDataSource) {
            ((PushBufferDataSource) dataSource).pushBytes(cmddata, bufDataOffset, buffSize);
        }
    }

    @Override
    public void flush() {
        log.debug("flush()");
        getDataSource().flush();
        // player.setPosition(player.getPosition()+1);
    }

    @Override
    public int getBufferLeft() {
        if (getDataSource() == null) {
            return PushBufferDataSource.PIPE_SIZE;
        }
        return getDataSource().bufferAvailable();
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
        MiniClient.get().getHttpBridge().closeSessions();
        player = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    protected DataSource getDataSource() {
        //log.debug("get data source");
        return MiniClient.get().getHttpBridge().getCurrentDataSource();
    }
}
