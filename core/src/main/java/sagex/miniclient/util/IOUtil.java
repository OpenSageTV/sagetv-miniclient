package sagex.miniclient.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by seans on 15/09/15.
 */
public class IOUtil {
    public static void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        while (src.read(buffer) != -1) {
            // prepare the buffer to be drained
            buffer.flip();
            // write to the channel, may block
            dest.write(buffer);
            // If partial transfer, shift remainder down
            // If buffer is empty, same as doing clear()
            buffer.compact();
        }
        // EOF will leave buffer in fill state
        buffer.flip();
        // make sure the buffer is fully drained.
        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }
    }

    public static void fastCopy(final InputStream src, final OutputStream dest) throws IOException {
        final ReadableByteChannel inputChannel = Channels.newChannel(src);
        final WritableByteChannel outputChannel = Channels.newChannel(dest);
        fastChannelCopy(inputChannel, outputChannel);
        inputChannel.close();
        outputChannel.close();
    }

    public static String toString(InputStream is) throws IOException {
        // this was causing crashes on on Samsung Phones
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        fastCopy(is, os);
//        return os.toString("UTF-8");

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        return line;
    }

    public static String toString(File f) throws IOException {
        FileInputStream fis = null;
        try {
            return toString(fis=new FileInputStream(f));
        } finally {
            if (fis!=null) {
                fis.close();
            }
        }
    }

    public static void StringToFile(String text, File f) throws IOException {
        FileOutputStream fos = null;
        try {
            fos=new FileOutputStream(f);
            fos.write(text.getBytes("UTF-8"));
            fos.flush();
        } finally {
            if (fos!=null) {
                fos.close();
            }
        }
    }

}
