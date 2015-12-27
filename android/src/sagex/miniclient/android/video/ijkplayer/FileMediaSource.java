package sagex.miniclient.android.video.ijkplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

public class FileMediaSource implements IMediaDataSource {
    private final File file;
    RandomAccessFile randomAccessFile;

    public FileMediaSource(File file) throws FileNotFoundException {
        this.file = file;
        randomAccessFile = new RandomAccessFile(file, "r");
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        randomAccessFile.seek(position);
        long pos = randomAccessFile.getFilePointer();
        int read = randomAccessFile.read(buffer, offset, size);
        System.out.printf("FileMediaSource.readAt(): bufsize: %s, position: %s, offset: %s, size: %s, bytes read: %s, (filepos during read: %s)\n", buffer.length, position, offset, size, read, pos);
        return read;
    }

    @Override
    public long getSize() throws IOException {
        return file.length();
    }

    @Override
    public void close() throws IOException {
        randomAccessFile.close();
    }
}
