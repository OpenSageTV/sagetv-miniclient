package sagex.miniclient.android.gdx;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.squareup.otto.DeadEvent;
import com.squareup.otto.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.MACAddressResolver;
import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.NavigationFragment;
import sagex.miniclient.android.R;
import sagex.miniclient.android.VideoInfoFragment;
import sagex.miniclient.android.events.BackPressedEvent;
import sagex.miniclient.android.events.ChangePlayerOneTime;
import sagex.miniclient.android.events.CloseAppEvent;
import sagex.miniclient.android.events.HideKeyboardEvent;
import sagex.miniclient.android.events.HideNavigationEvent;
import sagex.miniclient.android.events.HideSystemUIEvent;
import sagex.miniclient.android.events.MessageEvent;
import sagex.miniclient.android.events.ToggleAspectRatioEvent;
import sagex.miniclient.android.events.ShowKeyboardEvent;
import sagex.miniclient.android.events.ShowNavigationEvent;
import sagex.miniclient.android.ui.MiniClientKeyListener;
import sagex.miniclient.android.ui.AndroidUIController;
import sagex.miniclient.android.ui.MiniclientTouchListener;
import sagex.miniclient.android.util.AudioUtil;
import sagex.miniclient.android.video.PlayerSurfaceView;
import sagex.miniclient.events.ConnectionLost;
import sagex.miniclient.events.VideoInfoShow;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.prefs.PrefStore.Keys;
import sagex.miniclient.uibridge.EventRouter;
import sagex.miniclient.util.ClientIDGenerator;
import sagex.miniclient.video.HasVideoInfo;

import static sagex.miniclient.android.AppUtil.confirmExit;
import static sagex.miniclient.android.AppUtil.hideSystemUI;
import static sagex.miniclient.android.AppUtil.message;

/**
 * Created by seans on 20/09/15.
 */
public class MiniClientGDXActivity extends AndroidApplication implements MACAddressResolver, AndroidUIController {
    public static final String ARG_SERVER_INFO = "server_info";
    private static final Logger log = LoggerFactory.getLogger(MiniClientGDXActivity.class);
    FrameLayout uiFrameHolder;
    PlayerSurfaceView videoHolder;
    ViewGroup videoHolderParent;
    View pleaseWait = null;
    TextView plaseWaitText = null;
    // error stuff
    TextView errorMessage;
    TextView errorCause;
    ViewGroup errorContainer;

    MiniClient client;
    MiniClientGDXRenderer mgr;

    MediaSessionCompat mediaSessionCompat;

    private View miniClientView;

    private ChangePlayerOneTime changePlayerOneTime = null;

    public MiniClientGDXActivity() {
    }

    public MiniClient getClient() {
        return client;
    }

    @Override
    protected void onResume() {
        log.debug("MiniClient UI onResume() called");

        AudioUtil.requestAudioFocus(this);

        // setup to handle events
        client.eventbus().register(this);

        MiniClientKeyListener keyListener = new MiniClientKeyListener(this, client);

        try {
            miniClientView.setOnTouchListener(new MiniclientTouchListener(this, client));
            miniClientView.setOnKeyListener(keyListener);
        } catch (Throwable t) {
            log.error("Failed to restore the key and touch handlers");
        }

        try {
            if (client.getUIRenderer() != null && client.getUIRenderer().isFirstFrameRendered() && mgr.uiSize.width > 0 && mgr.uiSize.height > 0) {
                log.debug("Telling SageTV to repaint {}x{}", mgr.uiSize.getWidth(), mgr.uiSize.getHeight());
                client.getCurrentConnection().postRepaintEvent(0, 0, mgr.uiSize.getWidth(), mgr.uiSize.getHeight());
            }
        } catch (Throwable t) {
            log.warn("Failed to do a repaint event on refresh");
        }

        hideSystemUI(this);

        super.onResume();
    }

    @Override
    protected void onPause() {
        // remove ourself from handling events
        client.eventbus().unregister(this);

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
                    EventRouter.postCommand(client, SageCommand.STOP);
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
            initMediaSession();

            hideSystemUI(this);

            setContentView(R.layout.miniclientgdx_layout);

            findViewById(R.id.errorClose).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCloseClicked();
                }
            });

            uiFrameHolder = (FrameLayout) findViewById(R.id.surface);
            videoHolder = (PlayerSurfaceView) findViewById(R.id.video_surface);
            videoHolderParent = (ViewGroup) findViewById(R.id.video_surface_parent);
            pleaseWait = findViewById(R.id.waitforit);
            plaseWaitText = (TextView) findViewById(R.id.pleaseWaitText);
            errorMessage = (TextView) findViewById(R.id.errorMessage);
            errorCause = (TextView) findViewById(R.id.errorCause);
            errorContainer = (ViewGroup) findViewById(R.id.errorContainer);

            AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
            //cfg.useGL20 = false;
            // we need to change the default pixel format - since it does not include an alpha channel
            // we need the alpha channel so the camera preview will be seen behind the GL scene
            cfg.r = 8;
            cfg.g = 8;
            cfg.b = 8;
            cfg.a = 8;

            client = MiniclientApplication.get().getClient();

            mgr = new MiniClientGDXRenderer(this, client);
            miniClientView = initializeForView(mgr, cfg);

            if (graphics.getView() instanceof SurfaceView) {
                log.debug("Setting Translucent View");
                GLSurfaceView glView = (GLSurfaceView) graphics.getView();
                // This is needed or else we won't see OSD over video
                glView.setZOrderOnTop(true);
                // This is needed or else we will not see the video playing behind the OSD
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
                return;
            }

            //setupNavigationDrawer();
            String connect = null;
            if (si.isLocatorOnly() || si.forceLocator) {
                connect = getString(R.string.msg_connecting_locator, si.name);
            } else {
                connect = getString(R.string.msg_connecting, si.name);
            }
            plaseWaitText.setText(connect);
            setConnectingIsVisible(true);

            startMiniClient(si);
        } catch (Throwable t) {
            log.error("Failed to start/create the Main Activity for the MiniClient UI", t);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setErrorView(null, "MiniClient failed to initialize", null);
                }
            });
        }
    }

    void initMediaSession() {
        // NOTE: all this is so that when you press pause/play in the app, we can capture the
        // media control event, so that other apps DON'T (ie, google play music, plex, etc).
        // ideally we could do something useful with this, but for not, just eat it.

        try {
            ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
            mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "SAGETVMINICLIENT", mediaButtonReceiver, null);
            mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
                @Override
                public void onCommand(String command, Bundle extras, ResultReceiver cb) {
                    log.debug("Audio Session Callback Handler: Command {}", command);
                }
            });
            mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);

            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
            mediaSessionCompat.setMediaButtonReceiver(pendingIntent);
            mediaSessionCompat.setActive(true);
            log.debug("Media Session is setup to capture pause/play. session: "+mediaSessionCompat.getSessionToken());
        } catch (Throwable t) {
            log.error("Failed to capture the media session", t);
        }
    }

    public void startMiniClient(final ServerInfo si) {
        Thread t = new Thread("ANDROID-MINICLIENT") {
            @Override
            public void run() {
                try {
                    // cannot make network connections on the main thread
                    client.connect(si, MiniClientGDXActivity.this);
                } catch (final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setErrorView(si, "Unable to connect", e.getMessage());
//                            Toast.makeText(MiniClientGDXActivity.this, "Unable to connect to server: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                            finish();
                        }
                    });
                }
            }
        };
        t.start();
    }

    private void setErrorView(ServerInfo si, String message, String cause) {
        plaseWaitText.setVisibility(View.GONE);
        errorMessage.setText(message);
        errorCause.setText(cause);
        errorContainer.setVisibility(View.VISIBLE);
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

        if (mediaSessionCompat!=null) {
            try {
                mediaSessionCompat.setActive(false);
                mediaSessionCompat.release();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

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
                if (connectingIsVisible) {
                    errorContainer.setVisibility(View.GONE);
                    pleaseWait.setVisibility(View.VISIBLE);
                } else {
                    // hiding connecting is visible
                    //YoYo.with(Techniques.FadeOutLeft).duration(700).playOn(pleaseWait);
                    errorContainer.setVisibility(View.GONE);
                    pleaseWait.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public String getMACAddress() {
        // Android 6 generates the same MAC address, so let's outgenerate one
        String id = client.properties().getString(Keys.client_id);
        if (id == null) {
            ClientIDGenerator gen = new ClientIDGenerator();
            id = gen.generateId();
            client.properties().setString(Keys.client_id, id);
        }
        return id;
        //return AppUtil.getMACAddress(this);
    }

    public PlayerSurfaceView getVideoView() {
        if (videoHolder == null) {
            setupVideoFrame();
        }
        return videoHolder;
    }

    @Override
    public View getUIView() {
        return miniClientView;
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        log.debug("Configuration Change: Keyboard: {}, KeyboardHidden: {}, OBJECT: {}", config.keyboard, config.keyboardHidden, config);
    }

    public void showHideKeyboard(final boolean visible) {

        miniClientView.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (visible) {
                    log.debug("Showing Keyboard");
                    miniClientView.requestFocus();
                    miniClientView.requestFocusFromTouch();
                    im.showSoftInput(miniClientView, InputMethodManager.SHOW_FORCED);
                } else {
                    im.hideSoftInputFromWindow(miniClientView.getWindowToken(), 0);
                }
            }
        }, 200);
    }

    public void showHideSoftRemote(boolean visible) {
        if (visible) {
            showNavigationDialog();
        } else {
            hideNavigationDialog();
        }
    }

    void showNavigationDialog() {
        NavigationFragment.showDialog(this);
    }

    public void leftEdgeSwipe(MotionEvent event) {
        log.debug("Left Edge Swipe");
    }

    public View getRootView() {
        return miniClientView;
    }

    @Subscribe
    public void handleOnShowKeyboard(ShowKeyboardEvent event) {
        showHideKeyboard(true);
    }

    @Subscribe
    public void handleOnHideKeyboard(HideKeyboardEvent event) {
        showHideKeyboard(false);
    }

    @Subscribe
    public void handleOnHideSystemUI(HideSystemUIEvent event) {
        hideSystemUI(this);
    }

    @Subscribe
    public void handleOnShowNavigation(ShowNavigationEvent event) {
        try {
            log.debug("MiniClient built-in Naviation is visible");
            showHideSoftRemote(true);
        } catch (Throwable t) {
            log.debug("Failed to show navigation");
        }
    }

    @Subscribe
    public void handleVideoInfoRequest(VideoInfoShow request) {
        if (client.getUIRenderer() instanceof HasVideoInfo) {
            hideNavigationDialog();
            VideoInfoFragment.showDialog(this);
        }
    }

    @Subscribe
    public void handleOnHideNavigation(HideNavigationEvent event) {
        try {
            log.debug("MiniClient built-in Naviation is hidden");
            showHideSoftRemote(false);
            hideSystemUI(this);
        } catch (Throwable t) {
            log.debug("Failed to hide navigation");
        }
    }

    @Subscribe
    public void handleOnCloseApp(CloseAppEvent event) {
        confirmExit(this, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Subscribe
    public void handleOnConnectionLost(ConnectionLost event) {
        if (event.reconnecting) {
            message("SageTV Connection Closed.  Reconnecting...");
        } else {
            message("SageTV Connection Closed.");
            finish();
        }
    }

    boolean hideNavigationDialog() {
        log.debug("Hiding Navigation");
        // remove nav OSD
        Fragment prev = getFragmentManager().findFragmentByTag("nav");
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        boolean hidingOSD = false;
        if (prev != null) {
            try {
                DialogFragment f = (DialogFragment) prev;
                f.dismiss();
            } catch (Throwable t) {
            }
            hidingOSD = true;
            try {
                ft.remove(prev);
            } catch (Throwable t) {
            }
        }
        ft.commit();

        // return true if the remote was actually hidden
        return hidingOSD;
    }

    @Subscribe
    public void handleOnBackPressed(BackPressedEvent event) {
        hideSystemUI(this);

        log.debug("on back pressed");

        if (!hideNavigationDialog()) {
            log.debug("Navigation wasn't visible so will process normal back");
            EventRouter.postCommand(client, SageCommand.BACK);
        } else {
            log.debug("Just hiding navigation");
        }
    }

    @Subscribe
    public void onDeadEvent(DeadEvent event) {
        log.debug("Unhandled Event: {}", event);
    }

    public void setupVideoFrame() {
        log.debug("Setting up the Video Frame");
        videoHolder.setVisibility(View.VISIBLE);
    }

    public void removeVideoFrame() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log.debug("Removing Video View");
                //videoHolderFrame.removeAllViews();
                videoHolder.setVisibility(View.GONE);
            }
        });
    }

    @Subscribe
    public void onChangePlayerOneTime(ChangePlayerOneTime changePlayerOneTime) {
        this.changePlayerOneTime = changePlayerOneTime;
    }

    @Subscribe
    public void onToggleAspectRatio(ToggleAspectRatioEvent ar) {
        log.debug("SENDING AR_TOGGLE: " + SageCommand.AR_TOGGLE);
        EventRouter.postCommand(client, SageCommand.AR_TOGGLE);
    }

    /**
     * This is a one time read.  It will return true if we need to switch the player, one time,
     * but it will reset itself AFTER this read, so, only call it once.
     *
     * @return
     */
    public boolean isSwitchingPlayerOneTime() {
        boolean change = changePlayerOneTime != null;
        changePlayerOneTime = null;
        return change;
    }

    @Subscribe
    public void onMessage(final MessageEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(MiniClientGDXActivity.this, event.getMessage(), Toast.LENGTH_LONG).show();
                } catch (Throwable t) {
                    log.error("MESSAGE: {}", event.getMessage());
                }
            }
        });
    }

    public ViewGroup getVideoViewParent() {
        return videoHolderParent;
    }

    // @OnClick(R.id.errorClose)
    public void onCloseClicked() {
        // connect to server
//        if (getResources().getBoolean(R.bool.istv)) {
            finish();
//        } else {
//            Intent i = null;
//            i = new Intent(this, ServersActivity.class);
//            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(i);
//        }

    }
}
