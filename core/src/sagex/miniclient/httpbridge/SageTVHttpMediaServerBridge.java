package sagex.miniclient.httpbridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import fi.iki.elonen.NanoHTTPD;
import sagex.miniclient.MiniClient;

/**
 * The WebServer is a single instance, single connection server.  ie, a second connection will
 * terminate the first, and create a new stream for the media.
 * <p/>
 * Created by seans on 06/10/15.
 */
public class SageTVHttpMediaServerBridge extends NanoHTTPD implements DataSource.DataSourceListener {
    public static class MediaCommand {
        public static final class ActionType {
            public static final int Key=1;
            public static final int Command=2;
            public static final int Internal=3; // show keyboard, show mic, etc
        }

        public int type;
        public int key;
        public int modifiers;
        public String command;
    }

    public interface MediaCommandHandler {
        boolean onMediaCommand(MediaCommand command);
    }

    private static Logger log = LoggerFactory.getLogger(SageTVHttpMediaServerBridge.class);

    private final MiniClient client;

    private AtomicInteger sessions = new AtomicInteger();
    private Map<Integer, DataSourceInputStream> streams = new HashMap<Integer, DataSourceInputStream>();
    private DataSource currentDataSource;

    private MediaCommandHandler mediaCommandHandler = null;

    public SageTVHttpMediaServerBridge(MiniClient client, int port) {
        super(port);
        this.client = client;
        log.info("SageTV Http Brige Online using Port {}", port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        // handle remote media commands
        if (log.isDebugEnabled()) {
            log.debug("Request[{}]: {}", session.getUri(), session.getQueryParameterString());
        }
        try {
            if (session.getUri().contains("command")) {
                return processCommand(session);
            } else if (session.getUri().contains("stream")) {
                return processStreamRequest(session);
            }
        } catch (Throwable t) {
            log.error("Request Failed", t);
            return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Error: " + t.getMessage());
        }
        return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Unsupported Request: " + session.getUri());
    }

    private Response processStreamRequest(IHTTPSession session) {
        int sess = sessions.incrementAndGet();

        if (log.isDebugEnabled()) {
            log.debug("[{}]: Request[{}]: {}", sess, session.getMethod(), session.getUri());
            for (Map.Entry<String, String> h : session.getHeaders().entrySet()) {
                log.debug("[{}]  -- HEADER: {}='{}'", sess, h.getKey(), h.getValue());
            }
        }

        try {
            DataSource dataSource = null;
            // each request should contain a URL and a sessionid
            String url = session.getParms().get("url");
            // we need to create the datasource and stream
            if (url.startsWith("stv:")) {
                dataSource = new PullBufferDataSource(sess);
            } else {
                dataSource = new PushBufferDataSource(sess);
            }
            dataSource.setDataSourceListener(this);
            // cache it so we can quickly find it
            currentDataSource = dataSource;

            dataSource.setUri(url);
            dataSource.open(url);

            DataSourceInputStream dis = new DataSourceInputStream(dataSource, 0, sess); // set the offset later
            streams.put(sess, dis);

            if (session.getMethod() == Method.GET) {
                if (dataSource instanceof PushBufferDataSource) {
                    // use chunked for PushBuffer since we don't know the size
                    Response resp = NanoHTTPD.newChunkedResponse(Response.Status.OK, "video/mp2t", dis);
                    return resp;
                } else {
                    // it's a pull
                    long ranges[] = getRange(session);
                    log.debug("[{}]:PullBufferDataSource: Setting Range: {}-{}", sess, ranges[0], ranges[1]);
                    dis.setReadOffset(ranges[0]);
                    Response resp = NanoHTTPD.newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, "video/mp4", dis, dis.getDataSource().size() - ranges[0]);
                    resp.addHeader("Accept-Ranges", "bytes");
                    return resp;
                }
            } else {
                log.error("[" + sess + "]:Invalid Method: " + session.getMethod(), new Exception("METHOD NOT SUPPORTED: " + session.getMethod()));
                return NanoHTTPD.newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method Not Allowed: " + session.getMethod());
            }
        } catch (Throwable t) {
            log.error("[" + sess + "]:Request Failed", t);
            return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Error: " + t.getMessage());
        }
    }

    private Response processCommand(IHTTPSession session) {
        if (mediaCommandHandler != null) {
            MediaCommand command = new MediaCommand();
            command.type = Integer.parseInt(session.getParms().get("type"));
            command.key = Integer.parseInt(session.getParms().get("key"));
            command.command = session.getParms().get("cmd");
            command.modifiers = Integer.parseInt(session.getParms().get("modifiers"));
            if (mediaCommandHandler.onMediaCommand(command)) {
                return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "text/plain", "Command Handled");
            }
        } else {
            log.debug("Attempted to handle a command, but no command handler is set");
        }
        return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Command Not Found " + session.getUri());
    }

    long[] getRange(IHTTPSession session) {
        long ret[] = new long[2];
        String range = session.getHeaders().get("range");
        if (range != null) {
            log.debug("[{}]:We Have a Range Header {}", sessions.get(), range);
            String bytes = range.split("=")[1];
            String parts[] = bytes.split("-");
            if (parts[0].trim().length() == 0) {
                ret[0] = 0;
            } else {
                ret[0] = Long.parseLong(parts[0]);
            }
            if (parts.length > 1) {
                if (parts[1].trim().length() == 0) {
                    ret[1] = 0;
                } else {
                    ret[1] = Long.parseLong(parts[1]);
                }
            }
        }
        return ret;
    }

    public synchronized void closeSessions() {
        log.debug("[{}]:Closing {} Sessions", sessions.get(), streams.size());
        for (DataSourceInputStream dis : streams.values()) {
            try {
                dis.close();
            } catch (IOException e) {
                log.debug("[{}]:Failed to close session", sessions.get(), e);
            }
        }
        streams.clear();
        currentDataSource = null;
    }

    public String getVideoURI(String sageUri) {
        String bridgeUrl = "http://localhost:9991/stream";
        if (sageUri.startsWith("stv:")) {
            bridgeUrl += "/pull";
        } else {
            bridgeUrl += "/push";
        }
        bridgeUrl += "?url=";
        try {
            bridgeUrl += (URLEncoder.encode(sageUri, "UTF-8"));
        } catch (Throwable t) {
            bridgeUrl += (URLEncoder.encode(sageUri));
        }
        return bridgeUrl;
    }

    public DataSource getCurrentDataSource() {
        if (currentDataSource == null) {
            log.warn("Something is accessing the datasource, but we don't have, let's wait for 1 second");
        }
        long et = System.currentTimeMillis() + 1000;
        while (currentDataSource == null && System.currentTimeMillis() < et) {
            try {
                log.debug("Waiting for datasource...");
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.interrupted();
            }
        }
        if (currentDataSource == null) {
            throw new RuntimeException("datasource was null... something went wrong");
        }
        return currentDataSource;
    }

    public boolean hasDataSource() {
        return currentDataSource != null;
    }

    public void removeSession(int session) {
        log.debug("Removing HTTP Session {}", session);
        streams.remove(session);
    }

    @Override
    public void onOpen(DataSource source) {
        // don't care
    }

    @Override
    public void onClose(DataSource source) {
        removeSession(source.getSession());
        // stop listening
        source.setDataSourceListener(null);
    }

    public void setMediaCommandHandler(MediaCommandHandler mediaCommandHandler) {
        this.mediaCommandHandler = mediaCommandHandler;
    }

}
