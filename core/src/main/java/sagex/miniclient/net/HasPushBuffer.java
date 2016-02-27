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

    /**
     * When NO more data is coming EOS is set, and the buffer can drain any remaining bytes, but
     * once those bytes are gone, the read command will return -1
     */
    void setEOS();
}
