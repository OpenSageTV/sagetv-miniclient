/*
 * Copyright 2015 The SageTV Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sagex.miniclient;

import java.io.IOException;

import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Rectangle;

public interface MiniPlayerPlugin extends Runnable {
    /**
     * Indicates the MediaPlayer is in an uninitialized state
     */
    int NO_STATE = 0;
    /**
     * Indicates the MediaPlayer has loaded a file and is ready for playback
     */
    int LOADED_STATE = 1;
    /**
     * The MediaPlayer is playing
     */
    int PLAY_STATE = 2;
    /**
     * The MediaPlayer is paused
     */
    int PAUSE_STATE = 3;
    /**
     * The MediaPlayer is stopped
     */
    int STOPPED_STATE = 4;
    /**
     * The MediaPlayer has encountered an end of stream
     */
    int EOS_STATE = 5;

    void free();

    /**
     * Sets the push to true to use PUSH or false to use PULL
     *
     * @param b
     */
    void setPushMode(boolean b);

    /**
     * Should check pushMode to determine if PUSH or PULL is being used
     * @param b
     * @param b1
     * @param s
     * @param urlString
     * @param o
     * @param b2
     * @param i
     */
    void load(byte b, byte b1, String s, String urlString, Object o, boolean b2, int i);

    long getMediaTimeMillis();

    /**
     * Appears to be used only during detailed buffered stats
     * @return
     */
    int getState();

    void setMute(boolean b);

    void stop();

    void pause();

    void play();

    void seek(long maxValue);

    void inactiveFile();

    long getLastFileReadPos();

    int getVolume();

    int setVolume(float v);

    void setVideoRectangles(Rectangle srcRect, Rectangle destRect, boolean b);

    Dimension getVideoDimensions();

    void pushData(byte[] cmddata, int bufDataOffset, int buffSize) throws IOException;

    void flush();
}
