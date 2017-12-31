package sagex.miniclient.desktop;

import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sagex.miniclient.MiniClient;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.events.ConnectionLost;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.util.RandomMACAddressResolver;

import java.io.IOException;

public class MiniClientWindow {
    interface ResizeListener {
        void onResized(int w, int h);
    }

    Logger log = LoggerFactory.getLogger(MiniClientWindow.class);

    @FXML
    Label message;
    
    @FXML
    StackPane layers;
    
    Canvas canvas = null;    

    private ServerInfo server;

    private MiniClient client = null;

    public ResizeListener getResizeListener() {
        return resizeListener;
    }

    public void setResizeListener(ResizeListener resizeListener) {
        this.resizeListener = resizeListener;
    }

    private ResizeListener resizeListener;

    public MiniClientWindow() {
    }

    public void connectToServer(ServerInfo server) {
        log.info("Connecting to Server", server);
        this.server=server;
        try {
            message.setText("Connecting to " + server.address);
            MiniClient client = MiniClientInstance.get().getClient();
            client.setUIRenderer(new DesktopClientRenderer(this));
            client.connect(server, new RandomMACAddressResolver(client.properties()));
        } catch (IOException e) {
            log.error("Failed to connect to server: " + server, e);
        }
    }

    @Subscribe
    public void onConnectionLost(ConnectionLost lost) {
        log.debug("Connection Lost", lost);
    }

    @FXML
    private void initialize() {
        client = MiniClientInstance.get().getClient();
        client.eventbus().register(this);
    }

    public static void showAndConnect(ServerInfo si) {
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader();
            root = loader.load(MiniClientWindow.class.getResource("MiniClientWindow.fxml").openStream());
            MiniClientWindow controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("SageTV MiniClient Connection");
            Scene scene = new Scene(root, 1024, 768);
            stage.setScene(scene);
            stage.setOnHidden(e -> controller.hiding());
            scene.widthProperty().addListener((observable, oldValue, newValue) -> controller.onResize(scene.getWidth(), scene.getHeight()));
            scene.heightProperty().addListener((observable, oldValue, newValue) -> controller.onResize(scene.getWidth(), scene.getHeight()));
            stage.show();


            // Hide this current window (if this is what you want)
            //((Node)(event.getSource())).getScene().getWindow().hide();
            controller.connectToServer(si);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onResize(double width, double height) {
        if (resizeListener!=null) resizeListener.onResized((int)width, (int)height);
    }

    private void hiding() {
        log.debug("MiniClient Closing");
        MiniClientInstance.get().getClient().eventbus().unregister(this);
        return;
    }

    public void showCanvas(Dimension screenSize) {
        if (canvas==null) {
            canvas = new Canvas(screenSize.getWidth(), screenSize.getHeight());
            Platform.runLater(() -> layers.getChildren().add(canvas));

            layers.getScene().setOnKeyPressed(event -> {
                if (client.getCurrentConnection()==null) return;
                log.debug("Posting Key Event", event);
                client.getCurrentConnection().postKeyEvent(event.getCode().impl_getCode(), toSageKeyModifier(event), event.getCharacter().charAt(0));
            });

            layers.getScene().setOnMouseMoved(event -> {
                if (client.getCurrentConnection()==null) return;
                //log.debug("Mouse Moved " + event.getX() + "," + event.getY(), event);
                client.getCurrentConnection().postMouseEvent(toSageMouseEvent(event, sagex.miniclient.uibridge.MouseEvent.MOUSE_MOVED));
            });

            layers.getScene().setOnMouseClicked(event -> {
                if (client.getCurrentConnection()==null) return;
                log.debug("Mouse Clicked", event);
                client.getCurrentConnection().postMouseEvent(toSageMouseEvent(event, sagex.miniclient.uibridge.MouseEvent.MOUSE_PRESSED));
                client.getCurrentConnection().postMouseEvent(toSageMouseEvent(event, sagex.miniclient.uibridge.MouseEvent.MOUSE_RELEASED));
                client.getCurrentConnection().postMouseEvent(toSageMouseEvent(event, sagex.miniclient.uibridge.MouseEvent.MOUSE_CLICKED));
            });
        }
    }

    private sagex.miniclient.uibridge.MouseEvent toSageMouseEvent(MouseEvent event, int evtType) {
        if (evtType == sagex.miniclient.uibridge.MouseEvent.MOUSE_CLICKED
                || evtType == sagex.miniclient.uibridge.MouseEvent.MOUSE_PRESSED
                || evtType == sagex.miniclient.uibridge.MouseEvent.MOUSE_RELEASED) {
            return new sagex.miniclient.uibridge.MouseEvent(event.getSource(), evtType, System.currentTimeMillis(), 16, (int) event.getSceneX(), (int) event.getSceneY(), 1, 1, 0);
        } else {
            return new sagex.miniclient.uibridge.MouseEvent(event.getSource(), evtType, System.currentTimeMillis(), 0, (int) event.getSceneX(), (int) event.getSceneY(), event.getClickCount(), event.getButton().ordinal(), 0);
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Dimension getWindowSize() {
        return new Dimension((int)layers.getWidth(), (int)layers.getHeight());
    }

    protected int toSageKeyModifier(KeyEvent event) {
        int modifiers = 0;
        if (event.isShiftDown()) {
            modifiers = modifiers | Keys.SHIFT_MASK;
        }
        if (event.isControlDown()) {
            modifiers = modifiers | Keys.CTRL_MASK;
        }
        if (event.isAltDown()) {
            modifiers = modifiers | Keys.ALT_MASK;
        }
        return modifiers;
    }

}
