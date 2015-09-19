package sagex.miniclient.gl;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;

import sagex.miniclient.MiniClientConnectionGateway;
import sagex.miniclient.MiniClientMain;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.uibridge.MouseEvent;

/**
 * Created by seans on 18/09/15.
 */
public class SageTVGestureListener extends ActorGestureListener {
    private final MiniClientConnectionGateway myConn;

    public SageTVGestureListener(MiniClientConnectionGateway myConn) {
        this.myConn=myConn;
    }
    @Override
    public void tap(InputEvent event, float x, float y, int count, int button) {
        System.out.println("TAP: " + x + "," + y);
        myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 16, (int)x, MiniClientMain.HEIGHT - (int)y, 1, 1, 0));
    }

    int flingThreshold = 200;

    @Override
    public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
        myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 16, (int)x, MiniClientMain.HEIGHT - (int)y, 1, 1, 0));
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 16, (int)x, MiniClientMain.HEIGHT - (int)y, 1, 1, 0));
    }

    @Override
    public boolean longPress(Actor actor, float x, float y) {
        myConn.postKeyEvent(Keys.VK_ENTER, 0, (char)13);
        return true;
    }

    @Override
    public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
        myConn.postMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 16, (int)x, MiniClientMain.HEIGHT - (int)y, 1, 1, 0));
    }

    @Override
    public void fling(InputEvent event, float velocityX, float velocityY, int button) {
        System.out.println("FLING: " + velocityX + "," + velocityY);
        if (velocityX > flingThreshold) {
            System.out.println("Flight Right");
            myConn.postKeyEvent(Keys.VK_RIGHT, 0, (char) 0);
        } else if (velocityX < -flingThreshold) {
            myConn.postKeyEvent(Keys.VK_LEFT, 0, (char) 0);
        } else if (velocityY > flingThreshold) {
            myConn.postKeyEvent(Keys.VK_UP, 0, (char) 0);
        } else if (velocityY < -flingThreshold) {
            myConn.postKeyEvent(Keys.VK_DOWN, 0, (char) 0);
        }
    }

    @Override
    public void pinch(InputEvent event, Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        System.out.println("Pinch == Escape");
        myConn.postKeyEvent(Keys.VK_ESCAPE, 0, (char)27);
    }

    @Override
    public void zoom(InputEvent event, float initialDistance, float distance) {
        System.out.println("Zoom == Enter");
        myConn.postKeyEvent(Keys.VK_ENTER, 0, (char)13);
    }

}
