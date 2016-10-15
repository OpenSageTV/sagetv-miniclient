package sagex.miniclient.android.video.exoplayer2;

import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import sagex.miniclient.net.HasClose;
import sagex.miniclient.net.HasPushBuffer;
import sagex.miniclient.net.PushBufferDataSource;

/**
 * Created by seans on 08/12/15.
 */
public class Exo2PushDataSource extends PushBufferDataSource implements DataSource, HasPushBuffer, HasClose {
    private static final Logger log = LoggerFactory.getLogger(Exo2PushDataSource.class);
    private Uri uri;

    public Exo2PushDataSource() {
        log.debug("ExoNative datasource being created.");
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        this.uri=dataSpec.uri;
        open(dataSpec.uri.toString());
        return C.LENGTH_UNSET;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        return read(0, buffer, offset, readLength);
    }

    @Override
    public Uri getUri() {
        return uri;
    }
}
