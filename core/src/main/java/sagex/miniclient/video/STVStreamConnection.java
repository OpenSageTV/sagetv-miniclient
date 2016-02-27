package sagex.miniclient.video;

import java.net.URI;

/**
 * Created by seans on 28/09/15.
 */
public class STVStreamConnection {
    private final String url;
    private final URI uri;

    public STVStreamConnection(String url) {
        this.url = url;
        this.uri = URI.create(url);
    }
}
