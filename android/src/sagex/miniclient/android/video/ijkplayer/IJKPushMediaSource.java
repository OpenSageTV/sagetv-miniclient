package sagex.miniclient.android.video.ijkplayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.net.HasPushBuffer;
import sagex.miniclient.net.PushBufferDataSource;
import sagex.miniclient.util.VerboseLogging;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * Created by seans on 20/12/15.
 */
public class IJKPushMediaSource implements IMediaDataSource, HasPushBuffer {
    private static final Logger log = LoggerFactory.getLogger(IJKPushMediaSource.class);

    private PushBufferDataSource dataSource;
    private String url;
    boolean released = false;

    public IJKPushMediaSource() {
    }

    public void open(String url) throws IOException {
        this.url = url;
    }

    private void _open() throws IOException {
        if (dataSource != null) {
            // we are already opened
            return;
        }
        dataSource = new PushBufferDataSource();
        dataSource.open(url);
    }

    @Override
    public int readAt(long position, byte[] bytes, int offset, int size) throws IOException {
        if (VerboseLogging.DATASOURCE_LOGGING)
            log.debug("readAt(): pos: {}, offset:{}, size: {}", position, offset, size);

        // ijkmediasource does a zero len read on a seek to see if it was successful
        // we'll return 0 (ok) so that the player buffers get cleaned up, and the the player
        // can start reading from the new location.
        if (size == 0) return 0;

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
    }

    @Override
    public void release() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataSource.release();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        released = true;
        dataSource = null;
    }

    @Override
    public void setEOS() {
        if (dataSource != null) {
            dataSource.setEOS();
        }
    }

    @Override
    public int bufferAvailable() {
        if (dataSource != null) {
            dataSource.bufferAvailable();
        }
        return PushBufferDataSource.PIPE_SIZE;
    }

    @Override
    public void pushBytes(byte[] bytes, int offset, int len) throws IOException {
        if (released) {
            log.info("DataSource is released, so ignoring push.");
            return;
        }
        while (dataSource == null) {
            // wait for the datasource
            try {
                Thread.sleep(50);
                if (VerboseLogging.DATASOURCE_LOGGING) log.warn("Waiting for datasource...");
            } catch (InterruptedException e) {
                Thread.interrupted();
                return;
            }
        }
        dataSource.pushBytes(bytes, offset, len);
    }

    @Override
    public void flush() {
        if (dataSource != null) {
            dataSource.flush();
        }
    }

    @Override
    public long getBytesRead() {
        if (dataSource != null) {
            return dataSource.getBytesRead();
        }
        return 0;
    }
}
