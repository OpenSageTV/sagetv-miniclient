package sagex.miniclient;

import java.util.TimerTask;

import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.MouseEvent;

public interface MiniClientConnectionGateway {
	boolean hasFontServer();
	String getServerName();
	void postOfflineCacheChange(boolean b, String lastImageResourceID);
	String getWindowTitle();
	void close();
	void postResizeEvent(Dimension dimension);
	void postKeyEvent(int lastKeyCode, int lastModifiers, char c);
	void postMouseEvent(MouseEvent e);
	void addTimerTask(TimerTask timerTask, long delay, long period);
	void postImageUnload(int oldestImage);
	void postRepaintEvent(int i, int j, int width, int height);
	public sagex.miniclient.uibridge.UIManager<?> getUiManager();
}
