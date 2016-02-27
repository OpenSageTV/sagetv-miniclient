package sagex.miniclient.android.video.ijkplayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.net.ISageTVDataSource;
import sagex.miniclient.net.SimplePullDataSource;
import sagex.miniclient.util.VerboseLogging;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * Created by seans on 20/12/15.
 */
public class IJKPullMediaSource implements IMediaDataSource {
    private static final Logger log = LoggerFactory.getLogger(IJKPullMediaSource.class);

    private ISageTVDataSource dataSource;
    private String url;

    public IJKPullMediaSource() {
    }

    public void open(String url) throws IOException {
        this.url = url;
    }

    private void _open() throws IOException {
        if (dataSource != null) return;
        dataSource = new SimplePullDataSource();
        dataSource.open(url);
    }

    @Override
    public int readAt(long position, byte[] bytes, int offset, int size) throws IOException {
        if (VerboseLogging.DATASOURCE_LOGGING)
            log.debug("readAt(): pos: {}, offset:{}, size: {}", position, offset, size);
        try {
            if (dataSource == null) _open();
            return dataSource.read(position, bytes, offset, size);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    @Override
    public long getSize() throws IOException {
        if (dataSource == null) _open();
        return dataSource.size();
    }

    @Override
    public void close() throws IOException {
        if (dataSource != null) {
            dataSource.close();
        }
        dataSource = null;
    }
}
