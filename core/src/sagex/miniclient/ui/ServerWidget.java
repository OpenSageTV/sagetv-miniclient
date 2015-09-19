package sagex.miniclient.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import sagex.miniclient.Assets;
import sagex.miniclient.ServerDiscovery;

/**
 * Created by seans on 18/09/15.
 */
public class ServerWidget extends Table {
    private final ServerDiscovery.ServerInfo server;

    public ServerWidget(ServerDiscovery.ServerInfo si) {
        super(Assets.get().skin);
        this.server = si;
        add(si.name);
        row();
        add(si.address);
        padTop(10);
        padBottom(10);
        pack();
    }
}
