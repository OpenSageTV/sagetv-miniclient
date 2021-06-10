package sagex.miniclient.android.video.exoplayer2;

import android.net.Uri;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ScatteringByteChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sagex.miniclient.net.BufferedPullDataSource;
import sagex.miniclient.net.HasClose;

/**
 * Created by seans on 10/12/15.
 */
public class Exo2PullDataSource implements DataSource, HasClose
{
    static final Logger log = LoggerFactory.getLogger(Exo2PullDataSource.class);
    private String host = null;
    BufferedPullDataSource dataSource = null;
    private long startPos;
    private Uri uri;

    public Exo2PullDataSource(String host)
    {
        this.host=host;
    }
    
    
    @Override
    public void addTransferListener(TransferListener transferListener)
    {
    
    }
    
    @Override
    public long open(DataSpec dataSpec) throws IOException
    {
        dataSource = new BufferedPullDataSource(host);
        this.uri = dataSpec.uri;
        long size = dataSource.open(dataSpec.uri.toString());
        this.startPos = dataSpec.position;
        log.debug("Open: {}, Offset: {}", dataSource.getUri(), startPos);
        return size;

    }

    @Override
    public void close() throws IOException
    {
        if (dataSource != null)
        {
            dataSource.close();
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException
    {
        try
        {
            if (dataSource == null)
            {
                log.debug("DATA SOURCE IS NULL");
                return 0;
            }
            int bytes = dataSource.read(startPos, buffer, offset, readLength);

            //log.debug("Bytes: {}", bytes);

            if (bytes == -1)
            {
                log.debug("DATA SOURCE RETURNED -1");
                return -1;
            }
            startPos += bytes;
            return bytes;
        }
        catch(Exception ex)
        {
            log.debug("Data source read error: " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public Uri getUri() {
        return uri;
    }
    
    @Override
    public Map<String, List<String>> getResponseHeaders()
    {
        return new HashMap<String, List<String>>();
    }


//    @Override
//    public Map<String, List<String>> getResponseHeaders() {
//        return Collections.emptyMap();
//    }
//
//    @Override
//    public void addTransferListener(TransferListener transferListener) {
//    }
}
