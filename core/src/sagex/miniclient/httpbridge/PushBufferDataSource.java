package sagex.miniclient.httpbridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * used push:// is used, this is the media data source that feeds the video player
 */
public class PushBufferDataSource implements DataSource {
    private static final int BUFFER_SIZE = 65535;
    private static final Logger log = LoggerFactory.getLogger(PushBufferDataSource.class);
    private PipedOutputStream provider;
    private PipedInputStream consumer;
    private String uri;

    private Thread readThread;

    public PushBufferDataSource() {
        consumer = new PipedInputStream(BUFFER_SIZE);
        provider = new PipedOutputStream();
        try {
            provider.connect(consumer);
            //consumer.connect(provider);
        } catch (IOException e) {
            throw new RuntimeException("Could not create PushBufferDataSource");
        }
    }

    @Override
    public long open(String uri) throws IOException {
        this.uri = uri;
        log.debug("Open Called: {}", uri);
        return -1;
    }

    @Override
    public void close() {
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
    public int read(long streamOffset, byte[] bytes, int offset, int len) throws IOException {
        // streamOffset is not used for push
        //log.debug("[{}]PB READ: {}", Thread.currentThread().getName(), len, new Exception());
        if (consumer == null) {
            log.warn("consumer is not connected");
            return -1;
        } else {
            return consumer.read(bytes, offset, len);
        }
    }

    public void pushBytes(byte[] bytes, int offset, int len) throws IOException {
        //log.debug("PB PUSH: {}", len);
//        if (log.isDebugEnabled()) {
//            log.debug("pushBytes: offset: {}, len: {}, byteSize: {}", offset, len, bytes.length);
//        }
        if (provider != null) {
            provider.write(bytes, offset, len);
        } else {
            log.warn("provider is not connected");
        }
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public void setUri(String uri) {
        this.uri = uri;
    }
}
