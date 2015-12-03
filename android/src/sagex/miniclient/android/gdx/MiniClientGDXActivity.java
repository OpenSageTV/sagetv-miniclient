package sagex.miniclient.android.gdx;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import sagex.miniclient.MACAddressResolver;
import sagex.miniclient.MiniClient;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.AppUtil;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.R;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.EventRouter;
import sagex.miniclient.uibridge.SageTVKey;

import static sagex.miniclient.android.AppUtil.hideSystemUI;

/**
 * Created by seans on 20/09/15.
 */
public class MiniClientGDXActivity extends AndroidApplication implements MACAddressResolver {
    public static final String ARG_SERVER_INFO = "server_info";
    private static final Logger log = LoggerFactory.getLogger(MiniClientGDXActivity.class);
    @Bind(R.id.surface)
    FrameLayout uiFrameHolder;

    @Bind(R.id.video_surface)
    SurfaceView videoHolder;

    @Bind(R.id.waitforit)
    View pleaseWait = null;

    @Bind(R.id.pleaseWaitText)
    TextView plaseWaitText = null;

    MiniClient client;
    MiniClientRenderer mgr;

    private View miniClientView;

    public MiniClientGDXActivity() {
    }

    @Override
    protected void onResume() {
        log.debug("MiniClient UI onResume() called");
        try {
            miniClientView.setOnTouchListener(new MiniclientTouchListener(this, client));
            miniClientView.setOnKeyListener(new MiniClientKeyListener(client));
        } catch (Throwable t) {
            log.error("Failed to restore the key and touch handlers");
        }

        try {
            log.debug("Telling SageTV to repaint {}x{}", mgr.uiSize.getWidth(), mgr.uiSize.getHeight());
            client.getCurrentConnection().postRepaintEvent(0, 0, mgr.uiSize.getWidth(), mgr.uiSize.getHeight());
        } catch (Throwable t) {
            log.warn("Failed to do a repaint event on refresh");
        }

        hideSystemUI(this);

        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            miniClientView.setOnTouchListener(null);
            miniClientView.setOnKeyListener(null);
        } catch (Throwable t) {
        }

        log.debug("MiniClient UI onPause() called");
        try {
            // pause video if we are leaving the app
            if (client.getCurrentConnection() != null && client.getCurrentConnection().getMediaCmd() != null) {
                if (client.getCurrentConnection().getMediaCmd().getPlaya() != null) {
                    log.info("We are leaving the App, Make sure Video is stopped.");
                    client.getCurrentConnection().getMediaCmd().getPlaya().pause();
                    EventRouter.post(client, EventRouter.MEDIA_STOP);
                }
            }
        } catch (Throwable t) {
            log.debug("Failed why attempting to pause media player");
        }
        try {
            if (client.properties().getBoolean(PrefStore.Keys.app_destroy_on_pause)) {
                try {
                    client.closeConnection();
                } catch (Throwable t) {
                }
                finish();
            } else {
                // TODO: Try to free up memory, clear caches, etc
            }
        } catch (Throwable t) {
            log.debug("Failed to close client connection");
        }
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            hideSystemUI(this);

            setContentView(R.layout.miniclientgl_layout);
            ButterKnife.bind(this);

            AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
            //cfg.useGL20 = false;
            // we need to change the default pixel format - since it does not include an alpha channel
            // we need the alpha channel so the camera preview will be seen behind the GL scene
            cfg.r = 8;
            cfg.g = 8;
            cfg.b = 8;
            cfg.a = 8;

            client = MiniclientApplication.get().getClient();

            mgr = new MiniClientRenderer(this, client);
            miniClientView = initializeForView(mgr, cfg);

            if (graphics.getView() instanceof SurfaceView) {
                log.debug("Setting Translucent View");
                GLSurfaceView glView = (GLSurfaceView) graphics.getView();
                glView.setZOrderOnTop(true);
                // force alpha channel - I'm not sure we need this as the GL surface is already using alpha channel
                glView.getHolder().setFormat(PixelFormat.RGBA_8888);
            }

            miniClientView.setFocusable(true);
            miniClientView.setFocusableInTouchMode(true);
            miniClientView.setOnTouchListener(null);
            miniClientView.setOnClickListener(null);
            miniClientView.setOnKeyListener(null);
            miniClientView.setOnDragListener(null);
            miniClientView.setOnFocusChangeListener(null);
            miniClientView.setOnGenericMotionListener(null);
            miniClientView.setOnHoverListener(null);
            miniClientView.setOnTouchListener(null);
            //miniClientView.setBackgroundColor(Color.TRANSPARENT);
            uiFrameHolder.addView(miniClientView);
            //uiFrameHolder.setBackgroundColor(Color.TRANSPARENT);
            miniClientView.requestFocus();

            ServerInfo si = (ServerInfo) getIntent().getSerializableExtra(ARG_SERVER_INFO);
            if (si == null) {
                log.error("Missing SERVER INFO in Intent: {}", ARG_SERVER_INFO);
                finish();
            }

            setupNavigationDrawer();

            plaseWaitText.setText("Connecting to " + si.address + "...");
            setConnectingIsVisible(true);

            startMiniClient(si);
        } catch (Throwable t) {
            log.error("Failed to start/create the Main Activity for the MiniClient UI", t);
            throw new RuntimeException("Unable to start Activity", t);
        }
    }

    private void setupNavigationDrawer() {
        if (getResources().getBoolean(R.bool.istv)) {
            log.debug("Disabling Slide Out Navigation on TV");
            return;
        }

        final Map<Integer, SageTVKey> menuActions = new HashMap<>();
//if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem pausePlay = new PrimaryDrawerItem().withName("Pause/Play").withIdentifier(R.id.pause_play);
        PrimaryDrawerItem stop = new PrimaryDrawerItem().withName("Stop").withIdentifier(R.id.stop);
        PrimaryDrawerItem ff = new PrimaryDrawerItem().withName("Skip Forward").withIdentifier(R.id.media_ff);
        PrimaryDrawerItem rew = new PrimaryDrawerItem().withName("Skip Back").withIdentifier(R.id.media_rew);

        menuActions.put(R.id.pause_play, EventRouter.MEDIA_PLAY_PAUSE);
        menuActions.put(R.id.stop, EventRouter.MEDIA_STOP);
        menuActions.put(R.id.media_ff, EventRouter.MEDIA_FF);
        menuActions.put(R.id.media_rew, EventRouter.MEDIA_REW);

//create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(null)
                .withFullscreen(true)
                .addDrawerItems(
                        pausePlay,
                        stop,
                        ff,
                        rew
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        SageTVKey key = menuActions.get(drawerItem.getIdentifier());
                        if (key != null) {
                            EventRouter.post(client, key);
                        }
                        return true;
                    }
                })
                .withDrawerGravity(Gravity.START)
                .withDrawerWidthDp(150)
                .withShowDrawerOnFirstLaunch(true)
                .build();
        result.getRecyclerView().bringToFront();
    }

    public void startMiniClient(final ServerInfo si) {
        Thread t = new Thread("ANDROID-MINICLIENT") {
            @Override
            public void run() {
                try {
                    // cannot make network connections on the main thread
                    client.connect(si, MiniClientGDXActivity.this);
                } catch (final IOException e) {
                    MiniClientGDXActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MiniClientGDXActivity.this, "Unable to connect to server: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        t.start();
    }

    @Override
    public void onBackPressed() {
        // hide system ui, in case keyboard is visible
        hideSystemUI(this);
        //EventRouter.post(client, EventRouter.BACK);
        //confirmExit(this);
    }

    @Override
    protected void onDestroy() {
        log.debug("Closing MiniClient Connection");

        try {
            client.closeConnection();
        } catch (Throwable t) {
            log.error("Error shutting down client", t);
        }
        super.onDestroy();
    }

    public void setConnectingIsVisible(final boolean connectingIsVisible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pleaseWait.setVisibility((connectingIsVisible) ? View.VISIBLE : View.GONE);
            }
        });
    }

    public Surface getVideoSurface() {
        return videoHolder.getHolder().getSurface();
    }

    @Override
    public String getMACAddress() {
        return AppUtil.getMACAddress(this);
    }

    public SurfaceView getVideoView() {
        return videoHolder;
    }

    public void showHideKeyboard(boolean visible) {
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (visible) {
            log.debug("Showing Keyboard");
            im.showSoftInput(miniClientView, InputMethodManager.SHOW_FORCED);
        } else {
            im.hideSoftInputFromWindow(miniClientView.getWindowToken(), 0);
        }
    }

    public void showHideSoftRemote(boolean visible) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Showing Dialog Window");
        builder.show();
    }


    public void leftEdgeSwipe(MotionEvent event) {
        log.debug("Left Edge Swipe");
    }
}
