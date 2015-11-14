package sagex.miniclient.httpbridge;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import sagex.miniclient.MiniClient;
import sagex.miniclient.MiniClientOptions;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.prefs.PropertiesPrefStore;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

/**
 * Created by seans on 26/10/15.
 */
public class SageTVHttpMediaServerBridgeTest {
    @Mock
    NanoHTTPD.IHTTPSession session;

    MiniClientOptions options = new MiniClientOptions() {
        @Override
        public PrefStore getPrefs() {
            return new PropertiesPrefStore(new File("test.properties"));
        }

        @Override
        public File getConfigDir() {
            return new File(".");
        }

        @Override
        public File getCacheDir() {
            return new File(".");
        }
    };

    MiniClient client = new MiniClient(options);
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
        headers.put("range", "bytes=" + range);

        doReturn(headers).when(session).getHeaders();

        long ranges[] = bridge.getRange(session);

        assertEquals(start, ranges[0]);
        assertEquals(end, ranges[1]);
    }
}