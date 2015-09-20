package sagex.miniclient.uibridge;

import sagex.miniclient.MiniClientConnection;

public interface UIFactory {
	UIManager<?> getUIManager(MiniClientConnection conn);
}
