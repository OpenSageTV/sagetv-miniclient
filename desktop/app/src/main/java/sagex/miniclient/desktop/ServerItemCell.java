package sagex.miniclient.desktop;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import sagex.miniclient.ServerInfo;

import java.io.IOException;

public class ServerItemCell extends ListCell<ServerInfo> {
    Label label = new Label();

    public ServerItemCell() {
        super();
    }

    @Override
    protected void updateItem(ServerInfo server, boolean empty) {
        super.updateItem(server, empty);
        setText(null);
        if (empty || server==null) {
            setText(null);
            setGraphic(null);
        } else {
            label.setText(server.name);
            setGraphic(label);
        }
    }
}
