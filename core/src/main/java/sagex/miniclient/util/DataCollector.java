package sagex.miniclient.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by seans on 17/12/15.
 */
public class DataCollector {
    String prefix;
    File outFile;
    FileOutputStream fos = null;
    long maxSize = 1024 * 1024 * 10;
    long bytesRead = 0;

    public DataCollector() {
        this(null, null, -1);
    }

    public DataCollector(String prefix) {
        this(prefix, null, -1);
    }

    private static String getTimeStamp() {
        DateFormat df = new SimpleDateFormat("yyyyMMdd_hhmmss_SSS");
        return df.format(new Date());
    }

    public DataCollector(File out) {
        this(null, out, 1024 * 1024 * 10);
    }

    public DataCollector(String prefix, File out, long maxSize) {
        prefix = (prefix != null ? prefix : "");
        this.prefix = prefix;
        this.outFile = (out != null ? out : new File("/sdcard/Movies/" + prefix + "sagetv-sample-" + getTimeStamp() + ".ts"));
        this.maxSize = (maxSize > 0 ? maxSize : (1024 * 1024 * 10));
        if (outFile.getParentFile() != null && !outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
    }

    public void open() throws IOException {
        fos = new FileOutputStream(outFile);
    }

    public void write(byte[] buffer, int offset, int len) throws IOException {
        if (fos == null) return;
        try {
            fos.write(buffer, offset, len);
        } catch (Exception e) {
            // basically the stream was closed by someone else
            close();
        }
        bytesRead += len;
        if (bytesRead >= maxSize) {
            close();
        }
    }

    public void close() {
        if (fos == null) return;
        try {
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fos = null;
    }
}
