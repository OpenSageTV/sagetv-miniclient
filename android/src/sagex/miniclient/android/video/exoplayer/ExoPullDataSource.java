package sagex.miniclient.android.video.exoplayer;

import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.httpbridge.PullBufferDataSource;

/**
 * Created by seans on 10/12/15.
 */
public class ExoPullDataSource implements DataSource {
    static final Logger log = LoggerFactory.getLogger(ExoPullDataSource.class);
    static int sessions = 0;
    PullBufferDataSource dataSource = null;
    private long startPos;

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        dataSource = new PullBufferDataSource(sessions++);
        dataSource.setUri(dataSpec.uri.toString());
        long size = dataSource.open(dataSpec.uri.toString());
        this.startPos = dataSpec.position;
        log.debug("Open: {}, Offset: {}", dataSource.getUri(), startPos);
        return size;
    }

    @Override
    public void close() throws IOException {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if (dataSource == null) return 0;
//        if (log.isDebugEnabled())
//            log.debug("read: start:{}, offset:{}, len:{}", startPos, offset, readLength);
        return dataSource.read(startPos, buffer, offset, readLength);
    }
}
