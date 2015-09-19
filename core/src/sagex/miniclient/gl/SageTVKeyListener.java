package sagex.miniclient.gl;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import java.util.HashMap;

import sagex.miniclient.MiniClientConnectionGateway;
import sagex.miniclient.MiniClientMain;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.uibridge.MouseEvent;

/**
 * Created by seans on 18/09/15.
 */
public class SageTVKeyListener extends InputListener {
    static final HashMap<Integer, Integer> KEYMAP = new HashMap<Integer, Integer>();
    
    static {
        KEYMAP.put(Input.Keys.UP, Keys.VK_UP);
        KEYMAP.put(Input.Keys.DPAD_UP, Keys.VK_UP);
        KEYMAP.put(Input.Keys.DOWN, Keys.VK_DOWN);
        KEYMAP.put(Input.Keys.DPAD_DOWN, Keys.VK_DOWN);
        KEYMAP.put(Input.Keys.LEFT, Keys.VK_LEFT);
        KEYMAP.put(Input.Keys.DPAD_LEFT, Keys.VK_LEFT);
        KEYMAP.put(Input.Keys.RIGHT, Keys.VK_RIGHT);
        KEYMAP.put(Input.Keys.DPAD_RIGHT, Keys.VK_RIGHT);
        KEYMAP.put(Input.Keys.BUTTON_SELECT, Keys.VK_ENTER);
        KEYMAP.put(Input.Keys.BUTTON_A, Keys.VK_ENTER);
        KEYMAP.put(Input.Keys.BUTTON_B, Keys.VK_ESCAPE);
    }

    private final MiniClientConnectionGateway myConn;

    public SageTVKeyListener(MiniClientConnectionGateway conn) {
        this.myConn = conn;
    }
    @Override
    public boolean keyUp(InputEvent event, int keycode) {
        System.out.println("Post Key Press: " + keycode + "; char: " + event.getCharacter());
        if (KEYMAP.containsKey(keycode)) {
            keycode = KEYMAP.get(keycode);
        }
        myConn.postKeyEvent(keycode, 0, event.getCharacter());
        return true;
    }

    @Override
    public boolean mouseMoved(InputEvent event, float x, float y) {
        myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 16, (int)x, MiniClientMain.HEIGHT - (int)y, 1, 1, 0));
        return true;
    }

}
