package sagex.miniclient.net;

import java.io.IOException;

/**
 * Created by seans on 23/12/15.
 */
public interface ISageTVDataSource {
    long open(String uri) throws IOException;

    void close();

    long size();

    int read(long position, byte[] buffer, int offset, int len) throws IOException;
}
