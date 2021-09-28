package sagex.miniclient.android;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.squareup.otto.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.MiniClient;
import sagex.miniclient.android.events.ToggleAspectRatioEvent;
import sagex.miniclient.events.VideoInfoRefresh;
import sagex.miniclient.util.AspectHelper;
import sagex.miniclient.video.HasVideoInfo;
import sagex.miniclient.video.VideoInfoResponse;

/**
 * Created by seans on 05/12/15.
 */
public class VideoInfoFragment extends DialogFragment {
    static final Logger log = LoggerFactory.getLogger(VideoInfoFragment.class);
    private final MiniClient client;
    private View navView;


    public VideoInfoFragment() {
        this.client = MiniclientApplication.get().getClient();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Dialog_DoNotDim);
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        navView = inflater.inflate(R.layout.video_info, container, false);

        refresh(VideoInfoRefresh.INSTANCE);

        connect(R.id.vib_Close, new Runnable() {
            public void run() {
                dismiss();
            }
        });

        connect(R.id.vib_refresh, new Runnable() {
            public void run() {
                refresh(VideoInfoRefresh.INSTANCE);
            }
        });

        connect(R.id.vib_toggleAR, new Runnable() {
            @Override
            public void run() {
                onToggleAspectRatio();
            }
        });

        connect(R.id.vib_set16_9, new Runnable() {
            @Override
            public void run() {
                client.getUIRenderer().setUIAspectRatio(AspectHelper.ar_16_9);
            }
        });

        connect(R.id.vib_set4_3, new Runnable() {
            @Override
            public void run() {
                client.getUIRenderer().setUIAspectRatio(AspectHelper.ar_4_3);
            }
        });

        connect(R.id.vib_set2_dot_4, new Runnable() {
            @Override
            public void run() {
                client.getUIRenderer().setUIAspectRatio(2.4f);
            }
        });

        return navView;
    }

    @Subscribe
    public void refresh(VideoInfoRefresh refresh)
    {
        VideoInfoResponse resp = ((HasVideoInfo) client.getUIRenderer()).getVideoInfo();

        log.debug("Got a Request for Video Info", resp);

        if (resp!=null)
        {
            if (resp.videoInfo != null)
            {
                setText(R.id.vi_videoSize, resp.videoInfo.size);
                setText(R.id.vi_aspectMode, resp.videoInfo.aspectMode);
                setText(R.id.vi_videoPixelAspect, resp.videoInfo.size.getAR());
                setText(R.id.vi_aspectRatio, resp.videoInfo.aspectRatio);
                setText(R.id.vi_sagetvDestRect, resp.videoInfo.destRect);
            }
            setText(R.id.vi_sagetvScreenAspect, resp.uiAspectRatio);
            if (resp.uiScreenSizePixels != null)
            {
                setText(R.id.vi_screenAdjustedSize, resp.uiScreenSizePixels.copy().updateHeightUsingAspectRatio(resp.uiAspectRatio));
                setText(R.id.vi_screenPixelAR, resp.uiScreenSizePixels.getAR());
                setText(R.id.vi_screenPixelSize, resp.uiScreenSizePixels);
            }
            setText(R.id.vi_uri, resp.uri);
        }
    }

    public void connect(int id, final Runnable runnable) {
        Button b = (Button) navView.findViewById(id);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    void setText(int id, Object text) {
        TextView tv = (TextView) navView.findViewById(id);
        if (tv!=null) {
            String val = (text==null)?"":text.toString();
            tv.setText(val);
        }
    }

    // @OnClick(R.id.nav_toggle_ar)
    public void onToggleAspectRatio() {
        client.eventbus().post(ToggleAspectRatioEvent.INSTANCE);
    }

    @Override
    public void onPause() {
        super.onPause();
        client.eventbus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        client.eventbus().register(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = new WindowManager.LayoutParams();
        wmlp.copyFrom(dialog.getWindow().getAttributes());

        wmlp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        wmlp.x = 0;   //x position
        wmlp.y = 0;   //y position
        wmlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(wmlp);
//        d.show();
//        d.getWindow().setAttributes(lp);

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    public static void showDialog(Activity activity) {
        log.debug("Showing Video Info");
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        Fragment prev = activity.getFragmentManager().findFragmentByTag("vidinfo");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = new VideoInfoFragment();
        newFragment.show(ft, "vidinfo");
    }
}
