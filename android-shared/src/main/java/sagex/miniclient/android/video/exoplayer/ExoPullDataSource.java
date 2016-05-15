package sagex.miniclient.android.video.exoplayer;

import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.net.BufferedPullDataSource;
import sagex.miniclient.net.HasClose;

/**
 * Created by seans on 10/12/15.
 */
public class ExoPullDataSource implements DataSource, HasClose {
    static final Logger log = LoggerFactory.getLogger(ExoPullDataSource.class);
    BufferedPullDataSource dataSource = null;
    private long startPos;

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        dataSource = new BufferedPullDataSource();
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
        int bytes = dataSource.read(startPos, buffer, offset, readLength);
        if (bytes == -1) return -1;
        startPos += bytes;
        return bytes;
    }
}
