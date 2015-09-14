package sagex.miniclient.uibridge;

import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.UIManager;

public interface UIFactory {
	UIManager<?, ?> getUIManager(MiniClientConnection conn);
}
