package sagex.miniclient.android.gdx;

import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import sagex.miniclient.MACAddressResolver;
import sagex.miniclient.MiniClient;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.AppUtil;
import sagex.miniclient.android.R;

import static sagex.miniclient.android.AppUtil.confirmExit;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUI(this);

        setContentView(R.layout.miniclientgl_layout);
        ButterKnife.bind(this);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        client = MiniClient.get();

        mgr = new MiniClientRenderer(this, client);
        miniClientView = initializeForView(mgr, config);
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

        ServerInfo si = (ServerInfo) getIntent().getSerializableExtra(ARG_SERVER_INFO);
        if (si == null) {
            log.error("Missing SERVER INFO in Intent: {}", ARG_SERVER_INFO);
            finish();
        }

        plaseWaitText.setText("Connecting to " + si.address + "...");
        setConnectingIsVisible(true);

        startMiniClient(si);
    }

    public void startMiniClient(final ServerInfo si) {
        miniClientView.setOnTouchListener(new MiniclientTouchListener(this, client));
        miniClientView.setOnKeyListener(new MiniClientKeyListener(client));
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
        confirmExit(this);
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
}
