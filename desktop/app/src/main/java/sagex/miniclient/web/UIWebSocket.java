package sagex.miniclient.web;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sagex.miniclient.MiniClient;
import sagex.miniclient.ServerInfo;
import sagex.miniclient.desktop.DesktopClientRenderer;
import sagex.miniclient.desktop.MiniClientInstance;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Keys;
import sagex.miniclient.util.ClientIDGenerator;
import sagex.miniclient.util.RandomMACAddressResolver;

import java.io.IOException;

@WebSocket
public class UIWebSocket {
    Logger log = LoggerFactory.getLogger(UIWebSocket.class);

    @OnWebSocketConnect
    public void connected(Session session) {
        log.debug("Connected from " + session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        log.debug("Session Closed From " + session.getRemoteAddress());
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        log.debug("Message: ", message);
        MiniClient client = MiniClientInstance.get().getClient();
        if (message.startsWith("connect ")) {
            log.debug("Connecting: ", message);

            // "connect to " + server + " from " + clientId + " with screen size " + screenW + " " + screenH
            String  parts[] = message.split("\\s+");

            String server = parts[2];
            String clientId = parts[4];
            int w = Integer.parseInt(parts[8]);
            int h = Integer.parseInt(parts[9]);

            String serverParts[] = server.split(":");
            int port = Integer.parseInt(serverParts[1]);

            client.setUIRenderer(new WebUIRenderer(session, w, h));
            ServerInfo si = new ServerInfo();
            si.address = serverParts[0];
            si.name = server;
            si.port = port;
            //si.macAddress = new ClientIDGenerator().generateId(clientId);
            client.connect(si, new RandomMACAddressResolver(client.properties()));
            //session.getRemote().sendString("connected");
        } else if (message.startsWith("key ")) {
            // handle keyboard
            log.debug("Key Press: " + message);
            String parts[] = message.split("\\s+");
            String key = parts[2];
            int keyCode = Integer.parseInt(parts[1]);
            switch (keyCode) {
                case 13:
                    client.getCurrentConnection().postKeyEvent(Keys.VK_ENTER, 0, (char)0);
                    break;

                default:
                    client.getCurrentConnection().postKeyEvent(keyCode, 0, (char)0);
            }
        }
        else if (message.startsWith("resize ")) {
            String parts[] = message.split("\\s+");
            client.getCurrentConnection().postResizeEvent(new Dimension(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
        }
    }
}
