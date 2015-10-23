package sagex.miniclient.httpbridge;

import com.Ostermiller.util.CircularByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * used push:// is used, this is the media data source that feeds the video player
 */
public class PushBufferDataSource implements DataSource {
    public static final int PIPE_SIZE = 16 * 1024 * 1024;
    private static final Logger log = LoggerFactory.getLogger(PushBufferDataSource.class);

    CircularByteBuffer circularByteBuffer = null;

    InputStream in = null;
    OutputStream out = null;
    private String uri;
    private long bytesRead = 0;

    public PushBufferDataSource() {
        circularByteBuffer = new CircularByteBuffer(PIPE_SIZE);
        in = circularByteBuffer.getInputStream();
        out = circularByteBuffer.getOutputStream();
    }

    @Override
    public long open(String uri) throws IOException {
        this.uri = uri;
        log.debug("Open Called: {}", uri);
        bytesRead = 0;
        return -1;
    }

    @Override
    public void close() {
        log.debug("Close on PushBufferDataSource was called", new Exception("**pushbuffer closed**"));
        try {
            circularByteBuffer.clear();
            out.close();
        } catch (Throwable t) {
        }

        try {
            in.close();
        } catch (Throwable t) {
        }

        in = null;
        out = null;
    }

    @Override
    public void flush() {
        bytesRead = 0;
        circularByteBuffer.clear();
    }

    @Override
    public String getFileName() {
        return "file.ts";
    }

    @Override
    public int bufferAvailable() {
        return circularByteBuffer.getSpaceLeft();
    }

    @Override
    public int read(long streamOffset, byte[] bytes, int offset, int len) throws IOException {
        // streamOffset is not used for push
        //log.debug("[{}]PB READ: {}", Thread.currentThread().getName(), len, new Exception());
        int read = in.read(bytes, offset, len);
        if (read >= 0) {
            bytesRead += read;
        }
        return read;
    }

    public void pushBytes(byte[] bytes, int offset, int len) throws IOException {
        //log.debug("PB PUSH: {}", len);
        out.write(bytes, offset, len);
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getBytesRead() {
        return bytesRead;
    }
}
