package sagex.miniclient.httpbridge;

import java.io.IOException;

/**
 * Created by seans on 06/10/15.
 */
public interface DataSource {
    /**
     * Get the URI that opened this DataSource (should be push: or stv:)
     *
     * @return
     */
    String getUri();

    /**
     * Set the URI for this datasource
     * @param uri
     */
    void setUri(String uri);

    /**
     * Open the Given URI
     *
     * @param uri
     * @return
     * @throws IOException
     */
    long open(String uri) throws IOException;

    /**
     * Read stream of bytes into the bytes array
     *
     * @param bytes
     * @param offset
     * @param len
     * @return
     * @throws IOException
     */
    int read(long readOffset, byte[] bytes, int offset, int len) throws IOException;

    /**
     * Close the datasource
     */
    void close();

    /**
     * Flush all data in the datasource
     */
    void flush();

    /**
     * Get the FileName for the datasource
     * @return
     */
    String getFileName();

    /**
     * Amount of empty buffer space available in the datasource
     * @return
     */
    int bufferAvailable();

    /**
     * Size of the Stream for the datasource, or -1, if not known.
     *
     * @return
     */
    long size();

    /**
     * returns true if the datasource is open
     *
     * @return
     */
    boolean isOpen();
}
