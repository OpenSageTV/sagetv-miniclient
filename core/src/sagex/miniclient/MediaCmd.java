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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.Rectangle;

/**
 * @author Narflex
 */
public class MediaCmd {
    public static final int MEDIACMD_INIT = 0;
    public static final int MEDIACMD_DEINIT = 1;
    public static final int MEDIACMD_OPENURL = 16;
    public static final int MEDIACMD_GETMEDIATIME = 17;
    // length, url
    public static final int MEDIACMD_SETMUTE = 18;
    public static final int MEDIACMD_STOP = 19;
    // mute
    public static final int MEDIACMD_PAUSE = 20;
    public static final int MEDIACMD_PLAY = 21;
    public static final int MEDIACMD_FLUSH = 22;
    public static final int MEDIACMD_PUSHBUFFER = 23;
    public static final int MEDIACMD_GETVIDEORECT = 24;
    // size, flags, data
    public static final int MEDIACMD_SETVIDEORECT = 25;
    // returns 16bit width, 16bit height
    public static final int MEDIACMD_GETVOLUME = 26;
    // x, y, width, height, x, y, width, height
    public static final int MEDIACMD_SETVOLUME = 27;
    // volume
    public static final int MEDIACMD_FRAMESTEP = 28;
    public static final int MEDIACMD_SEEK = 29;

    public static final Map<Integer, String> CMDMAP = new HashMap<Integer, String>();
    private static final Logger log = LoggerFactory.getLogger(MediaCmd.class);

    static {
        CMDMAP.put(MEDIACMD_INIT, "MEDIACMD_INIT");
        CMDMAP.put(MEDIACMD_DEINIT, "MEDIACMD_DEINIT");
        CMDMAP.put(MEDIACMD_OPENURL, "MEDIACMD_OPENURL");
        CMDMAP.put(MEDIACMD_GETMEDIATIME, "MEDIACMD_GETMEDIATIME");

        CMDMAP.put(MEDIACMD_SETMUTE, "MEDIACMD_SETMUTE");
        CMDMAP.put(MEDIACMD_STOP, "MEDIACMD_STOP");

        CMDMAP.put(MEDIACMD_PAUSE, "MEDIACMD_PAUSE");
        CMDMAP.put(MEDIACMD_PLAY, "MEDIACMD_PLAY");
        CMDMAP.put(MEDIACMD_FLUSH, "MEDIACMD_FLUSH");
        CMDMAP.put(MEDIACMD_PUSHBUFFER, "MEDIACMD_PUSHBUFFER");
        CMDMAP.put(MEDIACMD_GETVIDEORECT, "MEDIACMD_GETVIDEORECT");

        CMDMAP.put(MEDIACMD_SETVIDEORECT, "MEDIACMD_SETVIDEORECT");

        CMDMAP.put(MEDIACMD_GETVOLUME, "MEDIACMD_GETVOLUME");

        CMDMAP.put(MEDIACMD_SETVOLUME, "MEDIACMD_SETVOLUME");

        CMDMAP.put(MEDIACMD_FRAMESTEP, "MEDIACMD_FRAMESTEP");
        CMDMAP.put(MEDIACMD_SEEK, "MEDIACMD_SEEK");
    }

    private final MiniClient client;
    private MiniPlayerPlugin playa;
    private long pushDataLeftBeforeInit;
    private long bufferFilePushedBytes;
    private boolean pushMode;
    private int numPushedBuffers;
    private int DESIRED_VIDEO_PREBUFFER_SIZE = 4 * 1024 * 1024;
    private int DESIRED_AUDIO_PREBUFFER_SIZE = 2 * 1024 * 1024;
    private int maxPrebufferSize;
    private MiniClientConnection myConn;
    private int statsChannelBWKbps;
    private int statsStreamBWKbps;
    private int statsTargetBWKbps;
    private long serverMuxTime;
    private long prebufferTime;

    /**
     * Creates a new instance of MediaCmd
     */
    public MediaCmd(MiniClient client) {
        this.client = client;
        this.myConn = client.getCurrentConnection();
    }

    public static void writeInt(int value, byte[] data, int offset) {
        data[offset] = (byte) ((value >> 24) & 0xFF);
        data[offset + 1] = (byte) ((value >> 16) & 0xFF);
        data[offset + 2] = (byte) ((value >> 8) & 0xFF);
        data[offset + 3] = (byte) (value & 0xFF);
    }

    public static void writeShort(short value, byte[] data, int offset) {
        data[offset] = (byte) ((value >> 8) & 0xFF);
        data[offset + 1] = (byte) (value & 0xFF);
    }

    public static int readInt(int pos, byte[] cmddata) {
        return ((cmddata[pos + 0] & 0xFF) << 24) | ((cmddata[pos + 1] & 0xFF) << 16) | ((cmddata[pos + 2] & 0xFF) << 8) | (cmddata[pos + 3] & 0xFF);
    }

    public static short readShort(int pos, byte[] cmddata) {
        return (short) (((cmddata[pos + 0] & 0xFF) << 8) | (cmddata[pos + 1] & 0xFF));
    }

    public MiniPlayerPlugin getPlaya() {
        return playa;
    }

    public void close() {
        if (myConn.getGfxCmd() != null)
            myConn.getGfxCmd().setVideoBounds(null, null);
        if (playa != null)
            playa.free();
        playa = null;
    }

    public int ExecuteMediaCommand(int cmd, int len, byte[] cmddata, byte[] retbuf) {
        // TODO verify sizes...
        //if (cmd != MEDIACMD_PUSHBUFFER)
        log.debug("Execute media command '{}[{}]'", cmd, CMDMAP.get(cmd));
        switch (cmd) {
            case MEDIACMD_INIT:
                log.info("MEDIACMD_INIT");
                try {
                    DESIRED_VIDEO_PREBUFFER_SIZE = Integer.parseInt(client.getProperty("video_buffer_size", "" + (4 * 1024 * 1024)));
                    DESIRED_AUDIO_PREBUFFER_SIZE = Integer.parseInt(client.getProperty("audio_buffer_size", "" + (2 * 1024 * 1024)));
                } catch (Exception e) {
                    log.error("MEDIACMD_INIT: ERROR", e);
                }
                readInt(0, cmddata); // video format code
                writeInt(1, retbuf, 0);
                return 4;
            case MEDIACMD_DEINIT:
                writeInt(1, retbuf, 0);
                close();
                return 4;
            case MEDIACMD_OPENURL:
                int strLen = readInt(0, cmddata);
                String urlString = "";
                maxPrebufferSize = DESIRED_VIDEO_PREBUFFER_SIZE;
                if (strLen > 1)
                    urlString = new String(cmddata, 4, strLen - 1);
                if (!urlString.startsWith("push:")) {
                    if (urlString.startsWith("dvd:")) {
                        log.error("DVD PlayBack not supported");
                    } else if (urlString.startsWith("file://")) {
                        playa = myConn.newPlayerPlugin();//new MiniMPlayerPlugin(myConn.getGfxCmd(), myConn);
                        playa.setPushMode(false);
                        playa.load((byte) 0, (byte) 0, "", urlString, null, false, 0);
                        pushDataLeftBeforeInit = 0;
                        pushMode = false;
                    } else {
                        playa = myConn.newPlayerPlugin();//new MiniMPlayerPlugin(myConn.getGfxCmd(), myConn);
                        // We always set it to be an active file because it'll get turned off by the streaming code if it is not.
                        // It's safe to say it's active when it's not (as long as it's a streamable file format), but the opposite is not true.
                        // So we always say it's active to avoid any problems loading the file if it's a streamable file format.
                        boolean isActive = urlString.toLowerCase().endsWith(".mpg") || urlString.toLowerCase().endsWith(".ts") ||
                                urlString.toLowerCase().endsWith(".flv");
                        playa.setPushMode(false);
                        playa.load((byte) 0, (byte) 0, "", urlString, myConn.getServerName(), isActive, 0);
                        pushDataLeftBeforeInit = 0;
                        pushMode = false;
                    }
                } else {
                    pushMode = true;
                    {
                        if (urlString.indexOf("audio") != -1 && urlString.indexOf("bf=vid") == -1) {
                            pushDataLeftBeforeInit = 1024 * 16;
                            maxPrebufferSize = DESIRED_AUDIO_PREBUFFER_SIZE;
                        } else {
                            pushDataLeftBeforeInit = 1024 * 64;
                            maxPrebufferSize = DESIRED_VIDEO_PREBUFFER_SIZE;
                        }
                        playa = myConn.newPlayerPlugin();//new MiniMPlayerPlugin(myConn.getGfxCmd(), myConn);
                        playa.setPushMode(true);
                        playa.load((byte) 0, (byte) 0, "", urlString, null, true, 0);
                    }
                }
                writeInt(1, retbuf, 0);
                return 4;
            case MEDIACMD_GETMEDIATIME:
                if (playa == null)
                    return 0;
                long theTime = playa.getMediaTimeMillis();
                writeInt((int) theTime, retbuf, 0);
                if (MiniClientConnection.detailedBufferStats) {
                    if (playa != null) {
                        retbuf[4] = (byte) (playa.getState() & 0xFF);
                    } else {
                        retbuf[4] = 0;
                    }
                    return 5;
                } else
                    return 4;
            case MEDIACMD_SETMUTE:
                writeInt(1, retbuf, 0);
                if (playa == null)
                    return 4;
                playa.setMute(readInt(0, cmddata) != 0);
                return 4;
            case MEDIACMD_STOP:
                writeInt(1, retbuf, 0);
                if (playa == null)
                    return 4;
                playa.stop();
                return 4;
            case MEDIACMD_PAUSE:
                writeInt(1, retbuf, 0);
                if (playa == null)
                    return 4;
                playa.pause();
                return 4;
            case MEDIACMD_PLAY:
                writeInt(1, retbuf, 0);
                if (playa == null)
                    return 4;
                playa.play();
                return 4;
            case MEDIACMD_FLUSH:
                writeInt(1, retbuf, 0);
                // TODO
                if (playa != null && pushMode && numPushedBuffers > 0) {
                    numPushedBuffers = 0;
                    playa.flush();
                }
                return 4;
            case MEDIACMD_PUSHBUFFER:
                int buffSize = readInt(0, cmddata);
                int flags = readInt(4, cmddata);
                int bufDataOffset = 8;
                if (MiniClientConnection.detailedBufferStats && buffSize > 0 && len > buffSize + 13) {
                    bufDataOffset += 10;
                    statsChannelBWKbps = readShort(8, cmddata);
                    statsStreamBWKbps = readShort(10, cmddata);
                    statsTargetBWKbps = readShort(12, cmddata);
                    serverMuxTime = readInt(14, cmddata);
                    if (playa != null) {
                        prebufferTime = serverMuxTime - playa.getMediaTimeMillis();
                    }
                    log.debug("STATS chanBW=" + statsChannelBWKbps + " streamBW=" + statsStreamBWKbps + " targetBW=" + statsTargetBWKbps + " pretime=" + prebufferTime);
                }
                // sometimes pushbuffer is called to just get bandwidth so don't pass that along to the player
                if (playa != null) {
                    if (buffSize > 0) {
                        bufferFilePushedBytes += buffSize;
                        numPushedBuffers++;
                        try {
                            playa.pushData(cmddata, bufDataOffset, buffSize);
                        } catch (IOException e) {
                            log.error("Pushbuffer Error", e);
                            client.closeConnection();
                        }
                    }
                    if (flags == 0x80 && playa != null) {
                        playa.inactiveFile();
                    }
                }

                int rv;
                // Always indicate we have at least 512K of buffer...there's NO reason to stop buffering additional
                // data since as playback goes on we keep writing to the filesystem anyways. Yeah, we could recover some bandwidth
                // but that's not how any online video players work and we shouldn't be any different than that.
                if (playa == null)
                    rv = Math.max(maxPrebufferSize, 131072 * 4);
                else
                    rv = (int) Math.max(131072 * 4, maxPrebufferSize - (bufferFilePushedBytes - playa.getLastFileReadPos()));
                log.debug("Finished pushing current data buffer of " + buffSize + " availSize=" + rv + " totalPushed=" + bufferFilePushedBytes +
                        "");
                writeInt(rv, retbuf, 0);
                if (MiniClientConnection.detailedBufferStats) {
                    if (playa != null) {
                        writeInt((int) playa.getMediaTimeMillis(), retbuf, 4);
                        retbuf[8] = (byte) (playa.getState() & 0xFF);
                    } else {
                        writeInt(0, retbuf, 4);
                        retbuf[8] = 0;
                    }
                    if (flags == 0x80 && (playa == null || pushDataLeftBeforeInit > 0)) {
                        retbuf[8] = (byte) (MiniPlayerPlugin.EOS_STATE & 0xFF);
                    }
                    return 9;
                } else
                    return 4;
            case MEDIACMD_GETVOLUME:
                if (playa == null)
                    writeInt(65535, retbuf, 0);
                else
                    writeInt(Math.round(playa.getVolume() * 65535), retbuf, 0);
                return 4;
            case MEDIACMD_SETVOLUME:
                if (playa == null)
                    writeInt(65535, retbuf, 0);
                else
                    writeInt(Math.round(playa.setVolume(readInt(0, cmddata) / 65535.0f) * 65535), retbuf, 0);
                return 4;
            case MEDIACMD_SETVIDEORECT:
                Rectangle srcRect = new Rectangle(readInt(0, cmddata), readInt(4, cmddata),
                        readInt(8, cmddata), readInt(12, cmddata));
                Rectangle destRect = new Rectangle(readInt(16, cmddata), readInt(20, cmddata),
                        readInt(24, cmddata), readInt(28, cmddata));
                if (playa != null)
                    playa.setVideoRectangles(srcRect, destRect, false);
                myConn.getGfxCmd().setVideoBounds(srcRect, destRect);
                writeInt(0, retbuf, 0);
                return 4;
            case MEDIACMD_GETVIDEORECT:
                Dimension vidRect = null;
                if (playa != null) {
                    vidRect = playa.getVideoDimensions();
                    writeShort((short) vidRect.width, retbuf, 0);
                    writeShort((short) vidRect.height, retbuf, 2);
                } else {
                    writeInt(0, retbuf, 0);
                }
                return 4;
            case MEDIACMD_SEEK:
                long seekTime = ((long) readInt(0, cmddata) << 32) | readInt(4, cmddata);
                if (playa != null)
                    playa.seek(seekTime);
                return 0;
            default:
                return -1;
        }
    }
}
