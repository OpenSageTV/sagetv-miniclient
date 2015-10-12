package sagex.miniclient.android.video;

import android.content.res.Configuration;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;

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
public class VLCMediaPlayerImpl implements MiniPlayerPlugin, IVideoPlayer {
    private static final Logger log = LoggerFactory.getLogger(VLCMediaPlayerImpl.class);
    private final static int VideoSizeChanged = -1;
    private final MiniClientGDXActivity context;
    DataSource dataSource = null;
    private boolean pushMode;
    private boolean playerReady;
    private LibVLC libvlc;
    private SurfaceView mSurface;
    private SurfaceHolder holder;
    // media player
    private int mVideoWidth;
    private int mVideoHeight;

    public VLCMediaPlayerImpl(MiniClientGDXActivity activity) {
        this.context = activity;
        this.mSurface = activity.getVideoView();
        this.holder = mSurface.getHolder();
        //this.holder.addCallback(this);
    }

    @Override
    public void free() {
        try {
            context.hideVideo();
        } catch (Throwable t) {
        }

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
    public void load(byte b, byte b1, String s, final String urlString, Object o, boolean b2, int i) {
        // we need to use the http bridge
        SageTVHttpMediaServerBridge bridge = MiniClient.get().getHttpBridge();
        dataSource = bridge.createDataSource(pushMode, urlString);

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // we need to use the http bridge
                MiniClient.get().setUsingHttpBridge(true);
                SageTVHttpMediaServerBridge bridge = MiniClient.get().getHttpBridge();
                bridge.setDataSource(dataSource);

                // Setup VLC
                createPlayer();
                playerReady = true;
                log.debug("Sending URL to libbvc");
                libvlc.playMRL("http://localhost:9991/stream?uri=" + Uri.encode(urlString));
                //libvlc.playMRL("http://techslides.com/demos/sample-videos/small.mp4");
                //libvlc.playMRL("http://ia801903.us.archive.org/18/items/rmE163ArabicSubHdarabRunnersTeamBingutopHangukSib.mkv/rmE163ArabicSubHdarabRunnersTeamBingutopHangukSib.mp4");
                log.debug("LibVLC has our URL");
            }
        });
    }

    /**
     * Waits until the player has been contructed
     */
    void waitForPlayer() {
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
        return libvlc.getTime();
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
        context.hideVideo();
        libvlc.stop();
    }

    @Override
    public void pause() {
        waitForPlayer();
        log.debug("pause()");
        if (libvlc.isPlaying()) {
            libvlc.pause();
        }
    }

    @Override
    public void play() {
        waitForPlayer();
        log.debug("play()");
        if (!libvlc.isPlaying()) {
            libvlc.play();
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
        return (long) libvlc.getPosition();
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

    @Override
    public void setSurfaceLayout(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
        log.debug("setSurfaceLayout: {}, {}, {}, {}", width, height, visible_width, visible_height);
        setSize(width, height);
    }

    @Override
    public int configureSurface(Surface surface, int width, int height, int hal) {
        log.debug("configureSurface: {}, {}", width, height);
        return -1;
    }

    @Override
    public void eventHardwareAccelerationError() {
        log.error("eventHardwareAccelerationError()");
    }
//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        log.debug("surfaceCreated()");
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        log.debug("surfaceChanged(): {}, {}", width, height);
//        if (libvlc != null)
//            libvlc.attachSurface(holder.getSurface(), this);
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        log.debug("surfaceDestroyed()");
//    }

    private void setSize(final int width, final int height) {
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
                holder.setFixedSize(mVideoWidth, mVideoHeight);

                // set display size
                ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
                lp.width = w;
                lp.height = h;
                mSurface.setLayoutParams(lp);
                mSurface.invalidate();
            }
        });
    }

    private void createPlayer() {
        log.debug("Creating Player");
        releasePlayer();
        try {
//            if (media.length() > 0) {
//                Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
//                        0);
//                toast.show();
//            }

            // Create a new media player
            LibVLC.setOnNativeCrashListener(new LibVLC.OnNativeCrashListener() {
                @Override
                public void onNativeCrash() {
                    log.error("**** LIBVLC CRASHED ****");
                }
            });
            log.debug("Creating VLC");
            libvlc = new LibVLC();
            log.debug("Created VLC");
//            libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_AUTOMATIC);
//            //libvlc.setSubtitlesEncoding("");
//            libvlc.setAout(LibVLC.AOUT_OPENSLES);
//            libvlc.setVout(LibVLC.VOUT_ANDROID_SURFACE);
//            //libvlc.setTimeStretching(true);
//            //libvlc.setChroma("RV32");

            libvlc.setAout(LibVLC.AOUT_AUDIOTRACK);
            libvlc.setVout(LibVLC.VOUT_ANDROID_SURFACE);
            libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_FULL);
            libvlc.setVerboseMode(true);
            libvlc.init(context.getApplicationContext());

            //holder.setFormat(PixelFormat.RGBX_8888);
            //holder.setKeepScreenOn(true);

            libvlc.attachSurface(holder.getSurface(), this);

        } catch (Exception e) {
            Toast.makeText(context, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    private void releasePlayer() {
        if (libvlc == null)
            return;
        log.debug("Releasing Player");
        libvlc.stop();
        libvlc.detachSurface();
        holder = null;
        libvlc.destroy();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }
}
