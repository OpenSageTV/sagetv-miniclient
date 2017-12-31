package sagex.miniclient.web;

import sagex.miniclient.desktop.MiniClientInstance;

import static spark.Spark.*;

public class ClientServer {
    public static void main(String args[]) {
        port(8099);

        staticFiles.location("/web");
        webSocket("/ui", UIWebSocket.class);

        get("/hello", (req, res) -> "Hello");
        get("/servers", (req, res) -> {
             return MiniClientInstance.get().getClient().getServerDiscovery().discoverServers(3000, null);
        }, new JsonTransformer());
    }
}
