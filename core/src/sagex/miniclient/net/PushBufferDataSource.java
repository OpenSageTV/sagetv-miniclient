package sagex.miniclient.net;

import com.Ostermiller.util.CircularByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sagex.miniclient.util.DataCollector;
import sagex.miniclient.util.VerboseLogging;

/**
 * used push:// is used, this is the media data source that feeds the video player
 */
public class PushBufferDataSource implements ISageTVDataSource, HasPushBuffer {
    public enum State {Idle, Opened, Closed}
    public static final int PIPE_SIZE = 16 * 1024 * 1024;
    private static final Logger log = LoggerFactory.getLogger(PushBufferDataSource.class);

    CircularByteBuffer circularByteBuffer = null;

    InputStream in = null;
    OutputStream out = null;
    String uri;
    long bytesRead = 0;
    boolean eos = false;

    State state = State.Idle;

    DataCollector dataCollector = null;

    public PushBufferDataSource() {
    }

    @Override
    public long open(String uri) throws IOException {
        // push:f=MPEG2-TS;dur=1851466;br=2500000;
        // [bf=vid;f=H.264;index=0;main=yes;tag=1011;fps=59.94006;fpsn=60000;fpsd=1001;ar=1.777778;arn=16;ard=9;w=1280;h=720;]
        // [bf=aud;f=AAC;index=1;main=yes;tag=1100;sr=48000;ch=2;at=ADTS-MPEG2;]
        if (state == State.Opened) {
            log.warn("opened called on an already opened push buffer, will ignore");
            return -1;
        }
        state = State.Idle;
        eos = false;

        this.uri = uri;
        log.debug("Open Called: {}", uri);
        bytesRead = 0;
        if (circularByteBuffer != null) {
            circularByteBuffer.clear();
        } else {
            circularByteBuffer = new CircularByteBuffer(PIPE_SIZE);
        }
        in = circularByteBuffer.getInputStream();
        out = circularByteBuffer.getOutputStream();
        if (VerboseLogging.LOG_DATASOURCE_BYTES_TO_FILE) {
            log.warn("DataCollector is enabled");
            dataCollector = new DataCollector();
        }
        state = State.Opened;
        if (dataCollector != null) {
            dataCollector.open();
        }
        return -1;
    }

    public void close() {
        log.debug("close() for a push is ignored, until the release() is called.");
    }

    @Override
    public void release() {
        log.debug("Release on PushBufferDataSource was called");
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
        state = State.Closed;
        circularByteBuffer = null;
        eos = true;
        if (dataCollector != null) {
            dataCollector.close();
        }
    }

    @Override
    public void setEOS() {
        eos = true;
        if (in != null) {
            try {
                if (in.available() == 0) {
                    log.debug("setEOS() called and there is no data, so closing the stream");
                    in.close();
                } else {
                    log.debug("setEOS() called and we still have {} bytes of data", in.available());
                }
            } catch (Throwable t) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void flush() {
        log.debug("FLUSH()");
        bytesRead = 0;
        circularByteBuffer.clear();
    }

    @Override
    public int bufferAvailable() {
        if (circularByteBuffer == null) return 0;
        return circularByteBuffer.getSpaceLeft();
    }

    @Override
    public long size() {
        return -1;
    }

    public boolean isOpen() {
        return state == State.Opened;
    }

    @Override
    public int read(long readOffset, byte[] bytes, int offset, int len) throws IOException {
        if (state != State.Opened) {
            throw new IOException("read() called on DataSource that is not opened: " + uri);
        }
        if (in == null) return 0;
        // streamOffset is not used for push
        if (VerboseLogging.DATASOURCE_LOGGING && log.isDebugEnabled()) log.debug("READ: {}", len);
        if (eos && in.available() <= 0) return -1; // EOS - no data left
        int read = in.read(bytes, offset, Math.min(len, in.available())); // never read more than what we have
        if (read >= 0) {
            bytesRead += read;
        }
        return read;
    }

    @Override
    public void pushBytes(byte[] bytes, int offset, int len) throws IOException {
        if (VerboseLogging.DATASOURCE_LOGGING && log.isDebugEnabled()) log.debug("PUSH: {}", len);
        while (out == null) {
            if (state == State.Closed) return;
            log.warn("PUSH: We are waiting this PUSH because our DataSource is not yet opened.");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (len > 0) {
            if (eos) {
                log.warn("We are getting data, even after EOS has been set");
                return;
            }
            out.write(bytes, offset, len);
            if (dataCollector != null) {
                dataCollector.write(bytes, offset, len);
            }
        }
    }

    @Override
    public long getBytesRead() {
        return bytesRead;
    }
}
