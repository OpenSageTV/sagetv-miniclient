package sagex.miniclient.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import sagex.miniclient.MiniClientMain;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width=720*2;
		config.height=480*2;
		new LwjglApplication(new MiniClientMain(), config);
	}
}
