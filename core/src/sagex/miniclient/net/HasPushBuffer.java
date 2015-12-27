package sagex.miniclient.net;

import java.io.IOException;

/**
 * Created by seans on 23/12/15.
 */
public interface HasPushBuffer {
    int bufferAvailable();

    void pushBytes(byte[] bytes, int offset, int len) throws IOException;

    void flush();

    long getBytesRead();

    void release();
}
