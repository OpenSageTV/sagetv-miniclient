package sagex.miniclient.android;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
//import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.DeadEvent;
import com.squareup.otto.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.MACAddressResolver;
import sagex.miniclient.MiniClient;
import sagex.miniclient.SageCommand;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.events.BackPressedEvent;
import sagex.miniclient.android.events.ChangePlayerOneTime;
import sagex.miniclient.android.events.CloseAppEvent;
import sagex.miniclient.android.events.HideKeyboardEvent;
import sagex.miniclient.android.events.HideNavigationEvent;
import sagex.miniclient.android.events.HideSystemUIEvent;
import sagex.miniclient.android.events.MessageEvent;
import sagex.miniclient.android.events.ToggleAspectRatioEvent;
import sagex.miniclient.android.ui.AndroidUIController;
import sagex.miniclient.android.ui.MiniClientKeyListener;
import sagex.miniclient.android.ui.MiniclientTouchListener;
import sagex.miniclient.android.ui.keymaps.DebugKeyEvent;
import sagex.miniclient.android.ui.keymaps.DebugKeyPressWindow;
import sagex.miniclient.android.ui.keymaps.KeyMapProcessor;
import sagex.miniclient.android.util.AudioUtil;
import sagex.miniclient.android.video.PlayerSurfaceView;
import sagex.miniclient.events.ConnectionLost;
import sagex.miniclient.events.DebugSageCommandEvent;
import sagex.miniclient.events.ShowKeyboardEvent;
import sagex.miniclient.events.ShowNavigationEvent;
import sagex.miniclient.events.VideoInfoShow;
import sagex.miniclient.prefs.PrefStore.Keys;
import sagex.miniclient.uibridge.EventRouter;
import sagex.miniclient.uibridge.UIRenderer;
import sagex.miniclient.util.ClientIDGenerator;
import sagex.miniclient.video.HasVideoInfo;

import static sagex.miniclient.android.AppUtil.confirmExit;
import static sagex.miniclient.android.AppUtil.hideSystemUI;
import static sagex.miniclient.android.AppUtil.message;

import androidx.media.session.MediaButtonReceiver;


/**
 * Created by seans on 20/09/15.
 */
public class UIActivityLifeCycleHandler<UIRenderType extends UIRenderer> implements MACAddressResolver, AndroidUIController
{

    public interface IActivityCallback<UIRenderType extends UIRenderer>
    {
        View createAndConfigureUIView(UIActivityLifeCycleHandler<UIRenderType> handler);

        UIRenderType createUIRenderer(UIActivityLifeCycleHandler<UIRenderType> handler);

        int getLayoutViewId(UIActivityLifeCycleHandler<UIRenderType> handler);
    }

    protected boolean keyboardVisible = false;
    public static final String ARG_SERVER_INFO = "server_info";
    protected static final Logger log = LoggerFactory.getLogger(UIActivityLifeCycleHandler.class);
    protected FrameLayout uiFrameHolder;
    protected PlayerSurfaceView videoHolder;
    protected ViewGroup videoHolderParent;
    protected View pleaseWait = null;
    protected TextView plaseWaitText = null;
    protected TextView captionsText = null;
    // error stuff
    protected TextView errorMessage;
    protected TextView errorCause;
    protected ViewGroup errorContainer;

    protected MiniClient client;
    protected UIRenderType mgr;

    protected MediaSessionCompat mediaSessionCompat;

    protected View miniClientView;

    protected ChangePlayerOneTime changePlayerOneTime = null;

    protected Activity activity;
    protected IActivityCallback<UIRenderType> activityCallback;

    public UIActivityLifeCycleHandler(IActivityCallback<UIRenderType> activityCallback)
    {
        this.activityCallback = activityCallback;
    }

    public void onWindowFocusChanged(boolean hasFocus)
    {
        AppUtil.hideSystemUI(activity);
    }

    public MiniClient getClient()
    {
        return client;
    }


    public void onResume(Activity activity)
    {
        this.activity = activity;

        log.debug("MiniClient UI onResume() called");

        AudioUtil.requestAudioFocus(activity);

        // setup to handle events
        client.eventbus().register(UIActivityLifeCycleHandler.this);

        MiniClientKeyListener keyListener = new MiniClientKeyListener(activity, client, UIActivityLifeCycleHandler.this);

        try
        {
            miniClientView.setOnTouchListener(new MiniclientTouchListener(activity, client));
            miniClientView.setOnKeyListener(keyListener);
        }
        catch (Throwable t)
        {
            log.error("Failed to restore the key and touch handlers");
        }

        try
        {
            if (client.getUIRenderer() != null && client.getUIRenderer().isFirstFrameRendered() && mgr.getUISize().width > 0 && mgr.getUISize().height > 0)
            {
                log.debug("Telling SageTV to repaint {}x{}", mgr.getUISize().getWidth(), mgr.getUISize().getHeight());
                client.getCurrentConnection().postRepaintEvent(0, 0, mgr.getUISize().getWidth(), mgr.getUISize().getHeight());
            }
        }
        catch (Throwable t)
        {
            log.warn("Failed to do a repaint event on refresh");
        }

        hideSystemUI(activity);
    }

    public void onPause(Activity activity)
    {
        // remove ourself from handling events
        client.eventbus().unregister(this);

        try
        {
            miniClientView.setOnTouchListener(null);
            miniClientView.setOnKeyListener(null);
        }
        catch (Throwable t)
        {
        }

        log.debug("MiniClient UI onPause() called");
        try
        {
            // pause video if we are leaving the app
            if (client.getCurrentConnection() != null && client.getCurrentConnection().getMediaCmd() != null)
            {
                if (client.getCurrentConnection().getMediaCmd().getPlaya() != null)
                {
                    log.info("We are leaving the App, Make sure Video is stopped.");
                    client.getCurrentConnection().getMediaCmd().getPlaya().pause();
                    EventRouter.postCommand(client, SageCommand.STOP);
                }
            }
        }
        catch (Throwable t)
        {
            log.debug("Failed why attempting to pause media player");
        }
        try
        {
            if (client.properties().getBoolean(Keys.app_destroy_on_pause, true))
            {
                try
                {
                    client.closeConnection();
                }
                catch (Throwable t)
                {
                }
                finish();
            }
            else
            {
                // TODO: Try to free up memory, clear caches, etc
            }
        }
        catch (Throwable t)
        {
            log.debug("Failed to close client connection");
        }
    }

    public void onCreate(Activity activity)
    {
        this.activity = activity;
        try
        {
            hideSystemUI(activity);

            activity.setContentView(activityCallback.getLayoutViewId(this));

            activity.findViewById(R.id.errorClose).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onCloseClicked();
                }
            });

            uiFrameHolder = (FrameLayout) activity.findViewById(R.id.surface);
            videoHolder = (PlayerSurfaceView) activity.findViewById(R.id.video_surface);
            videoHolderParent = (ViewGroup) activity.findViewById(R.id.video_surface_parent);
            pleaseWait = activity.findViewById(R.id.waitforit);
            plaseWaitText = (TextView) activity.findViewById(R.id.pleaseWaitText);
            captionsText = (TextView) activity.findViewById(R.id.captionsText);
            errorMessage = (TextView) activity.findViewById(R.id.errorMessage);
            errorCause = (TextView) activity.findViewById(R.id.errorCause);
            errorContainer = (ViewGroup) activity.findViewById(R.id.errorContainer);


            client = MiniclientApplication.get().getClient();

            mgr = activityCallback.createUIRenderer(this);

            miniClientView = activityCallback.createAndConfigureUIView(this);

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
            uiFrameHolder.addView(miniClientView);
            miniClientView.requestFocus();

            ServerInfo si = (ServerInfo) activity.getIntent().getSerializableExtra(ARG_SERVER_INFO);
            if (si == null)
            {
                log.error("Missing SERVER INFO in Intent: {}", ARG_SERVER_INFO);
                finish();
                return;
            }

            //setupNavigationDrawer();
            String connect = null;
            if (si.isLocatorOnly() || si.forceLocator)
            {
                connect = activity.getString(R.string.msg_connecting_locator, si.name);
            }
            else
            {
                connect = activity.getString(R.string.msg_connecting, si.name);
            }
            plaseWaitText.setText(connect);
            setConnectingIsVisible(true);

            startMiniClient(si);
            //VideoPlayerCaptions.

        }
        catch (Throwable t)
        {
            log.error("Failed to start/create the Main Activity for the MiniClient UI", t);
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    setErrorView(null, "MiniClient failed to initialize", null);
                }
            });
        }
    }

    public void startMiniClient(final ServerInfo si)
    {
        Thread t = new Thread("ANDROID-MINICLIENT")
        {
            @Override
            public void run()
            {
                try
                {
                    // cannot make network connections on the main thread
                    client.connect(si, UIActivityLifeCycleHandler.this);
                }
                catch (final IOException e)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setErrorView(si, "Unable to connect", e.getMessage());
                        }
                    });
                }
            }
        };
        t.start();
    }

    public void showErrorMessage(String message, String cause)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Toast.makeText(activity, cause + " - " + message, Toast.LENGTH_LONG).show();
                }
                catch (Throwable t)
                {
                    log.error("MESSAGE: {}", message);
                }
            }
        });
    }

    private void setErrorView(ServerInfo si, String message, String cause)
    {
        plaseWaitText.setVisibility(View.GONE);
        errorMessage.setText(message);
        errorCause.setText(cause);
        errorContainer.setVisibility(View.VISIBLE);
    }

    public void onBackPressed()
    {
        // hide system ui, in case keyboard is visible
        hideSystemUI(activity);
    }

    public void onDestroy()
    {
        log.debug("Closing MiniClient Connection");

        if (mediaSessionCompat != null)
        {
            try
            {
                mediaSessionCompat.setActive(false);
                mediaSessionCompat.release();
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }

        try
        {

            client.closeConnection();
        }
        catch (Throwable t)
        {
            log.error("Error shutting down client", t);
        }
    }

    public void setConnectingIsVisible(final boolean connectingIsVisible)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (connectingIsVisible)
                {
                    errorContainer.setVisibility(View.GONE);
                    pleaseWait.setVisibility(View.VISIBLE);
                }
                else
                {
                    // hiding connecting is visible
                    //YoYo.with(Techniques.FadeOutLeft).duration(700).playOn(pleaseWait);
                    errorContainer.setVisibility(View.GONE);
                    pleaseWait.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public String getMACAddress()
    {
        // Android 6 generates the same MAC address, so let's outgenerate one
        String id = client.properties().getString(Keys.client_id);
        if (id == null)
        {
            ClientIDGenerator gen = new ClientIDGenerator();
            id = gen.generateId();
            client.properties().setString(Keys.client_id, id);
        }
        return id;
        //return AppUtil.getMACAddress(this);
    }

    public PlayerSurfaceView getVideoView()
    {
        if (videoHolder == null)
        {
            setupVideoFrame();
        }
        return videoHolder;
    }

    @Override
    public View getUIView()
    {
        return miniClientView;
    }

    public TextView getPleaseWaitText()
    {
        return this.plaseWaitText;
    }

    @Override
    public Context getContext()
    {
        return activity;
    }

    public boolean isKeyboardVisible()
    {
        return this.keyboardVisible;
    }

    public void showHideKeyboard(final boolean visible)
    {

        miniClientView.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                if (visible)
                {
                    log.debug("Showing Keyboard");
                    miniClientView.requestFocus();
                    im.showSoftInput(miniClientView, InputMethodManager.SHOW_FORCED);
                    UIActivityLifeCycleHandler.this.keyboardVisible = true;
                }
                else
                {
                    im.hideSoftInputFromWindow(miniClientView.getWindowToken(), 0);
                    UIActivityLifeCycleHandler.this.keyboardVisible = false;
                }
            }
        }, 200);
    }

    public void showHideSoftRemote(boolean visible)
    {
        if (visible)
        {
            showNavigationDialog();
        }
        else
        {
            hideNavigationDialog();
        }
    }

    void showNavigationDialog()
    {
        NavigationFragment.showDialog(activity);
    }

    public void leftEdgeSwipe(MotionEvent event)
    {
        log.debug("Left Edge Swipe");
    }

    public View getRootView()
    {
        return miniClientView;
    }

    @Subscribe
    public void handleOnShowKeyboard(ShowKeyboardEvent event)
    {
        showHideKeyboard(true);
    }

    @Subscribe
    public void handleOnHideKeyboard(HideKeyboardEvent event)
    {
        showHideKeyboard(false);
    }

    @Subscribe
    public void handleOnHideSystemUI(HideSystemUIEvent event)
    {
        hideSystemUI(activity);
    }

    @Subscribe
    public void handleOnShowNavigation(ShowNavigationEvent event)
    {
        try
        {
            log.debug("MiniClient built-in Naviation is visible");
            showHideSoftRemote(true);
        }
        catch (Throwable t)
        {
            log.debug("Failed to show navigation");
        }
    }

    @Subscribe
    public void handleVideoInfoRequest(VideoInfoShow request)
    {
        if (client.getUIRenderer() instanceof HasVideoInfo)
        {
            hideNavigationDialog();
            VideoInfoFragment.showDialog(activity);
        }
    }

    @Subscribe
    public void handleOnHideNavigation(HideNavigationEvent event)
    {
        try
        {
            log.debug("MiniClient built-in Naviation is hidden");
            showHideSoftRemote(false);
            hideSystemUI(activity);
        }
        catch (Throwable t)
        {
            log.debug("Failed to hide navigation");
        }
    }

    @Subscribe
    public void handleOnCloseApp(CloseAppEvent event)
    {
        confirmExit(activity, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                activity.finish();
            }
        });
    }

    @Subscribe
    public void handleOnConnectionLost(ConnectionLost event)
    {
        if (event.reconnecting)
        {
            message("SageTV Connection Closed.  Reconnecting...");
        }
        else
        {
            message("SageTV Connection Closed.");
            finish();
        }
    }

    boolean hideNavigationDialog()
    {
        log.debug("Hiding Navigation");
        // remove nav OSD
        Fragment prev = activity.getFragmentManager().findFragmentByTag("nav");
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        boolean hidingOSD = false;
        if (prev != null)
        {
            try
            {
                DialogFragment f = (DialogFragment) prev;
                f.dismiss();
            }
            catch (Throwable t)
            {
            }
            hidingOSD = true;
            try
            {
                ft.remove(prev);
            }
            catch (Throwable t)
            {
            }
        }
        ft.commit();

        // return true if the remote was actually hidden
        return hidingOSD;
    }

    @Subscribe
    public void handleOnBackPressed(BackPressedEvent event)
    {
        hideSystemUI(activity);

        // prevents multiple back events from firing form different key handlers
        log.debug("on back pressed event");

        if (hideNavigationDialog())
        {
            log.debug("Just hiding navigation");
            KeyMapProcessor.skipBackOneTime = true;
        }
        else
        {
            // log.debug("Navigation wasn't visible so will process normal back");
            //EventRouter.postCommand(client, SageCommand.BACK);
        }
    }

    @Subscribe
    public void onDeadEvent(DeadEvent event)
    {
        log.debug("Unhandled Event: {} -- source: {}", event.event, event.source);
    }

    public void setupVideoFrame()
    {
        log.debug("Setting up the Video Frame");
        videoHolder.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onChangePlayerOneTime(ChangePlayerOneTime changePlayerOneTime)
    {
        this.changePlayerOneTime = changePlayerOneTime;
    }

    @Subscribe
    public void onToggleAspectRatio(ToggleAspectRatioEvent ar)
    {
        log.debug("SENDING AR_TOGGLE: " + SageCommand.AR_TOGGLE);
        EventRouter.postCommand(client, SageCommand.AR_TOGGLE);
    }

    /**
     * This is a one time read.  It will return true if we need to switch the player, one time,
     * but it will reset itself AFTER this read, so, only call it once.
     *
     * @return
     */
    public boolean isSwitchingPlayerOneTime()
    {
        boolean change = changePlayerOneTime != null;
        changePlayerOneTime = null;
        return change;
    }

    @Subscribe
    public void onMessage(final MessageEvent event)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Toast.makeText(activity, event.getMessage(), Toast.LENGTH_LONG).show();
                }
                catch (Throwable t)
                {
                    log.error("MESSAGE: {}", event.getMessage());
                }
            }
        });
    }

    DebugKeyPressWindow debugKeyWindow;

    @Subscribe
    public void onDebugKey(final DebugKeyEvent event)
    {
        log.debug("DEBUG KEY: {}", event.fieldName);
        if (debugKeyWindow == null)
        {
            log.debug("Creating debugKeyWindow");
            debugKeyWindow = new DebugKeyPressWindow();
        }

        debugKeyWindow.show(activity);
        debugKeyWindow.showKey(event.fieldName, event.longPress, event.keyCode);
    }

    @Subscribe
    public void onDebugKey(final DebugSageCommandEvent event)
    {
        log.debug("DEBUG SageCommand: {}", event.command.getDisplayName());
        if (debugKeyWindow == null)
        {
            log.debug("Creating debugKeyWindow");
            debugKeyWindow = new DebugKeyPressWindow();
        }

        debugKeyWindow.show(activity);
        debugKeyWindow.showSageCommand(event.command);
    }

    public ViewGroup getVideoViewParent()
    {
        return videoHolderParent;
    }

    // @OnClick(R.id.errorClose)
    public void onCloseClicked()
    {
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

    public void removeVideoFrame()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                log.debug("Removing Video View");
                //videoHolderFrame.removeAllViews();
                videoHolder.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void finish()
    {
        activity.finish();
    }

    @Override
    public Object getSystemService(String windowService)
    {
        return activity.getSystemService(windowService);
    }

    @Override
    public void runOnUiThread(Runnable runnable)
    {
        activity.runOnUiThread(runnable);
    }

    public UIRenderType getUIRenderer()
    {
        return mgr;
    }

    public TextView getCaptionsText()
    {
        return this.captionsText;
    }
}
