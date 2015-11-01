package sagex.miniclient.httpbridge;

import com.Ostermiller.util.CircularByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import sagex.miniclient.MiniClient;

/**
 * Created by seans on 03/10/15.
 */
public class PullBufferDataSource implements DataSource {
    private static final Logger log = LoggerFactory.getLogger(PullBufferDataSource.class);

    private static final int MAX_BUFFER = 32768;
    private final int session;
    Socket remoteServer;
    byte buffer[] = new byte[MAX_BUFFER + 1];
    // pipes are used to hold and buffer the data
    CircularByteBuffer circularByteBuffer = null;
    InputStream in = null;
    OutputStream out = null;
    boolean opened = false;
    long size = -1;
    boolean verboseLogging = false;
    private String uri;
    private DataInputStream remoteReader;
    private OutputStream remoteWriter;
    private long bytesRead = 0; // how many bytes have we requested
    private long bytesPos = 0; // total byte position that we send to sagetv
    private long bytesAvailable = 0; // how many bytes are left unread in our byte queue

    public PullBufferDataSource(int session) {
        this.session = session;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public void setUri(String uri) {
        this.uri = uri;
    }

    public long open(String uri) throws IOException {
        if (opened) {
            throw new IOException("Attempting to re-open an OPENED datasource for uri " + uri);
        }
        log.debug("[{}]:Open(): {}", session, uri);
        try {
            String host = getHost(uri);
            this.uri = uri;
            bytesRead = 0;
            bytesPos = 0;
            bytesAvailable = 0;

            circularByteBuffer = new CircularByteBuffer(PushBufferDataSource.PIPE_SIZE);
            in = circularByteBuffer.getInputStream();
            out = circularByteBuffer.getOutputStream();

            remoteServer = new Socket();
            remoteServer.connect(new java.net.InetSocketAddress(host, 7818), 2000);
            this.remoteReader = new DataInputStream(remoteServer.getInputStream());
            this.remoteWriter = remoteServer.getOutputStream();

            sendStringCommandWithReply("OPEN " + getPath(uri));
            String strSize = sendStringCommandWithReply("SIZE");
            if (strSize != null) {
                try {
                    size = Long.parseLong(strSize.split(" ")[0]);
                } catch (Throwable t) {
                    log.error("[{}]:Failed to get Size", session, t);
                    size = -1;
                }
            }
            log.debug("SIZE got {} for {}", size, uri);
            opened = true;
        } catch (Throwable t) {
            log.error("[{}]:Unable to open: {}", session, uri, t);
        }

        return size;
    }

    String getPath(String uri) {
        if (uri == null) return null;
        int pos = uri.indexOf("/", "stv://".length());
        log.debug("[{}]:PATH: {}({})", session, uri, pos);
        return uri.substring(pos + 1);
    }

    String getHost(String uri) {
        if (uri == null) return null;
        int s = "stv://".length();
        int pos = uri.indexOf("/", s);
        return uri.substring(s, pos);
    }

    public void close() {
        log.debug("[{}]:Close()", session);
        try {
            if (remoteServer != null) {
                try {
                    sendStringCommandWithReply("CLOSE");
                } catch (Throwable t) {
                }
                remoteServer.close();

            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
        remoteServer = null;
        remoteReader = null;
        remoteWriter = null;

        try {
            circularByteBuffer.clear();
            if (in != null) in.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        try {
            if (out != null) out.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }

        buffer = null;
        circularByteBuffer = null;
        in = null;
        out = null;

        MiniClient.get().getHttpBridge().removeSession(session);

        opened = false;
    }

    @Override
    public void flush() {
        log.debug("[{}]:Flushing()", session);
        if (circularByteBuffer != null) {
            circularByteBuffer.clear();
        }
        size = 0;
        bytesPos = 0;
        bytesRead = 0;
        bytesAvailable = 0;
    }

    @Override
    public String getFileName() {
        if (uri == null) {
            return "file.mp4";
        } else {
            return new File(uri).getName();
        }
    }

    @Override
    public int bufferAvailable() {
        if (circularByteBuffer == null) return MAX_BUFFER;
        return circularByteBuffer.getSpaceLeft();
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public boolean isOpen() {
        return opened;
    }

    public int read(long readOffset, byte[] bytes, int offset, int len) throws IOException {
        if (!opened) {
            throw new IOException("read() called on DataSource that is not opened: " + uri);
        }

        if (verboseLogging && log.isDebugEnabled()) log.debug("[{}]:READ: len: {}", session, len);
        if (bytesAvailable < len) {
            fillBuffer(readOffset, MAX_BUFFER);
        }
        // reduce out buffer count
        bytesAvailable -= len;
        // increase the read count
        bytesRead += len;
        return in.read(bytes, offset, len);
    }

    private int fillBuffer(long readOffset, int len) throws IOException {
        String cmd = ("READ " + String.valueOf(readOffset + bytesPos) + " " + String.valueOf(len));
        if (verboseLogging && log.isDebugEnabled())
            log.debug("[{}]:fillBuffer(): {}", session, cmd);
        remoteWriter.write((cmd + "\r\n").getBytes());
        remoteWriter.flush();
        int bytes = readBuffer(len);
        if (bytes == -1) {
            throw new IOException("EOF for " + uri);
        }
        bytesPos += bytes;
        bytesAvailable += bytes; // add these bytes to queue
        out.write(buffer, 0, bytes);
        return bytes;
    }

    private int readBuffer(int len) throws IOException {
        int total = 0;
        int read = 0;
        while (total < len) {
            if (verboseLogging && log.isDebugEnabled())
                log.debug("total: {}, len: {}, delta: {}", total, len, (len - total));
            read = remoteReader.read(buffer, total, len - total);
            if (read == -1) {
                if (total == 0) {
                    log.warn("[{}]:End of File reached for {}", session, uri);
                    return -1;
                } else {
                    return total;
                }
            }
            total += read;
        }
        //log.debug("Filled buffer with {} bytes", total);
        return total;
    }

//    private void sendStringCommand(String cmd) throws IOException {
//        remoteWriter.write((cmd + "\r\n").getBytes());
//        remoteWriter.flush();
//        int read = readBuffer(4);
//        String val = null;
//        if (read > 0) {
//            val = new String(buffer, 0, read);
//        }
//        log.debug("Send Command: {}, Got Bytes {} Back with data[{}]", cmd, read, val);
//    }

    private String sendStringCommandWithReply(String cmd) throws IOException {
        remoteWriter.write((cmd + "\r\n").getBytes());
        remoteWriter.flush();
        byte buf[] = new byte[1024];
        int total = 0;
        while (true) {
            byte b = remoteReader.readByte();
            if (b == '\r') continue;
            if (b == '\n') break;
            buf[total++] = b;
        }
        String val = new String(buf, 0, total);
        log.debug("[{}]:Send Command: {}, Got Bytes {} Back with data [{}]", session, cmd, total, val);
        return val;
    }
}
