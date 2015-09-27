package sagex.miniclient.android.gl;

import android.app.Activity;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.MgrServerInfo;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.android.R;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.uibridge.UIFactory;
import sagex.miniclient.uibridge.UIManager;

import static sagex.miniclient.android.AppUtil.getMACAddress;
import static sagex.miniclient.android.AppUtil.hideSystemUI;

/**
 * Created by seans on 20/09/15.
 */
public class MiniClientGLActivity extends Activity {
    public static final String ARG_SERVER_INFO = "server_info";

    private static final String TAG = "MINICLIENT";
    private static final Map<Integer, Integer> KEYMAP = new HashMap<>();

    static {
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_UP, Keys.VK_UP);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_DOWN, Keys.VK_DOWN);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_LEFT, Keys.VK_LEFT);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, Keys.VK_RIGHT);
        KEYMAP.put(KeyEvent.KEYCODE_DPAD_CENTER, Keys.VK_ENTER);

        //KEYMAP.put(KeyEvent.KEYCODE_BUTTON_SELECT, Keys.VK_ENTER);
        //KEYMAP.put(KeyEvent.KEYCODE_BUTTON_START, Keys.VK_ENTER);

        //KEYMAP.put(KeyEvent.KEYCODE_BUTTON_A, Keys.VK_ENTER); (DPAD Center will catch this)
        //KEYMAP.put(KeyEvent.KEYCODE_BACK, Keys.VK_ESCAPE);

        KEYMAP.put(KeyEvent.KEYCODE_BACK, Keys.VK_ESCAPE);
    }

    FrameLayout surfaceHolder;
    View pleaseWait = null;
    TextView plaseWaitText = null;
    EGLUIManager mgr;
    GestureDetectorCompat mDetector = null;
    long lastPress = 0;
    private MiniClientConnection client;
    private UIGestureListener mGestureListener;

    public MiniClientGLActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUI(this);

        setContentView(R.layout.miniclientgl_layout);
        surfaceHolder=(FrameLayout)findViewById(R.id.surface);
        pleaseWait = findViewById(R.id.waitforit);
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
        client = new MiniClientConnection(si.address, getMACAddress(this), false, info, factory);
        mgr.setConnection(client);

        mGestureListener =new UIGestureListener(client);
        mDetector = new GestureDetectorCompat(this, mGestureListener);

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
            mGestureListener.processRawEvent(mDetector, event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "POST KEYCODE: " +keyCode + "; " + event);

//        // Sometimes the Android remote will auto-send multiple keys
//        // eg, if you hit A, it also sends
//        if (System.currentTimeMillis()-lastPress<100) {
//            Log.d(TAG, "Aborting Android Double Press: " +keyCode + "; " + event);
//        }
        lastPress=System.currentTimeMillis();
        if (KEYMAP.containsKey(keyCode)) {
            keyCode = KEYMAP.get(keyCode);
            client.postKeyEvent(keyCode, 0, (char) 0);
        } else {
            // only send keys that we have vetted, sending other keys will cause issues
            // client.postKeyEvent(keyCode, 0, (char) event.getUnicodeChar());
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
}
