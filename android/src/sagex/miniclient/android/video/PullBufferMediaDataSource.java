package sagex.miniclient.android.video;

import android.net.Uri;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.upstream.UriDataSource;

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
public class PullBufferMediaDataSource implements UriDataSource {
    private static final Logger log = LoggerFactory.getLogger(PullBufferMediaDataSource.class);

    private static final int MAX_BUFFER = 32768;
    Socket remoteServer;
    byte buffer[] = new byte[MAX_BUFFER];
    private Uri uri;
    private DataSpec dataSpec;

    // pipes are used to hold and buffer the data
    private PipedOutputStream provider;
    private PipedInputStream consumer;

    private InputStream remoteReader;
    private OutputStream remoteWriter;
    private long bytesRead = 0; // how many bytes have we requested
    private long bytesPos = 0; // total byte position that we send to sagetv
    private long bytesAvailable = 0; // how many bytes are left unread in our byte queue

    public PullBufferMediaDataSource(Uri uri) {
        this.uri = uri;
    }

    @Override
    public String getUri() {
        return uri.toString();
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        log.debug("Open(): {}, {}", uri, dataSpec);
        this.dataSpec = dataSpec;
        bytesRead = 0;
        bytesPos = 0;
        bytesAvailable = 0;
        consumer = new PipedInputStream(MAX_BUFFER);
        provider = new PipedOutputStream();
        consumer.connect(provider);

        remoteServer = new java.net.Socket();
        remoteServer.connect(new java.net.InetSocketAddress(uri.getHost(), 7818), 2000);
        this.remoteReader = remoteServer.getInputStream();
        this.remoteWriter = remoteServer.getOutputStream();

        sendStringCommand("OPEN " + uri.getPath());

        return C.LENGTH_UNBOUNDED;
    }

    @Override
    public void close() {
        log.debug("Close()");
        try {
            remoteServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            provider.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            consumer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int read(byte[] bytes, int offset, int len) throws IOException {
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
        log.debug("fillBuffer(): {}", cmd);
        remoteWriter.write((cmd + "\r\n").getBytes());
        remoteWriter.flush();
        int bytes = readBuffer(len);
        bytesPos += bytes;
        bytesAvailable += bytes; // add these bytes to queue
        provider.write(buffer, 0, bytes);
        return bytes;
    }

    private int readBuffer(int len) throws IOException {
        int total = 0;
        int read = 0;
        while (total < len) {
            read = remoteReader.read(buffer, total, len - total);
            total += read;
        }
        log.debug("Filled buffer with {} bytes", total);
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
