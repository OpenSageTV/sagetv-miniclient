package sagex.miniclient.httpbridge;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by seans on 16/12/15.
 */
public class PushBufferDataSourceTest {
    @Test
    public void testRead() throws IOException {
        PushBufferDataSource pbds = new PushBufferDataSource(0);
        pbds.open("push:this");
        // setup some push data
        String segment = "0123456789";
        byte data[] = getData(segment, 100).getBytes();
        pbds.pushBytes(data, 0, data.length);

        // read back the first bit of data
        byte buffer[] = new byte[segment.length()];
        int read = pbds.read(0, buffer, 0, buffer.length);
        assertEquals(buffer.length, read);
        String s = new String(buffer);
        assertEquals(segment, s);

        // clear the array
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = 0;
        }

        // let push more data
        pbds.pushBytes(segment.getBytes(), 0, segment.length());
        pbds.pushBytes(segment.getBytes(), 0, segment.length());
        pbds.pushBytes(segment.getBytes(), 0, segment.length());

        // now read a buch of single byte reads and fill the buffer
        for (int i = 0; i < buffer.length; i++) {
            assertEquals(1, pbds.read(0, buffer, i, 1));
        }
        // we should now have a full segment
        s = new String(buffer);
        assertEquals(segment, s);

        // verify that we have read 2 * segments size
        assertEquals(2 * segment.length(), pbds.getBytesRead());

        pbds.close();
    }

    private String getData(String segData, int segments) {
        StringBuilder sb = new StringBuilder(segments * segData.length());
        for (int i = 0; i < segments; i++) {
            sb.append(segData);
        }
        return sb.toString();
    }
}