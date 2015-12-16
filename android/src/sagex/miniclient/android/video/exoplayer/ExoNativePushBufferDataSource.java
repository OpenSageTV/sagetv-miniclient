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

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        read = 0;
        bb = new CircularByteBuffer(BUFFER_SIZE);
        log.debug("Opening ExoPushDataSource {}", dataSpec.uri);
        is = bb.getInputStream();
        os = bb.getOutputStream();
        return C.LENGTH_UNBOUNDED;
    }

    @Override
    public void close() throws IOException {
        bb.clear();
        try {
            is.close();
        } catch (Throwable t) {
        }
        try {
            os.close();
        } catch (Throwable t) {
        }

    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        read += readLength;
        log.debug("read: offset:{}, len:{}, total:{}", offset, readLength, readLength);
        return is.read(buffer, offset, readLength);
    }

    public long getBytesRead() {
        return read;
    }

    public void pushBytes(byte[] cmddata, int bufDataOffset, int buffSize) throws IOException {
        while (os == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {

            }
        }
        log.debug("push: offset:{}, len:{}", bufDataOffset, buffSize);
        os.write(cmddata, bufDataOffset, buffSize);
    }

    public void flush() {
        if (bb == null) return;
        bb.clear();
    }

    public int bufferAvailable() {
        if (bb == null) return BUFFER_SIZE;
        return bb.getSpaceLeft();
    }
}
