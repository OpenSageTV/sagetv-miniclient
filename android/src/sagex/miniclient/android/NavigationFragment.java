package sagex.miniclient.android;

import android.app.Dialog;
import android.app.DialogFragment;
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

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sagex.miniclient.MiniClient;
import sagex.miniclient.android.events.BackPressedEvent;
import sagex.miniclient.android.events.CloseAppEvent;
import sagex.miniclient.android.events.ShowKeyboardEvent;
import sagex.miniclient.uibridge.EventRouter;
import sagex.miniclient.uibridge.SageTVKey;

/**
 * Created by seans on 05/12/15.
 */
public class NavigationFragment extends DialogFragment {
    static final Logger log = LoggerFactory.getLogger(NavigationFragment.class);
    private final MiniClient client;

    // preconfigured in the MiniClientKeyListener
    static Map<Object, SageTVKey> mappedKeys = EventRouter.NATIVE_UI_KEYMAP;

    // map key code strings to keycode events
    AndroidKeyEventMapper keyMapper = new AndroidKeyEventMapper();

    private View navView;

    @Bind(R.id.nav_options)
    View navOptions = null;

    @Bind(R.id.nav_media_pause)
    View navPause = null;

    public NavigationFragment(MiniClient client) {
        this.client = client;
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

        if (client.isVideoPlaying()) {
            navPause.requestFocus();
        } else {
            navOptions.requestFocus();
        }


        return navView;
    }

    @OnClick({R.id.nav_up, R.id.nav_down, R.id.nav_left, R.id.nav_right, R.id.nav_select, R.id.nav_pgdn, R.id.nav_pgup,
            R.id.nav_options, R.id.nav_home, R.id.nav_media_pause, R.id.nav_media_play, R.id.nav_media_skip_back, R.id.nav_media_skip_forward,
            R.id.nav_media_stop, R.id.nav_back})
    public void buttonClick(View v) {
        try {
            log.debug("Clicked: {}", v.getTag());
            String key = v.getTag().toString().toLowerCase();
            boolean hide = key.startsWith("_");
            if (hide) {
                key = key.substring(1);
            }
            int androidKey = keyMapper.getField(key);
            if (hide) dismiss();
            SageTVKey sageKey = mappedKeys.get(androidKey);
            if (sageKey == null) {
                log.warn("Unmapped key: {}, Missing AndroidKey=>SageTVKey", key);
            } else {
                EventRouter.post(client, sageKey);
            }
        } catch (Throwable t) {
            log.error("Button Not Implemented for {} with ID {}", v.getTag(), v.getId(), t);
        }
    }

    @OnClick({R.id.nav_keyboard, R.id.nav_close})
    public void buttonClickInternal(View v) {
        String tag = v.getTag().toString().toLowerCase();
        if ("_keyboard".equalsIgnoreCase(tag)) {
            dismiss();
            client.eventbus().post(ShowKeyboardEvent.INSTANCE);
        } else if ("_close".equalsIgnoreCase(tag)) {
            client.eventbus().post(CloseAppEvent.INSTANCE);
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
}
