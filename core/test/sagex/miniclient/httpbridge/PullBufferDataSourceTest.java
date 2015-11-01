package sagex.miniclient.httpbridge;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by seans on 01/11/15.
 */
public class PullBufferDataSourceTest {

    @Test
    public void testGetPath() throws Exception {
        PullBufferDataSource ds = new PullBufferDataSource(0);
        assertEquals("/a/b/c/d.avi", ds.getPath("stv://192.168.1.10//a/b/c/d.avi"));
        assertEquals("D:\\Music\\FC Kahuna - Machine Says Yes\\(1) Hayling.wav", ds.getPath("stv://192.168.2.10/D:\\Music\\FC Kahuna - Machine Says Yes\\(1) Hayling.wav"));
    }

    @Test
    public void testGetHost() throws Exception {
        PullBufferDataSource ds = new PullBufferDataSource(0);
        assertEquals("192.168.1.10", ds.getHost("stv://192.168.1.10//a/b/c/d.avi"));
        assertEquals("192.168.2.10", ds.getHost("stv://192.168.2.10/D:\\Music\\FC Kahuna - Machine Says Yes\\(1) Hayling.wav"));
    }
}