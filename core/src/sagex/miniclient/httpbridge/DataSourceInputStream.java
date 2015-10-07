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
    byte singleByteArray[] = new byte[1];
    DataSource dataSource = null;
    private boolean closed = false;
    private boolean opened = false;
    private String uri;

    public DataSourceInputStream(DataSource source, String uri) {
        log.debug("Opened(): {}", uri);
        this.dataSource = source;
        this.uri = uri;
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
        checkOpened();
        //log.debug("Read(): {}, {}", offset, length);
        return dataSource.read(0, buffer, offset, length);
    }

    @Override
    public long skip(long byteCount) throws IOException {
        checkOpened();
        return super.skip(byteCount);
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            log.debug("Close()");
            dataSource.close();
            closed = true;
        }
    }

    private void checkOpened() throws IOException {
        if (!opened) {
            log.debug("Reopning DataSource");
            dataSource.open(uri);
            opened = true;
        }
    }

}
