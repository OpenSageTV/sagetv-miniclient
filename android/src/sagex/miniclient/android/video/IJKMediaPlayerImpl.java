package sagex.miniclient.android.video;

import android.content.res.Configuration;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Toast;

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
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by seans on 06/10/15.
 */
public class IJKMediaPlayerImpl implements MiniPlayerPlugin {
    private static final Logger log = LoggerFactory.getLogger(IJKMediaPlayerImpl.class);
    private final static int VideoSizeChanged = -1;
    private final MiniClientGDXActivity context;
    DataSource dataSource = null;
    private boolean pushMode;
    private boolean playerReady;
    private IjkMediaPlayer player;
    private SurfaceView mSurface;
    private SurfaceHolder holder;
    // media player
    private int mVideoWidth;
    private int mVideoHeight;

    public IJKMediaPlayerImpl(MiniClientGDXActivity activity) {
        this.context = activity;
        this.mSurface = activity.getVideoView();
        this.holder = mSurface.getHolder();
        //this.holder.addCallback(this);
    }

    @Override
    public void free() {
        log.info("Freeing Media Player");
        if (MiniClient.get().isUsingHttpBridge()) {
            MiniClient.get().getHttpBridge().setDataSource(null);
        }
        releasePlayer();
    }

    @Override
    public void setPushMode(boolean b) {
        this.pushMode = b;
    }

    @Override
    public void load(byte b, byte b1, String s, String urlString, Object o, boolean b2, int i) {
        // we need to use the http bridge
        SageTVHttpMediaServerBridge bridge = MiniClient.get().getHttpBridge();
        dataSource = bridge.createDataSource(pushMode, urlString);

        createPlayer();

        log.debug("Sending URL to mediaplayer");
        try {
            player.setDataSource("http://localhost:9991/stream");
            //player.setDataSource("http://techslides.com/demos/sample-videos/small.mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                playerReady = true;
            }
        });
        player.prepareAsync();
        player.start();
        //libvlc.playMRL("http://ia801903.us.archive.org/18/items/rmE163ArabicSubHdarabRunnersTeamBingutopHangukSib.mkv/rmE163ArabicSubHdarabRunnersTeamBingutopHangukSib.mp4");
        log.debug("mediaplayer has our URL");
    }

    /**
     * Waits until the player has been contructed
     */
    void waitForPlayer() {
        log.debug("waiting for player...");
        while (!playerReady) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.interrupted();
                e.printStackTrace();
                break;
            }
        }
        log.debug("player is ready...");
    }

    @Override
    public long getMediaTimeMillis() {
        return player.getCurrentPosition();
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
        player.stop();
    }

    @Override
    public void pause() {
        log.debug("pause()");
        if (player.isPlaying()) {
            player.pause();
        }
    }

    @Override
    public void play() {
        log.debug("play()");
        //waitForPlayer();
        if (!player.isPlaying()) {
            player.start();
        }
    }

    @Override
    public void seek(long maxValue) {

    }

    @Override
    public void inactiveFile() {

    }

    @Override
    public long getLastFileReadPos() {
        return player.getCurrentPosition();
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
        log.debug("pushData()");
        if (dataSource instanceof PushBufferDataSource) {
            ((PushBufferDataSource) dataSource).pushBytes(cmddata, bufDataOffset, buffSize);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void run() {

    }

    private void setSize(int width, int height) {
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
        holder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }

    private void createPlayer() {
        log.debug("Creating Player");
        releasePlayer();
        try {
            player = new IjkMediaPlayer();
            player.setLogEnabled(true);
            player.setDisplay(holder);

            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 200000);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 163840);
        } catch (Exception e) {
            Toast.makeText(context, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    private void releasePlayer() {
        if (player == null)
            return;
        log.debug("Releasing Player");
        player.stop();
        player.release();
        holder = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
    }
}
