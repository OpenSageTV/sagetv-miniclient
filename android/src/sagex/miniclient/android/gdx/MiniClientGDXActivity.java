package sagex.miniclient.android.gdx;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.MgrServerInfo;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.R;
import sagex.miniclient.android.gl.EGLUIManager;
import sagex.miniclient.android.gl.MiniClientSurfaceView;
import sagex.miniclient.android.gl.UIGestureListener;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.uibridge.UIFactory;
import sagex.miniclient.uibridge.UIManager;

/**
 * Created by seans on 20/09/15.
 */
public class MiniClientGDXActivity extends AndroidApplication {
    public static final String ARG_SERVER_INFO = "server_info";

    private static final String TAG = "GDXMINICLIENT";
    FrameLayout surfaceHolder;

    View pleaseWait = null;
    TextView plaseWaitText = null;

    MiniClientRenderer mgr;
    MiniclientTouchListener touchListener;

    private MiniClientConnection client;
    private View miniClientView;

    public MiniClientGDXActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUI();

        setContentView(R.layout.miniclientgl_layout);
        surfaceHolder=(FrameLayout)findViewById(R.id.surface);
        pleaseWait = (View)findViewById(R.id.waitforit);
        plaseWaitText = (TextView)findViewById(R.id.pleaseWaitText);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        mgr = new MiniClientRenderer(this);
        miniClientView =initializeForView(mgr, config);
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
        surfaceHolder.addView(miniClientView);
        miniClientView.requestFocus();

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
        System.setProperty("user.home", getCacheDir().getAbsolutePath());
        final UIFactory factory = new UIFactory() {
            @Override
            public UIManager<?> getUIManager(MiniClientConnection conn) {
                return mgr;
            }
        };
        MgrServerInfo info = new MgrServerInfo(si.address, (si.port==0)?31099:si.port, si.locatorID);
        client = new MiniClientConnection(si.address, getMACAddress(), false, info, factory);
        mgr.setConnection(client);

        miniClientView.setOnTouchListener(new MiniclientTouchListener(this, client));
        miniClientView.setOnKeyListener(new MiniClientKeyListener(client));

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    public void setConnectingIsVisible(final boolean connectingIsVisible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pleaseWait.setVisibility((connectingIsVisible) ? View.VISIBLE : View.GONE);
            }
        });
    }

    String getMACAddress() {
        try {
            WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            String mac= info.getMacAddress();
            if (mac==null) throw new Exception("No WIFI, Will Try eth0");
            return mac;
        } catch (Throwable t) {
            return getMacAddressForEth0();
        }
    }

    static String loadFileAsString(String filePath) throws IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    /*
     * Get the STB MacAddress
     */
    String getMacAddressForEth0(){
        try {
            return loadFileAsString("/sys/class/net/eth0/address")
                    .toUpperCase().substring(0, 17);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
        public void onBackPressed() {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Closing MiniClient")
                    .setMessage("Are you sure you want to close the SageTV MiniClient?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        }

    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                //View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        //| View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        );
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
