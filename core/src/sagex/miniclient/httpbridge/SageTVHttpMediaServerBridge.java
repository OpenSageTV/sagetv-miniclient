package sagex.miniclient.httpbridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;
import sagex.miniclient.MiniClient;

/**
 * The WebServer is a single instance, single connection server.  ie, a second connection will
 * terminate the first, and create a new stream for the media.
 * <p/>
 * Created by seans on 06/10/15.
 */
public class SageTVHttpMediaServerBridge extends NanoHTTPD {
    private static Logger log = LoggerFactory.getLogger(SageTVHttpMediaServerBridge.class);

    private final MiniClient client;

    private DataSource dataSource;
    private InputStream inputStream;

    public SageTVHttpMediaServerBridge(MiniClient client, int port) {
        super(port);
        this.client = client;
        log.info("SageTV Http Brige Online using Port {}", port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        log.debug("Request: " + session.getUri());
        if (dataSource instanceof PushBufferDataSource && hasRangeHeader(session)) {
            // return the last stream
            return NanoHTTPD.newChunkedResponse(Response.Status.OK, "video/mp2t", inputStream);
        }
        if (session.getMethod() == Method.GET) {
            return NanoHTTPD.newChunkedResponse(Response.Status.OK, "video/mp2t", getStreamVideo());
            //return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "video/mp2t", getStreamVideo(), -1);
        } else if (session.getMethod() == Method.HEAD) {
            log.error("Invalid Method: " + session.getMethod());
            return NanoHTTPD.newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method Not Allowed: " + session.getMethod());
        } else {
            log.error("Invalid Method: " + session.getMethod());
            return NanoHTTPD.newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method Not Allowed: " + session.getMethod());
        }
    }

    private boolean hasRangeHeader(IHTTPSession session) {
        String range = session.getHeaders().get("Range");
        if (range != null) {
            log.debug("We Have a Range Header {}", range);
            return true;
        }
        return false;
    }

    private InputStream getStreamVideo() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        inputStream = new DataSourceInputStream(dataSource, dataSource.getUri());
//        try {
//            inputStream = new FileInputStream(new File("/sdcard/Movies/TheBigBangTheory-TheFortificationImplementation-13289053-0.ts"));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        return inputStream;
    }

    public void setDataSource(DataSource dataSource) {
        log.debug("Setting a new DataSource");
        closeDataSource();
        this.dataSource = dataSource;
    }

    public void closeDataSource() {
        if (dataSource != null) {
            log.debug("Terminating old datasource");
            dataSource.close();
        }
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.dataSource = null;
    }

    public DataSource createDataSource(boolean pushMode, String uri) {
        if (pushMode) {
            dataSource = new PushBufferDataSource();
            dataSource.setUri(uri);
        } else {
            dataSource = new PullBufferDataSource();
            dataSource.setUri(uri);
        }
        return dataSource;
    }
}
