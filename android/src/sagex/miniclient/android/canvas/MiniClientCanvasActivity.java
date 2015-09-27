package sagex.miniclient.android.canvas;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import sagex.miniclient.MgrServerInfo;
import sagex.miniclient.MiniClient;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.R;
import sagex.miniclient.android.gdx.MiniClientKeyListener;
import sagex.miniclient.android.gdx.MiniclientTouchListener;
import sagex.miniclient.uibridge.UIFactory;
import sagex.miniclient.uibridge.UIManager;

import static sagex.miniclient.android.AppUtil.confirmExit;
import static sagex.miniclient.android.AppUtil.getMACAddress;
import static sagex.miniclient.android.AppUtil.hideSystemUI;

/**
 * Created by seans on 20/09/15.
 */
public class MiniClientCanvasActivity extends Activity {
    public static final String ARG_SERVER_INFO = "server_info";

    private static final String TAG = "MINICLIENT";
    SurfaceView surface;

    View pleaseWait = null;
    TextView plaseWaitText = null;

    CanvasUIManager mgr;
    private MiniClientConnection client;

    public MiniClientCanvasActivity() {
        MiniClient.startup(new String[]{});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUI(this);

        setContentView(R.layout.miniclient_layout);
        surface=(SurfaceView)findViewById(R.id.surface);
        pleaseWait = findViewById(R.id.waitforit);
        plaseWaitText = (TextView)findViewById(R.id.pleaseWaitText);

        mgr = new CanvasUIManager(this);
        surface.getHolder().addCallback(mgr);

        System.setProperty("user.home", getCacheDir().getAbsolutePath());

        ServerInfo si = (ServerInfo) getIntent().getSerializableExtra(ARG_SERVER_INFO);
        if (si==null) {
            Log.e(TAG, "Missing SERVER INFO in Intent: " + ARG_SERVER_INFO );
            finish();
        }

        plaseWaitText.setText("Connecting to " + si.address + "...");
        setConnectingIsVisible(true);

        startMiniClient(si);
    }

    public void startMiniClient(final ServerInfo si) {
        final UIFactory factory = new UIFactory() {
            @Override
            public UIManager<?> getUIManager(MiniClientConnection conn) {
                return mgr;
            }
        };
        MgrServerInfo info = new MgrServerInfo(si.address, (si.port==0)?31099:si.port, si.locatorID);
        client = new MiniClientConnection(si.address, getMACAddress(this), false, info, factory);
        mgr.setConnection(client);

        surface.setOnTouchListener(new MiniclientTouchListener(this, client));
        surface.setOnKeyListener(new MiniClientKeyListener(client));


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.connect();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Closing MiniClient Connection");

        try {
            if (client!=null) {
                client.close();
            }
        } catch (Throwable t) {
            Log.w(TAG, "Error shutting down client", t);
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

    @Override
    public void onBackPressed() {
        confirmExit(this);
    }
}
