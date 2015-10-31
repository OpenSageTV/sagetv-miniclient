package sagex.miniclient.httpbridge;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import sagex.miniclient.MiniClient;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

/**
 * Created by seans on 26/10/15.
 */
public class SageTVHttpMediaServerBridgeTest {
    @Mock
    NanoHTTPD.IHTTPSession session;

    MiniClient client = new MiniClient();
    SageTVHttpMediaServerBridge bridge = new SageTVHttpMediaServerBridge(client, 9000);

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetRange() throws Exception {
        verifyRange("1000-", 1000, 0);
        verifyRange("1000-2000", 1000, 2000);
        verifyRange("-2000", 0, 2000);
    }

    void verifyRange(String range, long start, long end) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Range", "bytes=" + range);

        doReturn(headers).when(session).getHeaders();

        long ranges[] = bridge.getRange(session);

        assertEquals(start, ranges[0]);
        assertEquals(end, ranges[1]);
    }
}