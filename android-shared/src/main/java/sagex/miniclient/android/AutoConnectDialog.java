package sagex.miniclient.android;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.ServerInfo;
import sagex.miniclient.prefs.PrefStore;

/**
 * Created by seans on 22/11/15.
 */
public class AutoConnectDialog extends DialogFragment {
    static final Logger log = LoggerFactory.getLogger(AutoConnectDialog.class);

    TextView connectText = null;
    CircularProgressView progressView;

    int countdownSecs = 5;
    int totalCountDownMS = countdownSecs * 1000;
    boolean autoConnect = true;
    CountDownTimer countDownTimer;
    boolean connected = false;
    ServerInfo serverInfo;

    public AutoConnectDialog() {
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Base_Theme_AppCompat_Dialog);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Base_Theme_AppCompat_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_autoconnect, container, false);

        connectText = (TextView) v.findViewById(R.id.text);
        progressView = (CircularProgressView) v.findViewById(R.id.progress_view);
        v.findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onOK();
            }
        });

        v.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });


        return v;
    }

    private void processOnFinish() {
        log.debug("Finished");
        if (autoConnect) {
            connect();
        } else {
            dismissQuietly();
        }
    }

    private synchronized void connect() {
        if (connected) {
            dismiss();
            return;
        }
        connected = true;
        dismiss();
        if (serverInfo != null) {
            ServersActivity.connect(getActivity(), serverInfo);
        }
    }

    // @OnClick(R.id.ok)
    public void onOK() {
        countDownTimer.cancel();
        connect();
    }

    // @OnClick(R.id.cancel)
    public void onCancel() {
        autoConnect = false;
        countDownTimer.cancel();
        dismissQuietly();
    }

    private void updateText() {
        connectText.setText(getResources().getString(R.string.msg_auto_connect, getTimeLeft()));
    }

    private int getTimeLeft() {
        return countdownSecs;
    }

    @Override
    public void onPause() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            dismissQuietly();
        }
        super.onPause();
    }

    private void dismissQuietly() {
        try {
            dismiss();
        } catch (Throwable t) {
        }
    }

    @Override
    public void onResume() {
        autoConnect = true;
        countdownSecs = MiniclientApplication.get().getClient().properties().getInt(PrefStore.Keys.auto_connect_delay, 10);
        serverInfo = MiniclientApplication.get().getClient().getServers().getLastConnectedServer();
        updateText();

        totalCountDownMS = countdownSecs * 1000;
        progressView.setMaxProgress(totalCountDownMS);
        progressView.setProgress(totalCountDownMS);

        super.onResume();

        countDownTimer = new CountDownTimer(totalCountDownMS, 250) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdownSecs = (int) (millisUntilFinished / 1000);
                updateText();
                progressView.setProgress(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                processOnFinish();
            }
        };
        countDownTimer.start();
    }
}
