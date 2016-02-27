package sagex.miniclient.net;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by seans on 27/12/15.
 */
public class MockInputStream extends InputStream {
    private final int size;
    private int readPos = 0;

    public MockInputStream(int size) {
        this.size = size;
    }

    @Override
    public int read() throws IOException {
        if (readPos >= size) return -1;
        return (readPos++ % 10) + '0'; // returns a stream of 0123456789012345... bytes
    }
}
