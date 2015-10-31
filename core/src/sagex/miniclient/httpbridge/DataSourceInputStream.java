package sagex.miniclient.httpbridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by seans on 06/10/15.
 */
public class DataSourceInputStream extends InputStream {
    private static final Logger log = LoggerFactory.getLogger(DataSourceInputStream.class);
    // buffer for when a single byte read is called
    byte singleByteArray[] = new byte[1];
    DataSource dataSource = null;
    long readOffset = 0;
    int session = 0;

    public DataSourceInputStream(DataSource source, long readOffset, int session) {
        log.debug("[{}]:DataSourceInputStream()[{}]: {}", session, readOffset, source.getUri());
        this.readOffset = readOffset;
        this.dataSource = source;
        this.session = session;
    }

    @Override
    public int read() throws IOException {
        int length = read(singleByteArray);
        if (length == -1) {
            return -1;
        }
        return singleByteArray[0] & 0xFF;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        try {
            return dataSource.read(readOffset, buffer, offset, length);
        } catch (Throwable t) {
            log.error("READ FAILED", t);
            throw new IOException(t);
        }
    }

    @Override
    public long skip(long byteCount) throws IOException {
        return super.skip(byteCount);
    }

    @Override
    public void close() throws IOException {
        log.debug("[{}]DataSourceInputStream.Close()", session);
        dataSource.close();
    }

    public void setReadOffset(long readOffset) {
        this.readOffset = readOffset;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
