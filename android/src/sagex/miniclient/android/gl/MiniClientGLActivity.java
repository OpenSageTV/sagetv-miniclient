package sagex.miniclient.android.gl;

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
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.MgrServerInfo;
import sagex.miniclient.MiniClient;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.R;
import sagex.miniclient.android.canvas.CanvasUIManager;
import sagex.miniclient.android.canvas.UIGestureListener;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.uibridge.UIFactory;
import sagex.miniclient.uibridge.UIManager;

/**
 * Created by seans on 20/09/15.
 */
public class MiniClientGLActivity extends Activity {
    public static final String ARG_SERVER_INFO = "server_info";

    private static final String TAG = "MINICLIENT";
    FrameLayout surfaceHolder;

    View pleaseWait = null;
    TextView plaseWaitText = null;

    EGLUIManager mgr;
    GestureDetectorCompat mDetector = null;
    private MiniClientConnection client;

    public MiniClientGLActivity() {
        MiniClient.startup(new String[]{});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.miniclientgl_layout);
        surfaceHolder=(FrameLayout)findViewById(R.id.surface);
        pleaseWait = (View)findViewById(R.id.waitforit);
        plaseWaitText = (TextView)findViewById(R.id.pleaseWaitText);

        MiniClientSurfaceView surface  = new MiniClientSurfaceView(this);
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        surfaceHolder.addView(surface, param);

        mgr = new EGLUIManager(this, surface);
        surface.setRenderer(mgr);

        // non-continous rendering
        // must call GLSurfaceView.requestRender()
        surface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);



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

        final UIGestureListener gestureListener =new UIGestureListener(client);
        mDetector = new GestureDetectorCompat(this, gestureListener);

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
        if (mDetector!=null) {
            //Log.d(TAG, "Passing to touch event: " + event);
            mDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }


    private static final Map<Integer, Integer> KEYMAP = new HashMap<>();

    static {
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, Keys.VK_UP);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, Keys.VK_DOWN);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, Keys.VK_LEFT);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, Keys.VK_RIGHT);
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_SELECT, Keys.VK_ENTER);
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_A, Keys.VK_ENTER);
        KEYMAP.put(KeyEvent.KEYCODE_BUTTON_B, Keys.VK_ESCAPE);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KEYMAP.containsKey(keyCode)) {
            keyCode = KEYMAP.get(keyCode);
            client.postKeyEvent(keyCode, 0, (char)0);
        } else {
            client.postKeyEvent(keyCode, 0, (char)event.getUnicodeChar());
        }
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
}
