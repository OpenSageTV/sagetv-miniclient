package sagex.miniclient.net;

import java.io.IOException;

import sagex.miniclient.util.VerboseLogging;

/**
 * Extends the PullDataSource by adding buffering.  This means the requests to sagetv server will
 * request a minumum buffer of data with each request, and, then it read requests will determine
 * if the data is aleady in the buffer, or if it needs to clear the buffer and fill it again.
 * <p/>
 * Players like ExoPlayer do a lot of single byte reads, which, without buffering, these single
 * byte reads would be sent directly to the sagetv server and is very inefficient.
 */
public class BufferedPullDataSource extends SimplePullDataSource {
    static final int MAX_BUFFER = 32768;
    byte _buffer[];

    int bytesRead = 0; // how many bytes have we read into our buffer
    int bufPos = 0; // where we are in the buffer position
    long lastBufferStartPos = 0; // last "position" that we sent to sagetv.
    int lastReadSize = 0;

    public BufferedPullDataSource() {
    }

    @Override
    public void close() {
        super.close();

        flush();

        _buffer = null;
    }

    @Override
    public int read(long position, byte[] buffer, int offset, int len) throws IOException {
        int total = 0;
        int read = 0;
        int sanity = 0;
        while (true) {
            read = bufferedRead(position + total, buffer, offset + total, Math.min(len - total, MAX_BUFFER));
            if (read == -1 && total == 0) return -1;
            if (read == -1) break;
            if (read == 0) break;
            total += read;
            if (total >= len) break;
            if (sanity++ > 500) {
                throw new IOException("Invalid buffer fill request: len: " + len + "; total: " + total);
            }
        }
        return total;
    }

    int bufferedRead(long position, byte[] buffer, int offset, int len) throws IOException {
        // if we don't have any data, then read
        // if the distance between these 2 positions is larger than a our buffer, then read new data
        // if the amount requested is not in our buffer, then load a new buffer
        // if buffer pos + len is greater than what is have in our buffer, then re-fill the buffer
        if (bytesRead == 0 || Math.abs(position - lastBufferStartPos) > MAX_BUFFER || bufPos + len > MAX_BUFFER || bufPos + len > bytesRead) {
            if (VerboseLogging.DATASOURCE_LOGGING)
                log.debug("Filling Buffer with {} bytes at postion {}", len, position);
            // if might be more efficient to use a Circular Buffer, but right now I don't have the
            // time to work in that logic :(, so we just flush and fill.
            flush();
            bytesRead = fillBuffer(position);
            if (bytesRead < 0) return -1;
            if (bytesRead == 0) return 0;
            lastBufferStartPos = position;
            bufPos = 0;
        }

        // copy the buffer
        lastReadSize = Math.min(len, bytesRead);
        System.arraycopy(_buffer, bufPos, buffer, offset, lastReadSize);
        bufPos += lastReadSize;
        return lastReadSize;
    }

    int fillBuffer(long position) throws IOException {
        return super.read(position, _buffer, 0, MAX_BUFFER);
    }

    @Override
    public long open(String uri) throws IOException {
        if (_buffer != null) {
            throw new IOException("Attempting to OPEN an already OPENED Pull Data Source with URI: " + uri);
        }
        _buffer = new byte[MAX_BUFFER];
        flush();

        return super.open(uri);
    }

    public void flush() {
        lastBufferStartPos = 0;
        bytesRead = 0;
        bufPos = 0;
        lastReadSize = 0;
    }
}
