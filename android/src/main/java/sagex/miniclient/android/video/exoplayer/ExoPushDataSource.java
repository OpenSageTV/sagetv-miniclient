package sagex.miniclient.android.video.exoplayer;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.net.HasPushBuffer;
import sagex.miniclient.net.PushBufferDataSource;

/**
 * Created by seans on 08/12/15.
 */
public class ExoPushDataSource extends PushBufferDataSource implements DataSource, HasPushBuffer {
    private static final Logger log = LoggerFactory.getLogger(ExoPushDataSource.class);

    public ExoPushDataSource() {
        log.debug("ExoNative datasource being created.");
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        open(dataSpec.uri.toString());
        return C.LENGTH_UNBOUNDED;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        return read(0, buffer, offset, readLength);
    }
}
