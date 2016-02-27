package sagex.miniclient.net;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by seans on 27/12/15.
 */
public class BufferedPullDataSourceTest {
    static final byte data[] = SimplePullDataSourceTest.loadData(100).getBytes();

    @Test
    public void testRead() throws Exception {
        BufferedPullDataSource pds = spy(new BufferedPullDataSource());
        pds.remoteReader = new DataInputStream(new MockInputStream(BufferedPullDataSource.MAX_BUFFER * 5));
        pds.remoteWriter = new ByteArrayOutputStream(); // dummy output stream
        pds._buffer = new byte[BufferedPullDataSource.MAX_BUFFER];
        pds.opened = true;
        pds.size = BufferedPullDataSource.MAX_BUFFER * 5;

        byte buffer[] = new byte[BufferedPullDataSource.MAX_BUFFER];

        // read a single byte, which should force the buffer to fill
        int size = 1;
        int read = pds.read(0, buffer, 0, 1);
        assertEquals(size, read);
        assertEquals(new String(data, 0, size), new String(buffer, 0, size));
        verify(pds, times(1)).fillBuffer(eq(0l));

        // now read a bunch of other single byte reads, which should NOT force a buffer re-fill
        read = pds.read(0, buffer, 1, 1);
        assertEquals(size, read);
        assertEquals(new String(data, 1, size), new String(buffer, 1, size));
        verify(pds, times(1)).fillBuffer(eq(0l));

        // now read some data that is outside the buffer range, and it should force a re-read
        read = pds.read(BufferedPullDataSource.MAX_BUFFER * 2, buffer, 0, 1);
        assertEquals(size, read);
        //assertEquals(((BufferedPullDataSource.MAX_BUFFER*2)%10)+'0', buffer[0]);
        verify(pds, times(1)).fillBuffer(eq(BufferedPullDataSource.MAX_BUFFER * 2l));
    }

    @Test
    public void testLargeBufferRead() throws Exception {
        BufferedPullDataSource pds = spy(new BufferedPullDataSource());
        pds.remoteReader = new DataInputStream(new MockInputStream(BufferedPullDataSource.MAX_BUFFER * 5));
        pds.remoteWriter = new ByteArrayOutputStream(); // dummy output stream
        pds._buffer = new byte[BufferedPullDataSource.MAX_BUFFER];
        pds.opened = true;
        pds.size = BufferedPullDataSource.MAX_BUFFER * 5;

        byte buffer[] = new byte[BufferedPullDataSource.MAX_BUFFER * 2];

        // read a large buffer, which should force the buffer to have to fill twice
        int read = pds.read(0, buffer, 0, buffer.length);
        assertEquals(buffer.length, read);
        verify(pds, times(1)).fillBuffer(eq(0l));
        verify(pds, times(1)).fillBuffer(eq((long) BufferedPullDataSource.MAX_BUFFER));
    }


    @Test
    public void testLinearRead() throws Exception {
        BufferedPullDataSource pds = spy(new BufferedPullDataSource());
        pds.remoteReader = new DataInputStream(new MockInputStream(BufferedPullDataSource.MAX_BUFFER * 3));
        pds.remoteWriter = new ByteArrayOutputStream(); // dummy output stream
        pds._buffer = new byte[BufferedPullDataSource.MAX_BUFFER];
        pds.opened = true;
        pds.size = BufferedPullDataSource.MAX_BUFFER * 3;

        byte buffer[] = new byte[1];

        int reads = 0;
        while (true) {
            int val = pds.read(reads++, buffer, 0, 1);
            if (val == -1) break;
        }


        // 3 fully buffer reads + another read that return a -1
        verify(pds, times(4)).fillBuffer(anyLong());

        // our total reads should be 3 full buffers + 1 extra read for the -1 return
        assertEquals(BufferedPullDataSource.MAX_BUFFER * 3 + 1, reads);
    }

}