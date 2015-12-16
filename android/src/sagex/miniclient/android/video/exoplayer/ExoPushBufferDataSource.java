package sagex.miniclient.android.video.exoplayer;

import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.httpbridge.PushBufferDataSource;

/**
 * Created by seans on 08/12/15.
 */
public class ExoPushBufferDataSource implements DataSource {
    private static final Logger log = LoggerFactory.getLogger(ExoPushBufferDataSource.class);

    static int sessions = 0;

    PushBufferDataSource pushSource = null;

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        pushSource = new PushBufferDataSource(sessions++);
        pushSource.setUri(dataSpec.uri.toString());
        pushSource.open(dataSpec.uri.toString());
        log.debug("Opening ExoPushDataSource {}", dataSpec.uri);
        //return C.LENGTH_UNBOUNDED;
        return 1750740468L;
    }

    @Override
    public void close() throws IOException {
        if (pushSource != null) {
            pushSource.close();
        }
        pushSource = null;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if (pushSource == null) return 0;
        if (log.isDebugEnabled()) log.debug("read() offset:{} len:{}", offset, readLength);
        return pushSource.read(0, buffer, offset, readLength);
    }

    public long getBytesRead() {
        if (pushSource == null)
            return 0;
        return pushSource.getBytesRead();
    }

    public void pushBytes(byte[] cmddata, int bufDataOffset, int buffSize) throws IOException {
        if (pushSource == null) return;
        pushSource.pushBytes(cmddata, bufDataOffset, bufDataOffset);
    }

    public void flush() {
        if (pushSource == null) return;
        pushSource.flush();
    }

    public int bufferAvailable() {
        if (pushSource == null) return PushBufferDataSource.PIPE_SIZE;
        return pushSource.bufferAvailable();
    }
}
