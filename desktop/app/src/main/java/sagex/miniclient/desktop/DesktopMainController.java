package sagex.miniclient.desktop;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import sagex.miniclient.MiniClient;
import sagex.miniclient.ServerDiscovery;
import sagex.miniclient.ServerInfo;

import java.io.IOException;

public class DesktopMainController {
    @FXML
    ListView listView;

    ObservableList<ServerInfo> serverList;

    @FXML
    private void initialize() {
        System.out.println("Initialized");
        System.out.println(listView);

        serverList = FXCollections.observableArrayList();

        listView.setItems(serverList);

        listView.setCellFactory(new Callback<ListView<ServerInfo>, ListCell<ServerInfo>>() {
            @Override
            public ListCell call(ListView<ServerInfo> param) {
                return new ServerItemCell();
            }
        });

        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                System.out.println("Item Selected: " + newValue);
                connectToServer((ServerInfo) newValue);
            }
        });

        MiniClientInstance.get().getClient().getServerDiscovery().discoverServersAsync(5000, new ServerDiscovery.ServerDiscoverCallback() {
            @Override
            public void serverDiscovered(ServerInfo si) {
                System.out.println("Got Server" + si);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        serverList.add(si);
                    }
                });
            }
        });
    }

    public void connectToServer(ServerInfo server) {
        MiniClientWindow.showAndConnect(server);
    }
}
