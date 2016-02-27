package sagex.miniclient.net;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by seans on 22/12/15.
 */
public class SimplePullDataSourceTest {
    static final byte data[] = loadData(100).getBytes();

    static String loadData(int size) {
        // make sure we have at least 20 bytes so we can validate the stream
        assertTrue(size > 20);
        MockInputStream mis = new MockInputStream(size);
        byte buffer[] = new byte[size];
        try {
            mis.read(buffer, 0, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String s = new String(buffer, 0, size);
        // just make sure our MockInputStream is giving us the expected repeated data
        assertEquals(s.length(), size);
        assertEquals("01234567890123456789", s.substring(0, 20));
        // System.out.println("DATA:["+s+"]");
        return s;
    }

    @Test
    public void testReadNoPosition() throws Exception {
        SimplePullDataSource pds = new SimplePullDataSource();
        pds.remoteReader = new DataInputStream(new MockInputStream(100));
        pds.remoteWriter = new ByteArrayOutputStream(); // dummy output stream
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
        pds.remoteReader = new DataInputStream(new MockInputStream(2000));
        pds.remoteWriter = new ByteArrayOutputStream(); // dummy output stream
        pds.opened = true;
        pds.size = 3000; // need to tell the datasource we have lots of data
        byte buffer[] = new byte[100];
        int size = 3 * 9;
        int read = pds.read(1000, buffer, 0, size); // 1000 should not affect the buffer
        assertEquals(size, read);
        assertEquals(new String(data, 0, size), new String(buffer, 0, size));
    }

    @Test
    public void testReadBufferNoOffset() throws Exception {
        SimplePullDataSource pds = new SimplePullDataSource();
        pds.remoteReader = new DataInputStream(new MockInputStream(100));
        byte buffer[] = new byte[100];
        int size = 3 * 9;
        int read = pds.readBuffer(buffer, 0, size);
        assertEquals(size, read);
        assertEquals(new String(data, 0, size), new String(buffer, 0, size));
    }

    @Test
    public void testReadBufferWithOffset() throws Exception {
        SimplePullDataSource pds = new SimplePullDataSource();
        pds.remoteReader = new DataInputStream(new MockInputStream(100));
        byte buffer[] = new byte[100];
        int size = 3 * 9;
        int offset = 5;
        int read = pds.readBuffer(buffer, offset, size);
        assertEquals(size, read);
        assertEquals(new String(data, 0, size), new String(buffer, offset, size));
        System.out.println("data[" + new String(buffer, offset, size) + "]");
    }

    @Test
    public void testGetPath() throws Exception {
        SimplePullDataSource ds = new SimplePullDataSource();
        assertEquals("/a/b/c/d.avi", ds.getPath("stv://192.168.1.10//a/b/c/d.avi"));
        assertEquals("D:\\Music\\FC Kahuna - Machine Says Yes\\(1) Hayling.wav", ds.getPath("stv://192.168.2.10/D:\\Music\\FC Kahuna - Machine Says Yes\\(1) Hayling.wav"));
    }

    @Test
    public void testGetHost() throws Exception {
        SimplePullDataSource ds = new SimplePullDataSource();
        assertEquals("192.168.1.10", ds.getHost("stv://192.168.1.10//a/b/c/d.avi"));
        assertEquals("192.168.2.10", ds.getHost("stv://192.168.2.10/D:\\Music\\FC Kahuna - Machine Says Yes\\(1) Hayling.wav"));
    }
}