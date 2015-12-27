package sagex.miniclient.net;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by seans on 22/12/15.
 */
public class SimplePullDataSourceTest {
    static final byte data[] = "123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ".getBytes();

    private class MockStream extends InputStream {
        int counter = 0;

        @Override
        public int read() throws IOException {
            if (counter > data.length) return -1;
            return data[counter++];
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            // we only ever return x bytes no matter what
            // in order to similute a multiple read to fill the buffers
            len = 3;
            return super.read(b, off, len);
        }
    }


    @Test
    public void testReadNoPosition() throws Exception {
        SimplePullDataSource pds = new SimplePullDataSource();
        pds.remoteReader = new DataInputStream(new MockStream());
        pds.remoteWriter = new ByteArrayOutputStream(); // dummy output stream
        pds.verboseLogging = true;
        pds.opened = true;
        byte buffer[] = new byte[100];
        int size = 3 * 9;
        int read = pds.read(0, buffer, 0, size);
        assertEquals(size, read);
        assertEquals(new String(data, 0, size), new String(buffer, 0, size));
    }

    @Test
    public void testReadWithPosition() throws Exception {
        SimplePullDataSource pds = new SimplePullDataSource();
        pds.remoteReader = new DataInputStream(new MockStream());
        pds.remoteWriter = new ByteArrayOutputStream(); // dummy output stream
        pds.verboseLogging = true;
        pds.opened = true;
        byte buffer[] = new byte[100];
        int size = 3 * 9;
        int read = pds.read(1000, buffer, 0, size); // 1000 should not affect the buffer
        assertEquals(size, read);
        assertEquals(new String(data, 0, size), new String(buffer, 0, size));
    }

    @Test
    public void testReadBufferNoOffset() throws Exception {
        SimplePullDataSource pds = new SimplePullDataSource();
        pds.remoteReader = new DataInputStream(new MockStream());
        pds.verboseLogging = true;
        byte buffer[] = new byte[100];
        int size = 3 * 9;
        int read = pds.readBuffer(buffer, 0, size);
        assertEquals(size, read);
        assertEquals(new String(data, 0, size), new String(buffer, 0, size));
    }

    @Test
    public void testReadBufferWithOffset() throws Exception {
        SimplePullDataSource pds = new SimplePullDataSource();
        pds.remoteReader = new DataInputStream(new MockStream());
        pds.verboseLogging = true;
        byte buffer[] = new byte[100];
        int size = 3 * 9;
        int offset = 5;
        int read = pds.readBuffer(buffer, offset, size);
        assertEquals(size, read);
        assertEquals(new String(data, 0, size), new String(buffer, offset, size));
        System.out.println("data[" + new String(buffer, offset, size) + "]");
    }
}