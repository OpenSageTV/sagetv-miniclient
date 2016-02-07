package sagex.miniclient.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sagex.miniclient.MiniClient;
import sagex.miniclient.UserEvent;
import sagex.miniclient.android.events.BackPressedEvent;
import sagex.miniclient.android.events.ChangePlayerOneTime;
import sagex.miniclient.android.events.CloseAppEvent;
import sagex.miniclient.android.events.HideNavigationEvent;
import sagex.miniclient.android.events.HideSystemUIEvent;
import sagex.miniclient.android.events.ShowKeyboardEvent;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.EventRouter;

/**
 * Created by seans on 05/12/15.
 */
public class NavigationFragment extends DialogFragment {
    static final Logger log = LoggerFactory.getLogger(NavigationFragment.class);
    private final MiniClient client;

    private View navView;

    @Bind(R.id.nav_options)
    View navOptions = null;

    @Bind(R.id.nav_media_pause)
    View navPause = null;

    public NavigationFragment() {
        this.client = MiniclientApplication.get().getClient();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Dialog_DoNotDim);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        navView = inflater.inflate(R.layout.navigation, container, false);

        ButterKnife.bind(this, navView);

        if (client.getCurrentConnection().getMenuHint().isOSDMenuNoPopup() && (client.isVideoPlaying() || client.isVideoPaused())) {
            navPause.requestFocus();
        } else {
            navOptions.requestFocus();
        }

        return navView;
    }

    @OnClick({R.id.nav_up, R.id.nav_down, R.id.nav_left, R.id.nav_right, R.id.nav_select, R.id.nav_pgdn, R.id.nav_pgup,
            R.id.nav_options, R.id.nav_home, R.id.nav_media_pause, R.id.nav_media_play, R.id.nav_media_skip_back, R.id.nav_media_skip_back_2,
            R.id.nav_media_skip_forward, R.id.nav_media_skip_forward_2,
            R.id.nav_media_stop, R.id.nav_back})
    public void buttonClick(View v) {
        try {
            log.debug("Clicked: {}", v.getTag());
            String key = v.getTag().toString().toLowerCase();
            boolean hide = key.startsWith("_");
            if (hide) {
                key = key.substring(1);
            }
            if (hide) dismiss();

            // unique case... if the video is paused, and this pause/play button is clicked,
            // the hide the navigation.
            if ("play_pause".equalsIgnoreCase(key) && client.isVideoPaused()) {
                dismiss();
            }

            int sageCommand = UserEvent.getEvtCodeForName(key);
            if (sageCommand == 0) {
                log.warn("Invalid SageTV Command '{}'", key);
            } else {
                EventRouter.postCommand(client, sageCommand);
            }
        } catch (Throwable t) {
            log.error("Button Not Implemented for {} with ID {}", v.getTag(), v.getId(), t);
        }
    }

    @OnClick(R.id.nav_switch_player)
    public void onSwitchPlayer() {
        dismiss();

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_switch_player)
                .setMessage(getResources().getString(R.string.msg_switch_player, getPlayerName(), getOtherPlayerName()))
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        client.properties().setBoolean(PrefStore.Keys.use_exoplayer, !isExoPlayer());
                        AppUtil.message(MiniclientApplication.get().getString(R.string.msg_player_changed, getPlayerName()));
                        dismiss();
                    }
                })
                .setNeutralButton(R.string.yes_once, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        client.eventbus().post(new ChangePlayerOneTime());
                        AppUtil.message(MiniclientApplication.get().getString(R.string.msg_player_changed_one_time, getOtherPlayerName(), getPlayerName()));
                        dismiss();
                    }
                }).show();

    }

    @OnClick(R.id.nav_help)
    public void onHelp() {
        dismiss();
        HelpDialogFragment.showDialog(getActivity());
    }

    private boolean isExoPlayer() {
        return client.properties().getBoolean(PrefStore.Keys.use_exoplayer, false);
    }

    private String getPlayerName() {
        return (isExoPlayer() ? "ExoPlayer" : "IJKPlayer");
    }

    private String getOtherPlayerName() {
        return (isExoPlayer() ? "IJKPlayer" : "ExoPlayer");
    }

    @Override
    public void dismiss() {
        super.dismiss();
        client.eventbus().post(HideSystemUIEvent.INSTANCE);
    }

    @OnClick({R.id.nav_keyboard, R.id.nav_close, R.id.nav_hide})
    public void buttonClickInternal(View v) {
        String tag = v.getTag().toString().toLowerCase();
        if ("_keyboard".equalsIgnoreCase(tag)) {
            dismiss();
            client.eventbus().post(ShowKeyboardEvent.INSTANCE);
        } else if ("_close".equalsIgnoreCase(tag)) {
            client.eventbus().post(CloseAppEvent.INSTANCE);
        } else if ("_hide".equalsIgnoreCase(tag)) {
            client.eventbus().post(HideNavigationEvent.INSTANCE);
        } else {
            log.warn("Nothing Handled Internal Event: {}", tag);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

        wmlp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        wmlp.x = 0;   //x position
        wmlp.y = 0;   //y position

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    client.eventbus().post(BackPressedEvent.INSTANCE);
                    return true;
                }
                return false;
            }
        });
        return dialog;
    }

    public static void showDialog(Activity activity) {
        log.debug("Showing Navigation");
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        Fragment prev = activity.getFragmentManager().findFragmentByTag("nav");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = new NavigationFragment();

        newFragment.show(ft, "nav");

    }
}
