package sagex.miniclient.android.video.exoplayer;

import com.Ostermiller.util.CircularByteBuffer;
import com.google.android.exoplayer.C;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sagex.miniclient.util.DataCollector;

/**
 * Created by seans on 08/12/15.
 */
public class ExoNativePushBufferDataSource implements DataSource {
    static int BUFFER_SIZE = 1024 * 1024 * 16;
    private static final Logger log = LoggerFactory.getLogger(ExoNativePushBufferDataSource.class);

    CircularByteBuffer bb = null;
    InputStream is;
    OutputStream os;
    int read = 0;
    boolean closed = false;

    DataCollector dataCollector = null;

    public ExoNativePushBufferDataSource() {
        log.debug("ExoNative datasource being created.", new Exception("** DATASOURCE CREATED **"));
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        if (bb != null) {
            log.debug("Open Called Again on Push DataSource, ignoring.  {}, {}", dataSpec.uri, dataSpec.position);
            return C.LENGTH_UNBOUNDED;
        }
        read = 0;
        bb = new CircularByteBuffer(BUFFER_SIZE);
        log.debug("Opening ExoPushDataSource {}, offset: {}", dataSpec.uri, dataSpec.position);
        is = bb.getInputStream();
        os = bb.getOutputStream();
        boolean dataCollectorEnabled = false;
        if (dataCollectorEnabled) {
            dataCollector = new DataCollector("Exo");
            dataCollector.open();
        }

        return C.LENGTH_UNBOUNDED;
    }

    @Override
    public void close() throws IOException {
        // do nothing in this implementation of the close, since, we'll close it int he player
        log.debug("DataSource is being closed, but we'll ignore it, for now and wait for the release()");
    }

    public void release() {
        log.debug("Data Source is being closed", new Exception("DataSource is being closed"));
        closed = true;
        bb.clear();
        try {
            is.close();
        } catch (Throwable t) {
        }
        try {
            os.close();
        } catch (Throwable t) {
        }
        bb = null;
        os = null;
        is = null;
        if (dataCollector != null) {
            dataCollector.close();
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        read += readLength;
//        if ((read/(1024*1024) % 5) == 0) {
//            log.debug("read tick (every 5mb): offset:{}, len:{}, total:{}", offset, readLength, readLength);
//        }
        return is.read(buffer, offset, readLength);
    }

    public long getBytesRead() {
        return read;
    }

    public void pushBytes(byte[] cmddata, int bufDataOffset, int buffSize) throws IOException {
        while (os == null && !closed) {
            try {
                Thread.sleep(200);
                log.debug("Waiting for datasource...");
            } catch (InterruptedException e) {
                return;
            }
        }
        if (closed) return;
//        log.debug("push: offset:{}, len:{}", bufDataOffset, buffSize);
        os.write(cmddata, bufDataOffset, buffSize);
        if (dataCollector != null) {
            dataCollector.write(cmddata, bufDataOffset, buffSize);
        }
    }

    public void flush() {
        if (bb == null) return;
        log.debug("FLUSH: is called on the DataSource");
        bb.clear();
    }

    public int bufferAvailable() {
        if (bb == null) return BUFFER_SIZE;
        return bb.getSpaceLeft();
    }
}
