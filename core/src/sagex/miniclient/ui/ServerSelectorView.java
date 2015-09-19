package sagex.miniclient.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Array;

import java.io.IOException;

import sagex.miniclient.Assets;
import sagex.miniclient.MgrServerInfo;
import sagex.miniclient.MiniClient;
import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.MiniClientMain;
import sagex.miniclient.ServerDiscovery;
import sagex.miniclient.gl.OpenGLFBUIManager;

import static sagex.miniclient.MiniClientMain.HEIGHT;
import static sagex.miniclient.MiniClientMain.INSTANCE;
import static sagex.miniclient.MiniClientMain.WIDTH;
import static sagex.miniclient.MiniClientMain.requestRendering;

/**
 * Created by seans on 18/09/15.
 */
public class ServerSelectorView extends Group {
    public ServerSelectorView() {
        create();
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        return listView;
    }

    List<ServerDiscovery.ServerInfo> listView;

    public void create() {
        setSize(WIDTH, HEIGHT);
        final Table table = new Table(Assets.get().skin);
        //table.setDebug(true);
        table.setSize(WIDTH, HEIGHT);
        Label searching = new Label("SageTV Servers...",Assets.get().skin);
        table.add(searching);
        addActor(table);

        final Array<ServerDiscovery.ServerInfo> items = new Array<ServerDiscovery.ServerInfo>();
        listView = new List<ServerDiscovery.ServerInfo>(Assets.get().skin);
        listView.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                listView.getStage().setKeyboardFocus(listView);
                return false;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                boolean consumed = false;

                int selectedIndex = listView.getSelectedIndex();

                switch (keycode) {
                    case Input.Keys.UP: {
                        listView.setSelectedIndex(Math.max(selectedIndex - 1, 0));
                    }
                    break;

                    case Input.Keys.DOWN: {
                        listView.setSelectedIndex(Math.min(selectedIndex + 1, listView.getItems().size - 1));
                    }
                    break;

                    case Input.Keys.BUTTON_SELECT:
                    case Input.Keys.BUTTON_A:
                    case Input.Keys.ENTER: {
                        connectTo(listView.getSelected());
                        consumed=true;
                    }
                    break;
                }

                return consumed;
            }
        });
        listView.addListener(new ActorGestureListener() {
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                connectTo(listView.getSelected());
            }
        });

        table.row();
        table.add(listView);

        ServerDiscovery.discoverServersAsync(10000, new ServerDiscovery.ServerDiscoverCallback() {
            @Override
            public void serverDiscovered(final ServerDiscovery.ServerInfo si) {
                getStage().setKeyboardFocus(listView);
                ServerWidget sw = new ServerWidget(si);
                items.add(si);
                listView.setItems(items);
                requestRendering();
            }
        });
    }

    private void connectTo(ServerDiscovery.ServerInfo si) {
        // remove this page from the UI
        remove();

        // start the miniclient connector
        MiniClient.startup(new String[]{});
        MgrServerInfo info = new MgrServerInfo(si.address, 31099, null);
        MiniClientConnection client = new MiniClientConnection(si.address, null, true, info, OpenGLFBUIManager.getUIFactory());
        try {
            client.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
