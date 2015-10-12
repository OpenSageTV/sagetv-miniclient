package sagex.miniclient.httpbridge;

import java.io.IOException;

/**
 * Created by seans on 06/10/15.
 */
public interface DataSource {
    String getUri();

    void setUri(String uri);

    long open(String uri) throws IOException;

    int read(long streamOffset, byte[] bytes, int offset, int len) throws IOException;

    void close();

}
