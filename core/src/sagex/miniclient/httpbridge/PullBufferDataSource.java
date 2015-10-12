package sagex.miniclient.httpbridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;

/**
 * Created by seans on 03/10/15.
 */
public class PullBufferDataSource implements DataSource {
    private static final Logger log = LoggerFactory.getLogger(PullBufferDataSource.class);

    private static final int MAX_BUFFER = 32768;
    Socket remoteServer;
    byte buffer[] = new byte[MAX_BUFFER + 1];
    private String uri;

    // pipes are used to hold and buffer the data
    private PipedOutputStream provider;
    private PipedInputStream consumer;

    private InputStream remoteReader;
    private OutputStream remoteWriter;
    private long bytesRead = 0; // how many bytes have we requested
    private long bytesPos = 0; // total byte position that we send to sagetv
    private long bytesAvailable = 0; // how many bytes are left unread in our byte queue

    public PullBufferDataSource() {
    }

    public String getUri() {
        return uri;
    }

    @Override
    public void setUri(String uri) {
        this.uri = uri;
    }

    public long open(String uri) throws IOException {
        log.debug("Open(): {}", uri);
        try {
            String host = getHost(uri);
            this.uri = uri;
            bytesRead = 0;
            bytesPos = 0;
            bytesAvailable = 0;
            consumer = new PipedInputStream(MAX_BUFFER);
            provider = new PipedOutputStream();
            consumer.connect(provider);

            remoteServer = new Socket();
            remoteServer.connect(new java.net.InetSocketAddress(host, 7818), 2000);
            this.remoteReader = remoteServer.getInputStream();
            this.remoteWriter = remoteServer.getOutputStream();

            sendStringCommand("OPEN " + getPath(uri));
        } catch (Throwable t) {
            log.error("Unable to open: {}", uri, t);
        }
        return -1;
    }

    private String getPath(String uri) {
        if (uri == null) return null;
        String parts[] = uri.split("//");
        log.debug("PATH: {}({})", uri, parts);
        return "/" + parts[2];
    }

    private String getHost(String uri) {
        if (uri == null) return null;
        String parts[] = uri.split("/");
        log.debug("HOST: {}({})", uri, parts);
        return parts[2];
    }

    public void close() {
        log.debug("Close() from", new Exception("Pull is being closed"));
        try {
            if (remoteServer != null) remoteServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (provider != null) provider.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (consumer != null) consumer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int read(long streamOffset, byte[] bytes, int offset, int len) throws IOException {
        //log.debug("READ: offset: {}, len: {}, bufferSize: {}", offset, len, bytes.length);
        if (bytesAvailable < len) {
            fillBuffer(MAX_BUFFER);
        }
        // reduce out buffer count
        bytesAvailable -= len;
        // increase the read count
        bytesRead += len;
        return consumer.read(bytes, offset, len);
    }

    private int fillBuffer(int len) throws IOException {
        String cmd = ("READ " + String.valueOf(bytesPos) + " " + String.valueOf(len));
        //log.debug("fillBuffer(): {}", cmd);
        remoteWriter.write((cmd + "\r\n").getBytes());
        remoteWriter.flush();
        int bytes = readBuffer(len);
        if (bytes == -1) {
            throw new IOException("EOF for " + uri);
        }
        bytesPos += bytes;
        bytesAvailable += bytes; // add these bytes to queue
        provider.write(buffer, 0, bytes);
        return bytes;
    }

    private int readBuffer(int len) throws IOException {
        int total = 0;
        int read = 0;
        while (total < len) {
            //log.debug("total: {}, len: {}, delta: {}", total, len, (len-total));
            read = remoteReader.read(buffer, total, len - total);
            if (read == -1) {
                if (total == 0) {
                    log.warn("End of File reached for {}", uri);
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

    private void sendStringCommand(String cmd) throws IOException {
        remoteWriter.write((cmd + "\r\n").getBytes());
        remoteWriter.flush();
        int read = readBuffer(4);
        String val = null;
        if (read > 0) {
            val = new String(buffer, 0, read);
        }
        log.debug("Send Command: {}, Got Bytes {} Back with data[{}]", cmd, read, val);
    }
}
