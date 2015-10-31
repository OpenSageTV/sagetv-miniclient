package sagex.miniclient.uibridge;

/**
 * Created by seans on 24/10/15.
 */
public class SageTVKey {
    public final int keyCode;
    public final int modifiers;
    public final char keyChar;

    public SageTVKey(int keyCode, int modifiers, char keyChar) {
        this.keyCode = keyCode;
        this.modifiers = modifiers;
        this.keyChar = keyChar;
    }

    public SageTVKey(int keyCode, int modifiers) {
        this(keyCode, modifiers, (char) 0);
    }

    public SageTVKey(int keyCode) {
        this(keyCode, 0, (char) 0);
    }
}
