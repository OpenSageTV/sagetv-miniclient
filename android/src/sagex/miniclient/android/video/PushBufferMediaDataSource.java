package sagex.miniclient.android.video;

import android.net.Uri;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.upstream.UriDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * used push:// is used, this is the media data source that feeds the video player
 */
public class PushBufferMediaDataSource implements UriDataSource {
    private static final Logger log = LoggerFactory.getLogger(PushBufferMediaDataSource.class);
    private Uri uri;
    private PipedOutputStream provider;
    private PipedInputStream consumer;

    public PushBufferMediaDataSource(Uri uri) {
        log.debug("PushBuffer created using uri: {}", uri);
        this.uri = uri;
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        log.debug("Open Called");
        if (provider != null) {
            log.debug("Connection is open, closing existing pipes");
            close();
        }
        consumer = new PipedInputStream(32768);
        provider = new PipedOutputStream();
        try {
            consumer.connect(provider);
        } catch (IOException e) {
            throw new RuntimeException("Could not create PushBufferDataSource");
        }
        return C.LENGTH_UNBOUNDED;
    }

    @Override
    public void close() throws IOException {
        log.debug("Close on PushBufferDataSource was called", new Exception("**pushbuffer closed**"));
        try {
            provider.close();
            provider = null;
        } catch (Throwable t) {
        }

        try {
            consumer.close();
            consumer = null;
        } catch (Throwable t) {
        }

        consumer = null;
        provider = null;
    }

    @Override
    public int read(byte[] bytes, int offset, int len) throws IOException {
        if (consumer == null) {
            log.warn("consumer is not connected");
            return -1;
        } else {
            return consumer.read(bytes, offset, len);
        }
    }

    public void pushBytes(byte[] bytes, int offset, int len) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("pushBytes: offset: {}, len: {}, byteSize: {}", offset, len, bytes.length);
        }
        if (provider != null) {
            provider.write(bytes, offset, len);
        } else {
            log.warn("provider is not connected");
        }
    }

    @Override
    public String getUri() {
        return (uri != null ? uri.toString() : null);
    }
}
