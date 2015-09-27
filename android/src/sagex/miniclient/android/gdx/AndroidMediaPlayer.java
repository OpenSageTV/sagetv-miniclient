package sagex.miniclient.android.gdx;

import android.util.Log;

import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Rectangle;

/**
 * Created by seans on 27/09/15.
 */
public class AndroidMediaPlayer implements MiniPlayerPlugin {
    static final String TAG = "ANDROIDPLAYER";

    public AndroidMediaPlayer() {
    }

    @Override
    public void free() {
        Log.d(TAG, "free()");
    }

    @Override
    public void setPushMode(boolean b) {
        Log.d(TAG, "setPushMode: " + b);
    }

    @Override
    public void load(byte b, byte b1, String s, String urlString, Object o, boolean b2, int i) {
        Log.d(TAG, "load: b:" + b + "; b1:" + b1 + "; s: " + s + "; url:" + urlString + "; o: " + o + "; b2: " + b2 + "; i: " + i);
    }

    @Override
    public long getMediaTimeMillis() {
        Log.d(TAG, "getMediaTimeMillis()");
        return 0;
    }

    @Override
    public int getState() {
        Log.d(TAG, "getState()");
        return 0;
    }

    @Override
    public void setMute(boolean b) {
        Log.d(TAG, "setMute(): " + b);
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop()");
    }

    @Override
    public void pause() {
        Log.d(TAG, "pause()");
    }

    @Override
    public void play() {
        Log.d(TAG, "play()");
    }

    @Override
    public void seek(long maxValue) {
        Log.d(TAG, "seek(): " + maxValue);
    }

    @Override
    public void inactiveFile() {
        Log.d(TAG, "inactivateFile()");
    }

    @Override
    public long getLastFileReadPos() {
        Log.d(TAG, "getLastFileReadPos()");
        return 0;
    }

    @Override
    public int getVolume() {
        Log.d(TAG, "getVolume()");
        return 0;
    }

    @Override
    public int setVolume(float v) {
        Log.d(TAG, "setVolume(): " + v);
        return 0;
    }

    @Override
    public void setVideoRectangles(Rectangle srcRect, Rectangle destRect, boolean b) {
        Log.d(TAG, "setVideoRectangles(): " + srcRect + ", " + destRect + ", " + b);
    }

    @Override
    public Dimension getVideoDimensions() {
        Log.d(TAG, "getVideoDimensions()");
        return new Dimension(1280, 720);
    }

    @Override
    public void run() {
        Log.d(TAG, "run()");
    }
}
