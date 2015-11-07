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
    private final int session;

    CircularByteBuffer circularByteBuffer = null;

    InputStream in = null;
    OutputStream out = null;
    private String uri;
    private long bytesRead = 0;
    private boolean opened = false;

    private boolean verboseLogging = false;

    public PushBufferDataSource(int session) {
        this.session = session;
        circularByteBuffer = new CircularByteBuffer(PIPE_SIZE);
        in = circularByteBuffer.getInputStream();
        out = circularByteBuffer.getOutputStream();
    }

    @Override
    public long open(String uri) throws IOException {
        // push:f=MPEG2-TS;dur=1851466;br=2500000;
        // [bf=vid;f=H.264;index=0;main=yes;tag=1011;fps=59.94006;fpsn=60000;fpsd=1001;ar=1.777778;arn=16;ard=9;w=1280;h=720;]
        // [bf=aud;f=AAC;index=1;main=yes;tag=1100;sr=48000;ch=2;at=ADTS-MPEG2;]
        this.uri = uri;
        log.debug("[{}]:Open Called: {}", session, uri);
        bytesRead = 0;
        opened = true;
        return -1;
    }

    @Override
    public void close() {
        log.debug("[{}]: Close on PushBufferDataSource was called", session);
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
        opened = false;
        circularByteBuffer = null;
    }

    @Override
    public void flush() {
        log.debug("[{}]:FLUSH()", session, new Exception("FLUSH"));
        bytesRead = 0;
        circularByteBuffer.clear();
//
//        circularByteBuffer = new CircularByteBuffer(PIPE_SIZE);
//        in = circularByteBuffer.getInputStream();
//        out = circularByteBuffer.getOutputStream();
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
    public long size() {
        return -1;
    }

    @Override
    public boolean isOpen() {
        return opened;
    }

    @Override
    public int read(long readOffset, byte[] bytes, int offset, int len) throws IOException {
        if (!opened) {
            throw new IOException("read() called on DataSource that is not opened: " + uri);
        }
        if (in == null) return 0;
        // streamOffset is not used for push
        if (verboseLogging && log.isDebugEnabled()) log.debug("[{}]:READ: {}", session, len);

        int read = in.read(bytes, offset, len);
        if (read >= 0) {
            bytesRead += read;
        }
        return read;
    }

    public void pushBytes(byte[] bytes, int offset, int len) throws IOException {
        if (verboseLogging && log.isDebugEnabled()) log.debug("[{}]:PUSH: {}", session, len);
        if (out == null) {
            log.warn("PUSH: We are missing this PUSH because our DataSource is closed.");
            return;
        }

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
