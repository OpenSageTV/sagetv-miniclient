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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import sagex.miniclient.events.ConnectionLost;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.MouseEvent;
import sagex.miniclient.uibridge.UIRenderer;
import sagex.miniclient.util.Utils;


//import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
//import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;



public class MiniClientConnection implements SageTVInputCallback
{
    public static final String QUICKTIME = "Quicktime";
    public static final String AAC = "AAC";
    public static final String AC3 = "AC3";
    public static final String MPEG2_TS = "MPEG2-TS";
    public static final String MPEG2_VIDEO = "MPEG2-Video";
    public static final String WMA7 = "WMA7";
    public static final String WMA8 = "WMA8";
    public static final String WMA9LOSSLESS = "WMA9Lossless";
    public static final String WMA_PRO = "WMAPRO";
    public static final String WMV9 = "WMV9";
    public static final String WMV8 = "WMV8";
    public static final String WMV7 = "WMV7";
    public static final String FLASH_VIDEO = "FlashVideo";
    public static final String H264 = "H.264";
    public static final String OGG = "Ogg";
    public static final String VORBIS = "Vorbis";
    public static final String MPEG2_PS = "MPEG2-PS";
    public static final String MPEG2_PES_VIDEO = "MPEG2-PESVideo";
    public static final String MPEG2_PES_AUDIO = "MPEG2-PESAudio";
    public static final String MPEG1 = "MPEG1";
    public static final String JPEG = "JPEG";
    public static final String GIF = "GIF";
    public static final String PNG = "PNG";
    public static final String BMP = "BMP";
    public static final String MP2 = "MP2";
    public static final String MP3 = "MP3";
    public static final String MPEG1_VIDEO = "MPEG1-Video";
    public static final String MPEG4_VIDEO = "MPEG4-Video";
    public static final String MPEG4X = "MPEG4X"; // this is our private stream
    // format we put inside
    // MPEG2 PS for MPEG4/DivX
    // video on Windows
    public static final String AVI = "AVI";
    public static final String WAV = "WAV";
    public static final String ASF = "ASF";
    public static final String FLAC = "FLAC";
    public static final String MATROSKA = "MATROSKA";
    public static final String VC1 = "VC1";
    public static final String ALAC = "ALAC"; // Apple lossless
    public static final String SMIL = "SMIL"; // for SMIL-XML files which
    // represent sequences of
    // content
    public static final String VP6F = "VP6F";

    public static final String DEFAULT_VIDEO_CODECS = "MPEG2-VIDEO,MPEG2-VIDEO@HL,MPEG1-VIDEO,MPEG4-VIDEO,DIVX3,MSMPEG4,FLASHVIDEO,H.264,WMV9,VC1,MJPEG,HEVC,VP8,VP9";
    public static final String DEFAULT_AUDIO_CODECS = "MPG1L2,MPG1L3,AC3,AC4,AAC,AAC-HE,WMA,FLAC,VORBIS,PCM,DTS,DCA,PCM_S16LE,WMA8,ALAC,WMAPRO,0X0162,DolbyTrueHD,DTS-HD,DTS-MA,EAC3,EC-3,OPUS";
    public static final String DEFAULT_PULL_FORMATS = "AVI,FLASHVIDEO,Quicktime,Ogg,MP3,AAC,WMV,ASF,FLAC,MATROSKA,WAV,AC3,MPEG2-PS,MPEG2-TS,MPEG1-PS";
    public static final String DEFAULT_PUSH_FORMATS =  "MPEG2-PS,MPEG2-TS,MPEG1-PS";

    public static final int DRAWING_CMD_TYPE = 16;
    public static final int GET_PROPERTY_CMD_TYPE = 0;
    public static final int SET_PROPERTY_CMD_TYPE = 1;
    public static final int FS_CMD_TYPE = 2;
    public static final int IR_EVENT_REPLY_TYPE = 128;
    public static final int KB_EVENT_REPLY_TYPE = 129;
    public static final int MPRESS_EVENT_REPLY_TYPE = 130;
    public static final int MRELEASE_EVENT_REPLY_TYPE = 131;
    public static final int MCLICK_EVENT_REPLY_TYPE = 132;
    public static final int MMOVE_EVENT_REPLY_TYPE = 133;
    public static final int MDRAG_EVENT_REPLY_TYPE = 134;
    public static final int MWHEEL_EVENT_REPLY_TYPE = 135;
    public static final int SAGECOMMAND_EVENT_REPLY_TYPE = 136;
    public static final int UI_RESIZE_EVENT_REPLY_TYPE = 192;
    public static final int UI_REPAINT_EVENT_REPLY_TYPE = 193;
    public static final int MEDIA_PLAYER_UPDATE_EVENT_REPLY_TYPE = 201;
    public static final int REMOTE_FS_HOTPLUG_INSERT_EVENT_REPLY_TYPE = 202;
    public static final int REMOTE_FS_HOTPLUG_REMOVE_EVENT_REPLY_TYPE = 203;
    public static final int OUTPUT_MODES_CHANGED_REPLY_TYPE = 224;
    public static final int SUBTITLE_UPDATE_REPLY_TYPE = 225;
    public static final int IMAGE_UNLOAD_REPLY_TYPE = 226;
    public static final int OFFLINE_CACHE_CHANGE_REPLY_TYPE = 227;
    // Tells the GFX channel to force the media channel to reconnect
    public static final int GFXCMD_MEDIA_RECONNECT = 131;
    public static final int FS_RV_SUCCESS = 0;
    public static final int FS_RV_PATH_EXISTS = 1;
    public static final int FS_RV_NO_PERMISSIONS = 2;
    public static final int FS_RV_PATH_DOES_NOT_EXIST = 3;
    public static final int FS_RV_NO_SPACE_ON_DISK = 4;
    public static final int FS_RV_ERROR_UNKNOWN = 5;
    public static final int FSCMD_CREATE_DIRECTORY = 64;
    public static final int FS_PATH_HIDDEN = 0x01;
    public static final int FS_PATH_DIRECTORY = 0x02;
    public static final int FS_PATH_FILE = 0x04;
    public static final int FSCMD_GET_PATH_ATTRIBUTES = 65;
    public static final int FSCMD_GET_FILE_SIZE = 66;
    public static final int FSCMD_GET_PATH_MODIFIED_TIME = 67;
    // pathlen, path
    public static final int FSCMD_DIR_LIST = 68;
    public static final int FSCMD_LIST_ROOTS = 69;
    public static final int FSCMD_DOWNLOAD_FILE = 70;
    public static final int FSCMD_UPLOAD_FILE = 71;
    // pathlen, path
    public static final int FSCMD_DELETE_FILE = 72;
    private static final Logger log = LoggerFactory.getLogger(MiniClientConnection.class);
    // private static final int PUSH_BUFFER_LIMIT = 32 * 1024;
    // pathlen, path
    // 64-bit return value
    public static boolean detailedBufferStats = false;
    // pathlen, path
    // 64-bit return value
    public static String CONNECT_FAILURE_GENERAL_INTERNET = "The SageTV Placeshifter is having trouble connecting to the Internet. "
            + "Please make sure your Internet connection is established and properly configured. "
            + "If you have firewall software enabled (like ZoneAlarm or the Windows Firewall) be sure that the SageTV Placeshifter is allowed to have outgoing network access.";
    // pathlen, path
    // 16-bit numEntries, *(16-bit pathlen, path)
    public static String CONNECT_FAILURE_LOCATOR_SERVER = "The SageTV Placeshifter is unable to connect to the SageTV Locator server. "
            + "The SageTV Locator server may be temporarily down, or connection to it may be blocked by a firewall. "
            + "If you have firewall software enabled (like ZoneAlarm or the Windows Firewall) be sure that the SageTV Placeshifter is allowed to have outgoing network access on port 8018. "
            + "If you're on a network that has a firewall, please contact your network administrator and ask them if they can open the outbound port 8018 for you.";
    // pathlen, path
    // 16-bit numEntries, *(16-bit pathlen, path)
    public static String CONNECT_FAILURE_LOCATOR_REGISTRATION = "The SageTV Placeshifter is unable to connect to the specified SageTV Media Center Server because it is not registered with the SageTV Locator service. "
            + "You may have entered your Locator ID incorrectly. If your Locator ID is correct, please make sure that the Placeshifter is configured on your SageTV Media Center, and that there's no outbound firewall restrictions on port 8018. "
            + "This can be done by going through the Configuration Wizard and testing the Placeshifter connection.";
    // secureID[4], offset[8], size[8], pathlen, path
    public static String CONNECT_FAILURE_SEVER_SIDE = "The SageTV Placeshifter is unable to connect to the specified SageTV Media Center Server. "
            + "Please make sure that the Placeshifter is configured on your SageTV Media Center and that port forwarding is properly configured for your network. "
            + "This can be done by going through the Configuration Wizard on the SageTV Media Center and testing the Placeshifter connection.";
    public static String CONNECT_FAILURE_CLIENT_SIDE = "The SageTV Placeshifter is unable to connect to the specified SageTV Media Center Server. "
            + "Check to make sure you don't have any local firewall software running that may be blocking the connection. "
            + "The connection may also be blocked by a firewall on your network. If you're being blocked by a network firewall, "
            + "you should try reconfiguring the Placeshifter on the SageTV Server to use a common external port such as 80 or 443. "
            + "This can be done in the Configuration Wizard on the SageTV Media Center.";
    // pathlen, path
    public static int HIGH_SECURITY_FS = 3;
    public static int MED_SECURITY_FS = 2;
    public static int LOW_SECURITY_FS = 1;

    private MiniClient client;
    private UIRenderer<?> uiRenderer;
    private MediaCmd myMedia;
    private java.net.Socket gfxSocket;
    private java.net.Socket mediaSocket;

    private String myID;
    private java.io.DataInputStream gfxIs;

    // This is the secret symmetric key encrypted with the public key
    private byte[] encryptedSecretKeyBytes;
    private java.security.PublicKey serverPublicKey;
    private javax.crypto.Cipher evtEncryptCipher;
    private java.security.Key mySecretKey;
    private boolean encryptEvents;
    private java.io.DataOutputStream eventChannel;
    private int replyCount;
    private GFXCMD2 myGfx;
    private EventRouterThread eventRouterThread;
    private boolean alive;
    private String currentCrypto = null;
    private boolean fontServer;
    private java.util.Timer uiTimer;
    private java.io.File tempfile;
    private java.io.File tempfile2; // TODO: add function to get name for
    // mplayer
    private java.nio.ByteBuffer mappedVideo;
    private int serverfd = -1;
    private int socketfd = -1;
    private String mappedfname;
    private int videowidth = 0;
    private int videoheight = 0;
    private int videoformat = 0;
    private int videoframetype = 0;
    private int fsSecurity;
    private boolean subSupport = false;
    // We need this for being able to store the auth block in the properties
    // file correctly
    private ServerInfo msi;
    private boolean zipMode;
    private java.util.Map lruImageMap = new java.util.HashMap();
    private boolean usesAdvancedImageCaching;
    private boolean reconnectAllowed;
    private boolean firstFrameStarted;
    private boolean performingReconnect;

    private List<String> videoCodecs = new ArrayList<String>();
    private List<String> audioCodecs = new ArrayList<String>();
    private List<String> pushFormats = new ArrayList<String>();
    private List<String> pullFormats = new ArrayList<String>();

    private MenuHint menuHint = new MenuHint();
    private Properties profileProperties;

    public MiniClientConnection(MiniClient client, String myID, ServerInfo msi)
    {
        this.client = client;
        currentCrypto = client.getCryptoFormats();

        uiRenderer = client.getUIRenderer();
        if (uiRenderer == null)
        {
            throw new RuntimeException("client.setUIRenderer() needs to be set before creating the connection");
        }

        if (msi.port <= 0)
        {
            msi.port = 31099;
        }

        if (!Utils.isEmpty(msi.address))
        {
            if (msi.address.indexOf(":") != -1)
            {
                msi.address = msi.address.substring(0, msi.address.indexOf(":"));
                msi.port = 31099;
                try
                {
                    msi.port = Integer.parseInt(msi.address.substring(msi.address.indexOf(":") + 1));
                }
                catch (NumberFormatException e) { }
            }
        }

        this.myID = myID;

        if (msi.macAddress!=null && !msi.macAddress.trim().isEmpty()) {
            log.info("Overriding CLIENT ID with Connection Specific ID: Old ID: {}; New ID: {}", myID, msi.macAddress);
            this.myID = msi.macAddress;
        }

        this.msi = msi;
        usesAdvancedImageCaching = false;
    }

    public MenuHint getMenuHint() {
        return menuHint;
    }

    // Needed for local video images...
    private static int getInt(byte[] buf, int offset)
    {
        int value = (buf[offset] & 0xFF) << 24;
        value |= (buf[offset + 1] & 0xFF) << 16;
        value |= (buf[offset + 2] & 0xFF) << 8;
        value |= (buf[offset + 3] & 0xFF) << 0;
        return value;
    }

    private static void putInt(byte[] buf, int offset, int value)
    {
        buf[offset] = (byte) ((value >> 24) & 0xFF);
        buf[offset + 1] = (byte) ((value >> 16) & 0xFF);
        buf[offset + 2] = (byte) ((value >> 8) & 0xFF);
        buf[offset + 3] = (byte) ((value >> 0) & 0xFF);
    }

    private static void putString(byte[] buf, int offset, String str)
    {
        try
        {
            byte[] b = str.getBytes("ISO8859_1");
            System.arraycopy(b, 0, buf, offset, str.length());
        }
        catch (Exception e)
        {
            log.error("putString()", e);
            e.printStackTrace();
        }
    }

    private static String getCmdString(byte[] data, int offset) {
        int length = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
        try {
            return new String(data, offset + 2, length, "UTF-8");
        } catch (java.io.UnsupportedEncodingException uee) {
            return new String(data, offset + 2, length);
        }
    }

    private static long getCmdLong(byte[] data, int offset) {
        long rv = 0;
        for (int i = 0; i < 8; i++)
            rv = (rv << 8) | ((long) (data[offset + i] & 0xFF));
        return rv;
    }

    private java.net.Socket EstablishServerConnection(int connType) throws java.io.IOException {
        int flag = 1;
        java.net.Socket sake = null;
        java.io.InputStream inStream = null;
        java.io.OutputStream outStream = null;
        try {
            log.info("Establishing Server Connection {} for Connection Type: {}", msi, connType);
            sake = new java.net.Socket();
            sake.connect(new java.net.InetSocketAddress(msi.address, msi.port), 30000);
            sake.setSoTimeout(30000);
            sake.setTcpNoDelay(true);
            sake.setKeepAlive(true);
            outStream = sake.getOutputStream();
            inStream = sake.getInputStream();
            byte[] msg = new byte[7];
            msg[0] = (byte) 1;

            if (myID == null) {
                myID = client.getMACAddress();
            }

            log.info("Establishing Server Connection using Client ID '{}'", myID);

            if (myID != null) {
                int len = Math.min((msg.length - 1) * 3, myID.length());
                for (int i = 0; i < len; i += 3) {
                    msg[1 + i / 3] = (byte) (Integer.parseInt(myID.substring(i, i + 2), 16) & 0xFF);
                }
            }
            log.info("Establishing Server Connection using Client ID '{}'", msg);
            outStream.write(msg);
            outStream.write(connType);
            int rez = inStream.read();
            if (rez != 2) {
                log.error("Error with reply from server: {}", rez);
                inStream.close();
                outStream.close();
                sake.close();
                return null;
            }
            log.info("Connection accepted by server: {}", msi);
            sake.setSoTimeout(0);
            return sake;
        } catch (java.io.IOException e) {
            log.error("ERROR with socket connection", e);
            try {
                if (sake!=null)
                    sake.close();
            } catch (Exception e1) {
            }
            try {
                if (inStream!=null)
                    inStream.close();
            } catch (Exception e1) {
            }
            try {
                if(outStream!=null)
                    outStream.close();
            } catch (Exception e1) {
            }
            throw e;
        }
    }

    public void connect() throws java.io.IOException {
        discoverCodecSupport();

        if (client.getCurrentConnection() != null) {
            // TODO: We should check if the connection is the same as this one, if so, then just use this connection.
            if (client.getCurrentConnection() != this) {
                log.warn("We already have server connection.  Shutting it down before connecting with this one.");
                client.getCurrentConnection().close();
            }
        }

        log.info("Connecting to media server at {}", msi);
        while (mediaSocket == null) {
            mediaSocket = EstablishServerConnection(1);
            if (mediaSocket == null) {
                // System.out.println("couldn't connect to media server,
                // retrying in 1 secs.");
                // try{Thread.sleep(1000);}catch(InterruptedException e){}
                throw new java.net.ConnectException();
            }
        }
        log.info("Connected to media server {}", msi);

        log.info("Connecting to ui server at {}", msi);
        while (gfxSocket == null) {
            gfxSocket = EstablishServerConnection(0);
            if (gfxSocket == null) {
                // System.out.println("couldn't connect to gfx server, retrying
                // in 5 secs.");
                // try { Thread.sleep(5000);} catch (InterruptedException e){}
                throw new java.net.ConnectException();
            }
        }
        log.info("Connected to gfx server {}", msi);

        client.setCurrentConnection(this);

        alive = true;
        Thread t = new Thread("Media-" + msi.address) {
            public void run() {
                MediaThread();
            }
        };
        t.start();
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
        t = new Thread("GFX-" + msi.address) {
            public void run() {
                GFXThread();
            }
        };
        t.start();

        String str = client.properties().getString(PrefStore.Keys.local_fs_security, "high");
        if ("low".equals(str))
            fsSecurity = LOW_SECURITY_FS;
        else if ("med".equals(str))
            fsSecurity = MED_SECURITY_FS;
        else
            fsSecurity = HIGH_SECURITY_FS;
    }

    Properties loadProperties(String resourceName) {
        InputStream is = MiniClientConnection.class.getClassLoader().getResourceAsStream(resourceName);
        if (is==null) {
            log.warn("Didn't Resolve Profile Name for {}", resourceName);
            throw new RuntimeException("Missing resource " + resourceName);
        }
        Properties prop = new Properties();
        try {
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    private void discoverCodecSupport()
    {
        String profile = null;

        boolean isExoPlayer = client.properties().getString(PrefStore.Keys.default_player, "exoplayer").equalsIgnoreCase("exoplayer");
        
        if (isExoPlayer)
        {
            profile = "exoplayer.profile";
        }
        else
        {
            profile = "ijkplayer.profile";
        }
        
        profileProperties = loadProperties("common.profile");
        profileProperties.putAll(loadProperties(profile));

        String acodec = profileProperties.getProperty("AUDIO_CODECS", DEFAULT_AUDIO_CODECS);
        String vcodec = profileProperties.getProperty("VIDEO_CODECS", DEFAULT_VIDEO_CODECS);
        String pushf = profileProperties.getProperty("PUSH_FORMATS", DEFAULT_PUSH_FORMATS);
        String pullf = profileProperties.getProperty("PULL_FORMATS", DEFAULT_PULL_FORMATS);

        audioCodecs = stringToList(acodec);
        videoCodecs = stringToList(vcodec);
        pullFormats = stringToList(pullf);
        pushFormats = stringToList(pushf);
    
        //pushFormats=stringToList("MATROSKA");
        
        Properties codecs = loadProperties("codecs.properties");
        
        client.prepareCodecs(videoCodecs, audioCodecs, pushFormats, pullFormats, codecs);
    }

    private List<String> stringToList(String str) {
        ArrayList<String> list = new ArrayList<String>();
        if (str==null) return list;
        for (String s: str.split("\\s*,\\s*")) {
            list.add(s.trim());
        }
        return list;
    }

    public boolean isConnected() {
        return alive;
    }

    public void close() {
        alive = false;
        try {
            client.setCurrentConnection(null);
        } catch (Exception e) {
        }

        try {
            gfxSocket.close();
        } catch (Exception e) {
        }

        GFXCMD2 oldGfx = myGfx;
        myGfx = null;
        if (oldGfx != null)
            oldGfx.close();
        try {
            mediaSocket.close();
        } catch (Exception e) {
        }

        if (eventRouterThread!=null) {
            // shut down the event router thread
            eventRouterThread.interrupt();
        }

        client = null;
    }

    private void GFXThread()
    {

        myGfx = new GFXCMD2(client);

        detailedBufferStats = false;
        byte[] cmd = new byte[4];
        int command, len;
        int[] hasret = new int[1];
        int retval;
        byte[] retbuf = new byte[4];
        final java.util.Vector gfxSyncVector = new java.util.Vector();

        // Try to connect to the media server port to see if we can actually do
        // pull-mode streaming.
        boolean canDoPullStreaming = false;

        try
        {
            log.info("Testing to see if server can do a pull mode streaming connection at {}:{}...", msi.address, 7818);
            java.net.Socket mediaTest = new java.net.Socket();
            mediaTest.connect(new java.net.InetSocketAddress(msi.address, 7818), 2000);
            mediaTest.close();
            canDoPullStreaming = true;
            log.info("Server can do a pull-mode streaming connection at {}:{}", msi.address, 7818);
        }
        catch (Exception e)
        {
            log.warn("Failed pull mode media test....only use push mode for server {}", msi.address);
        }

        try
        {
            // create the event router thread to handle routing of event on a separate thread
            eventRouterThread = new EventRouterThread("EVTRouter");
            eventRouterThread.start();

            eventChannel = new java.io.DataOutputStream(new java.io.BufferedOutputStream(gfxSocket.getOutputStream()));
            gfxIs = new java.io.DataInputStream(gfxSocket.getInputStream());
            zipMode = false;

            // Create the parallel threads so we can sync video and UI rendering
            // appropriately
            Thread gfxReadThread = new Thread("GFXRead")
            {
                public void run()
                {
                    byte[] gfxCmds = new byte[4];
                    byte[] cmdbuffer = new byte[4096];
                    int len;
                    java.io.DataInputStream myStream = gfxIs;
                    boolean enabledzip = false;

                    while (alive)
                    {
                        synchronized (gfxSyncVector)
                        {
                            if (gfxSyncVector.contains(gfxCmds))
                            {
                                try
                                {
                                    gfxSyncVector.wait(5000);
                                }
                                catch (InterruptedException e) { }
                                continue;
                            }
                        }

                        try
                        {
                            if (zipMode && !enabledzip)
                            {
                                // Recreate stream wrappers with ZLIB
                                com.jcraft.jzlib.ZInputStream zs = new com.jcraft.jzlib.ZInputStream(gfxSocket.getInputStream(),true);
                                zs.setFlushMode(com.jcraft.jzlib.JZlib.Z_SYNC_FLUSH);
                                myStream = new java.io.DataInputStream(zs);
                                enabledzip = true;
                            }

                            // System.out.println("before gfxread readfully");
                            myStream.readFully(gfxCmds);
                            len = ((gfxCmds[1] & 0xFF) << 16) | ((gfxCmds[2] & 0xFF) << 8) | (gfxCmds[3] & 0xFF);

                            if (cmdbuffer.length < len)
                            {
                                cmdbuffer = new byte[len];
                            }

                            // Read from the tcp socket
                            myStream.readFully(cmdbuffer, 0, len);
                        }
                        catch (Exception e)
                        {
                            if (reconnectAllowed && alive && firstFrameStarted && !encryptEvents)
                            {
                                performingReconnect = true;
                                enabledzip = false;
                                log.error("GFX channel detected a connection error and we're in a mode that allows reconnect...try to reconnect to the server now", e);
                                try
                                {
                                    myStream.close();
                                }
                                catch (Exception e1) { }

                                try
                                {
                                    eventChannel.close();
                                }
                                catch (Exception e1) { }

                                try
                                {
                                    gfxSocket.close();
                                }
                                catch (Exception e1) { }

                                try
                                {
                                    gfxSocket = EstablishServerConnection(5);
                                    if (gfxSocket == null) throw new Exception("Failed to reconnect to server.  Unable to establish Graphics Socket.");
                                    eventChannel = new java.io.DataOutputStream(new java.io.BufferedOutputStream(gfxSocket.getOutputStream()));
                                    myStream = gfxIs = new java.io.DataInputStream(gfxSocket.getInputStream());

                                    if (zipMode && !enabledzip)
                                    {
                                        // Recreate stream wrappers with ZLIB
                                        com.jcraft.jzlib.ZInputStream zs = new com.jcraft.jzlib.ZInputStream(gfxSocket.getInputStream(), true);
                                        zs.setFlushMode(com.jcraft.jzlib.JZlib.Z_SYNC_FLUSH);
                                        myStream = new java.io.DataInputStream(zs);
                                        enabledzip = true;
                                    }
                                    log.info("Done doing server reconnect...continue on our merry way!");
                                }
                                catch (Exception e1)
                                {
                                    log.error("Failure in reconnecting to server...abort the client", e1);
                                    performingReconnect = false;

                                    if (client!=null)
                                    {
                                        client.eventbus().post(new ConnectionLost(performingReconnect));
                                    }
                                    synchronized (gfxSyncVector)
                                    {
                                        gfxSyncVector.add(e);
                                        return;
                                    }
                                }
                                performingReconnect = false;
                            }
                            else
                            {
                                synchronized (gfxSyncVector)
                                {
                                    gfxSyncVector.add(e);
                                    return;
                                }
                            }
                        }
                        synchronized (gfxSyncVector)
                        {
                            gfxSyncVector.add(gfxCmds);
                            gfxSyncVector.add(cmdbuffer);
                            gfxSyncVector.notifyAll();
                        }
                    }
                }
            };
            gfxReadThread.setDaemon(true);
            gfxReadThread.start();

            while (alive)
            {
                byte[] cmdbuffer;

                synchronized (gfxSyncVector)
                {
                    if (!gfxSyncVector.isEmpty())
                    {
                        Object newData = gfxSyncVector.get(0);
                        if (newData instanceof Throwable)
                        {
                            throw (Throwable) newData;
                        }
                        else
                        {
                            cmd = (byte[]) newData;
                            cmdbuffer = (byte[]) gfxSyncVector.get(1);
                        }
                    }
                    else
                    {
                        try
                        {
                            gfxSyncVector.wait(5000);
                        }
                        catch (InterruptedException e) { }
                        continue;
                    }
                }

                command = (cmd[0] & 0xFF);
                len = ((cmd[1] & 0xFF) << 16) | ((cmd[2] & 0xFF) << 8) | (cmd[3] & 0xFF);

                if ((command & 0x80) != 0) // Local video update command
                {
                    byte[] data = cmdbuffer;
                    switch (cmd[0] & 0xFF)
                    {
                        case 0x80: // New video
                            log.debug("NOT IMPLEMENTED(0x80): Video cmd {}", (cmd[0] & 0xFF));
                            videowidth = getInt(data, 0);
                            videoheight = getInt(data, 4);
                            videoformat = getInt(data, 8);
                            myGfx.createVideo(videowidth, videoheight, videoformat);
                            putInt(data, 0, mappedfname.length());
                            putString(data, 4, mappedfname);
                            putInt(data, 4 + mappedfname.length() + 0, 0); // offsetY
                            putInt(data, 4 + mappedfname.length() + 4, videowidth); // pitchY
                            putInt(data, 4 + mappedfname.length() + 8, videowidth * videoheight); // offsetU
                            putInt(data, 4 + mappedfname.length() + 12, videowidth / 2); // pitchU
                            putInt(data, 4 + mappedfname.length() + 16, videowidth * videoheight + videowidth * videoheight / 4); // offsetV
                            putInt(data, 4 + mappedfname.length() + 20, videowidth / 2); // pitchV
                            break;
                        case 0x81: // New frame
                            log.debug("NOT IMPLEMENTED(0x81): New Frame command");
                            videoframetype = getInt(data, 0);
                            myGfx.updateVideo(videoframetype, mappedVideo);
                            putInt(data, 0, 0); // offsetY
                            putInt(data, 4, videowidth); // pitchY
                            putInt(data, 8, videowidth * videoheight); // offsetU
                            putInt(data, 12, videowidth / 2); // pitchU
                            putInt(data, 16, videowidth * videoheight + videowidth * videoheight / 4); // offsetV
                            putInt(data, 20, videowidth / 2); // pitchV
                            break;
                    }

                }
                if (command == DRAWING_CMD_TYPE) // GFX cmd
                {
                    // We need to let the opengl rendering thread do that...
                    command = (cmdbuffer[0] & 0xFF);

                    if (command == GFXCMD_MEDIA_RECONNECT)
                    {
                        // Just tell the MediaThread to kill its current
                        // connection and reconnect
                        try
                        {
                            mediaSocket.close();
                        }
                        catch (Exception e) { }
                    }
                    else
                    {
                        if (command == GFXCMD2.GFXCMD_STARTFRAME)
                        {
                            firstFrameStarted = true;
                        }
                        retval = myGfx.ExecuteGFXCommand(command, len, cmdbuffer, hasret);

                        if (hasret[0] != 0)
                        {
                            retbuf[0] = (byte) ((retval >> 24) & 0xFF);
                            retbuf[1] = (byte) ((retval >> 16) & 0xFF);
                            retbuf[2] = (byte) ((retval >> 8) & 0xFF);
                            retbuf[3] = (byte) ((retval >> 0) & 0xFF);

                            try
                            {
                                synchronized (eventChannel)
                                {
                                    eventChannel.write(DRAWING_CMD_TYPE); // GFX
                                    // reply
                                    eventChannel.writeShort(0);
                                    eventChannel.write(4);// 3 byte length of 4
                                    eventChannel.writeInt(0); // timestamp
                                    eventChannel.writeInt(replyCount++);
                                    eventChannel.writeInt(0); // pad
                                    if (encryptEvents && evtEncryptCipher != null)
                                    {
                                        eventChannel.write(evtEncryptCipher.doFinal(retbuf, 0, 4));
                                    }
                                    else
                                    {
                                        eventChannel.write(retbuf, 0, 4);
                                    }
                                    eventChannel.flush();
                                }
                            }
                            catch (Throwable e)
                            {
                                eventChannelError();
                            }
                        }
                    }
                }
                else if (command == GET_PROPERTY_CMD_TYPE) // get property
                {
                    String propName = new String(cmdbuffer, 0, len);
                    String propVal = "";
                    byte[] propValBytes = null;
                    if ("GFX_TEXTMODE".equals(propName))
                    {

                        // NARFLEX - 1/17/10 - Just never allow text rendering
                        // directly because the new effects system has
                        // clipping issues associated with it and the
                        // Placeshifter never will connect to the localhost
                        // address automatically anyways. This way we'll have
                        // consistency across implementations.
                        // if (!isLocahostConnection())
                        propVal = "";
                    }
                    else if ("GFX_BLENDMODE".equals(propName))
                    {
                        propVal = "PREMULTIPLY"; // opengl using PRE
                        //propVal = "POSTMULTIPLY";
                    }
                    else if ("GFX_SCALING".equals(propName))
                    {
                        propVal = "HARDWARE"; // opengl uses hardware scaling
                    }
                    else if ("GFX_OFFLINE_IMAGE_CACHE".equals(propName))
                    {
                        if (client.properties().getBoolean(PrefStore.Keys.cache_images_on_disk, true))
                        {
                            propVal = "TRUE";
                        }
                        else
                        {
                            propVal = "FALSE";
                        }
                    }
                    else if ("OFFLINE_CACHE_CONTENTS".equals(propName))
                    {
                        propVal = client.getImageCache().getOfflineCacheList();
                    }
                    else if ("ADVANCED_IMAGE_CACHING".equals(propName))
                    {
                        propVal = "TRUE";
                        usesAdvancedImageCaching = true;
                    }
                    else if ("GFX_BITMAP_FORMAT".equals(propName))
                    {
                        if (!client.properties().getBoolean(PrefStore.Keys.use_bitmap_images, true))
                        {
                            propVal = "";
                        }
                        else
                        {
                            propVal = "PNG,JPG,GIF,BMP";
                        }
                    }
                    else if ("GFX_COMPOSITE".equals(propName))
                    {
                        propVal = "BLEND"; // opengl uses blend
                        //propVal = "COLORKEY";
                    }
                    else if ("GFX_SURFACES".equals(propName) || "GFX_HIRES_SURFACES".equals(propName))
                    {
                        propVal = "TRUE";
                    }
                    else if ("GFX_DIFFUSE_TEXTURES".equals(propName))
                    {
                        // if (myGfx instanceof DirectX9GFXCMD)
                        // propVal = "TRUE";
                        // else
                        propVal = "";
                    }
                    else if ("GFX_XFORMS".equals(propName))
                    {
                        // if (myGfx instanceof DirectX9GFXCMD)
                        // propVal = "TRUE";
                        // else
                        propVal = "";
                    }
                    else if ("GFX_TEXTURE_BATCH_LIMIT".equals(propName))
                    {
                        // We don't support this command yet
                        propVal = "";
                    }
                    else if ("GFX_COLORKEY".equals(propName))
                    {
                        propVal = "080010";
                    }
                    else if ("STREAMING_PROTOCOLS".equals(propName))
                    {
                        propVal = "file,stv";
                    }
                    else if ("INPUT_DEVICES".equals(propName))
                    {
                        propVal="IR,KEYBOARD";
                        if (client.options().isDesktopUI()) propVal+=",MOUSE";
                        if (client.options().isTouchUI()) propVal+=",TOUCH";
                        if (client.options().isTVUI()) propVal+=",TV";
                        // propVal = "IR,KEYBOARD,TOUCH"; // MOUSE,KEYBOARD,TOUCH,IR (mouse implies desktop)
                    }
                    else if ("DISPLAY_OVERSCAN".equals(propName))
                    {
                        propVal = "0;0;1.0;1.0";
                    }
                    else if ("FIRMWARE_VERSION".equals(propName))
                    {
                        // propVal = sage.Version.MAJOR_VERSION + "." +
                        // sage.Version.MINOR_VERSION + "." +
                        // sage.Version.MICRO_VERSION;
                        propVal = "9.0.0";
                    }
                    else if ("DETAILED_BUFFER_STATS".equals(propName))
                    {
                        propVal = "TRUE";
                        detailedBufferStats = true;
                    }
                    else if ("PUSH_BUFFER_SEEKING".equals(propName))
                    {
                        propVal = "TRUE";
                    }
                    else if ("GFX_SUBTITLES".equals(propName))
                    {
                        propVal = "TRUE";
                    }
                    else if ("FORCED_MEDIA_RECONNECT".equals(propName))
                    {
                        propVal = "TRUE";
                    }
                    else if ("AUTH_CACHE".equals(propName))
                    {
                        propVal = (msi != null) ? "TRUE" : "";
                        log.debug("AUTH_CACHE Called: {}", propVal);

                    }
                    else if ("GET_CACHED_AUTH".equals(propName))
                    {
                        // Make sure crypto is on before we send this back!!
                        if (encryptEvents && evtEncryptCipher != null && msi != null && msi.authBlock != null)
                        {
                            propVal = msi.authBlock;
                        }
                        else
                        {
                            propVal = "";
                        }
                        log.debug("GET_CACHED_AUTH Called: {}", propVal);
                    }
                    else if ("REMOTE_FS".equals(propName))
                    {
                        if (fsSecurity <= MED_SECURITY_FS)
                        {
                            propVal = "TRUE";
                        }
                        else
                        {
                            propVal = "";
                        }
                    }
                    else if ("GFX_VIDEO_UPDATE".equals(propName))
                    {
                        propVal = "TRUE";
                    }
                    else if ("ZLIB_COMM".equals(propName))
                    {
                        propVal = "TRUE";
                    }
                    else if ("VIDEO_CODECS".equals(propName))
                    {
                        String extra_codecs = client.properties().getString(PrefStore.Keys.mplayer_extra_video_codecs, null);
                        propVal = toStringList(videoCodecs);
                        if (extra_codecs != null)
                            propVal += "," + extra_codecs;
                    }
                    else if ("AUDIO_CODECS".equals(propName))
                    {
                        String extra_codecs = client.properties().getString(PrefStore.Keys.mplayer_extra_audio_codecs, null);
                        propVal = toStringList(audioCodecs);
                        if (extra_codecs != null)
                        {
                            propVal += "," + extra_codecs;
                        }
                    }
                    else if ("PUSH_AV_CONTAINERS".equals(propName))
                    {
                        if (((client.properties().getFixedEncodingPreference().equalsIgnoreCase("always")
                            || client.properties().getFixedRemuxingPreference().equalsIgnoreCase("always"))
                                && (client.properties().getStreamingMode()).equalsIgnoreCase("fixed")))
                        {
                            // If we are using fixed transcode always, do not allow transcode/remux to mpeg-ps/ts.
                            // pushing
                            propVal = "NONE";
                        }
                        else if (canDoPullStreaming && "pull".equalsIgnoreCase(client.properties().getStreamingMode()))
                        {
                            // If we are forced into pull mode then we don't support
                            // pushing
                            propVal = "NONE";
                        }
                        else
                        {
                            propVal = toStringList(pushFormats);
                        }
                    }
                    else if ("PULL_AV_CONTAINERS".equals(propName))
                    {

                        /*
                        PULL - Containers we can read without transcoding.
                        Set this to empty if we are remote or if we are fixed and preference is to always transcode or always remux
                        */
                        if (!canDoPullStreaming || ((client.properties().getFixedEncodingPreference().equalsIgnoreCase("always") || client.properties().getFixedRemuxingPreference().equalsIgnoreCase("always"))  && "fixed".equalsIgnoreCase(client.properties().getStreamingMode())))
                        {
                            propVal = "";
                        }
                        else
                        {
                            // if we are being forced into PULL mode, then add the push containers to our PULL containers
                            if ("pull".equalsIgnoreCase(client.properties().getStreamingMode()))
                            {
                                propVal = toStringList(pushFormats) + "," + toStringList(pullFormats);
                            }
                            else
                            {
                                propVal = toStringList(pullFormats);
                            }
                        }
                    }
                    else if ("MEDIA_PLAYER_BUFFER_DELAY".equals(propName))
                    {
                        // MPlayer needs an extra 2 seconds of buffer before it
                        // can do playback because of it's single-threaded
                        // nature
                        // NOTE: If MPlayer is not being used, this should be
                        // changed...hopefully to a lower value like 0 :)
                        propVal = "0";
                    }
                    else if ("FIXED_PUSH_MEDIA_FORMAT".equals(propName))
                    {
                        if ("fixed".equalsIgnoreCase(client.properties().getStreamingMode()))
                        {
                            String format = client.properties().getFixedEncodingContainerFormat();
                            
                            /* Video properties */
                            int videobitrate = client.properties().getFixedEncodingVideoBitrateKBPS() * 1000;

                            String framerate = client.properties().getFixedEncodingFPS();

                            int keyFrameInt = client.properties().getFixedEncodingKeyFrameInterval();

                            boolean useBFrames = client.properties().getFixedEncodingUseBFrames();

                            //TODO: Investigate why this is set as 0.  Maybe set as a property.  Could possibly expose to end user at some point
                            int bframeInterval= 0;

                            String resolution = client.properties().getFixedEncodingVideoResolution();
    
                            /* Audio properties */
                            String audioCodec = client.properties().getFixedEncodingAudioCodec();

                            int audiobitrate = client.properties().getFixedEncodingAudioBitrateKBPS() * 1000;

                            String audiochannels = client.properties().getFixedEncodingAudioChannels();
                            
                            // Build the fixed media format string
                            propVal = "container=" + format + ";";
                            propVal += "videobitrate=" + videobitrate + ";";
                            
                            if(!framerate.equalsIgnoreCase("SOURCE"))
                            {
                                int fps = 30;
                                
                                try
                                {
                                    fps = Math.round(Float.parseFloat(framerate));
                                }
                                catch (Exception ex) {}
                                
                                propVal += "gop=" + (fps * keyFrameInt) + ";";
                                
                                propVal += "fps=" + framerate + ";";
                            }
                            else
                            {
                                propVal += "fps=" + framerate + ";";
                            }
    
                            if(useBFrames)
                            {
                                propVal += "bframes=" + bframeInterval + ";";
                            }
    
                            propVal += "resolution=" + resolution + ";";
                            

                            if(!audioCodec.equalsIgnoreCase(""))
                            {
                                propVal += "audiocodec=" + audioCodec + ";";
                                propVal += "audiobitrate=" + audiobitrate + ";";
                                
                                if(!audiochannels.equalsIgnoreCase(""))
                                {
                                    propVal += "audiochannels=" + audiochannels + ";";
                                }
                            }
                        }
                        else
                        {
                            propVal = "";
                        }
                    }
                    else if ("FIXED_PUSH_REMUX_FORMAT".equals(propName))
                    {
                        //If we are using fixed streaming mode and
                        if ("fixed".equalsIgnoreCase(client.properties().getStreamingMode()))
                        {
                            if(!client.properties().getFixedRemuxingPreference().equalsIgnoreCase("off"))
                            {
                                propVal = "container=" + client.properties().getFixedRemuxingFormat() + ";videocodec=COPY;audiocodec=COPY;";
                            }
                        }
                    }
                    else if ("CRYPTO_ALGORITHMS".equals(propName))
                    {
                        propVal = client.getCryptoFormats();
                    }
                    else if ("CRYPTO_SYMMETRIC_KEY".equals(propName))
                    {
                        if (serverPublicKey != null && encryptedSecretKeyBytes == null)
                        {
                            if (currentCrypto.indexOf("RSA") != -1)
                            {
                                // We have to generate our secret key and then
                                // encrypt it with the server's public key
                                javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("Blowfish");
                                mySecretKey = keyGen.generateKey();
                                evtEncryptCipher = javax.crypto.Cipher.getInstance("Blowfish");
                                evtEncryptCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, mySecretKey);

                                byte[] rawSecretBytes = mySecretKey.getEncoded();
                                try
                                {
                                    javax.crypto.Cipher encryptCipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
                                    encryptCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, serverPublicKey);
                                    encryptedSecretKeyBytes = encryptCipher.doFinal(rawSecretBytes);
                                }
                                catch (Exception e)
                                {
                                    log.error("Error encrypting data to submit to server", e);
                                }
                            }
                            else
                            {
                                // We need to finish the DH key agreement and
                                // generate the shared secret key
                                /*
                                 * Bob gets the DH parameters associated with
								 * Alice's public key. He must use the same
								 * parameters when he generates his own key
								 * pair.
								 */
                                javax.crypto.spec.DHParameterSpec dhParamSpec = ((javax.crypto.interfaces.DHPublicKey) serverPublicKey).getParams();

                                // Bob creates his own DH key pair
                                log.debug("Generate DH keypair ...");
                                java.security.KeyPairGenerator bobKpairGen = java.security.KeyPairGenerator.getInstance("DH");
                                bobKpairGen.initialize(dhParamSpec);
                                java.security.KeyPair bobKpair = bobKpairGen.generateKeyPair();

                                // Bob creates and initializes his DH
                                // KeyAgreement object
                                javax.crypto.KeyAgreement bobKeyAgree = javax.crypto.KeyAgreement.getInstance("DH");
                                bobKeyAgree.init(bobKpair.getPrivate());

                                // Bob encodes his public key, and sends it over
                                // to Alice.
                                encryptedSecretKeyBytes = bobKpair.getPublic().getEncoded();

                                // We also have to generate the shared secret
                                // now
                                bobKeyAgree.doPhase(serverPublicKey, true);
                                mySecretKey = bobKeyAgree.generateSecret("DES");
                                evtEncryptCipher = javax.crypto.Cipher.getInstance("DES/ECB/PKCS5Padding");
                                evtEncryptCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, mySecretKey);
                            }
                        }
                        propValBytes = encryptedSecretKeyBytes;
                    }
                    else if ("GFX_SUPPORTED_RESOLUTIONS".equals(propName))
                    {
                        Dimension winny = myGfx.getScreenSize();
                        if (winny != null)
                        {
                            propVal = Integer.toString(winny.width) + "x" + Integer.toString(winny.height) + ";windowed";
                        }
                    }
                    else if ("GFX_FIXED_PAR".equals(propName))
                    {
                        // note: tels sagetv to go into iphone mode which enables httpls
                        if (client.properties().getBoolean(PrefStore.Keys.use_httpls, false))
                        {
                            propVal = "0.0";
                        }
                        else
                        {
                            propVal = "";
                        }
                        // propVal = "";
                    }
                    else if ("GFX_RESOLUTION".equals(propName))
                    {
                        Dimension winny = myGfx.getScreenSize();
                        if (winny != null)
                        {
                            propVal = Integer.toString(winny.width) + "x" + Integer.toString(winny.height);
                        }
                    }
                    else if ("GFX_DRAWMODE".equals(propName))
                    {
                        propVal = profileProperties.getProperty("GFX_DRAWMODE","FULLSCREEN");
                    }
                    else if ("VIDEO_ADVANCED_ASPECT".equals(propName))
                    {
                        if (client.options().isUsingAdvancedAspectModes())
                        {
                            propVal=client.options().getDefaultAdvancedAspectMode();
                        }
                        else
                        {
                            propVal="";
                        }
                    }
                    else if ("VIDEO_ADVANCED_ASPECT_LIST".equals(propName))
                    {
                        if (client.options().isUsingAdvancedAspectModes())
                        {
                            propVal=client.options().getAdvancedApectModes();
                        }
                        else
                        {
                            propVal="";
                        }
                    }

                    if (propVal==null||propVal.isEmpty() && profileProperties!=null)
                    {
                        propVal = profileProperties.getProperty(propName, propVal);
                    }

                    log.debug("GetProperty: {}='{}'", propName, propVal);

                    try
                    {
                        synchronized (eventChannel)
                        {
                            if (propValBytes == null)
                            {
                                propValBytes = propVal.getBytes(MiniClient.BYTE_CHARSET);
                            }

                            eventChannel.write(GET_PROPERTY_CMD_TYPE); // get
                            // property
                            // reply
                            eventChannel.write((propValBytes.length >> 16) & 0xFF);
                            eventChannel.write((propValBytes.length >> 8) & 0xFF);
                            eventChannel.write(propValBytes.length & 0xFF);// 3
                            // byte
                            // length
                            eventChannel.writeInt(0); // timestamp
                            eventChannel.writeInt(replyCount++);
                            eventChannel.writeInt(0); // pad
                            if (propValBytes.length > 0)
                            {
                                if (encryptEvents && evtEncryptCipher != null)
                                {
                                    eventChannel.write(evtEncryptCipher.doFinal(propValBytes));
                                }
                                else
                                {
                                    eventChannel.write(propValBytes);
                                }
                            }
                            eventChannel.flush();
                        }
                    }
                    catch (Exception e)
                    {
                        eventChannelError();
                    }
                }
                else if (command == SET_PROPERTY_CMD_TYPE) // set property
                {
                    short nameLen = (short) (((cmdbuffer[0] & 0xFF) << 8) | (cmdbuffer[1] & 0xFF));
                    short valLen = (short) (((cmdbuffer[2] & 0xFF) << 8) | (cmdbuffer[3] & 0xFF));
                    String propName = new String(cmdbuffer, 4, nameLen);
                    // String propVal = new String(cmdbuffer, 4 + nameLen,
                    // valLen);
                    String propVal = null;

                    synchronized (eventChannel)
                    {
                        boolean encryptThisReply = encryptEvents;

                        if ("CRYPTO_PUBLIC_KEY".equals(propName))
                        {
                            byte[] keyBytes = new byte[valLen];
                            System.arraycopy(cmdbuffer, 4 + nameLen, keyBytes, 0, valLen);
                            java.security.spec.X509EncodedKeySpec pubKeySpec = new java.security.spec.X509EncodedKeySpec(keyBytes);
                            java.security.KeyFactory keyFactory;
                            if (currentCrypto.indexOf("RSA") != -1)
                                keyFactory = java.security.KeyFactory.getInstance("RSA");
                            else
                                keyFactory = java.security.KeyFactory.getInstance("DH");
                            serverPublicKey = keyFactory.generatePublic(pubKeySpec);
                            retval = 0;
                        }
                        else if ("CRYPTO_ALGORITHMS".equals(propName))
                        {
                            currentCrypto = new String(cmdbuffer, 4 + nameLen, valLen);
                            propVal = currentCrypto;
                            retval = 0;
                        }
                        else if ("CRYPTO_EVENTS_ENABLE".equals(propName))
                        {
                            if ("TRUE".equalsIgnoreCase(new String(cmdbuffer, 4 + nameLen, valLen)))
                            {
                                if (evtEncryptCipher != null)
                                {
                                    encryptEvents = true;
                                    retval = 0;
                                }
                                else
                                {
                                    encryptEvents = false;
                                    retval = 1;
                                }
                            }
                            else
                            {
                                encryptEvents = false;
                                retval = 0;
                            }
                            log.debug("SageTVPlaceshifter event encryption is now={}", encryptEvents);
                        }
                        else if ("GFX_RESOLUTION".equals(propName))
                        {
                            propVal = new String(cmdbuffer, 4 + nameLen, valLen);
                            // NOTE: These resolution changes need to be done on
                            // the AWT thread because if we're disposing
                            // a window then that may invoke on AWT which could
                            // block against an event coming back in
                            if ("FULLSCREEN".equals(propVal))
                            {
                                uiRenderer.invokeLater(new Runnable()
                                {
                                    public void run() {
                                        myGfx.getWindow().setFullScreen(true);
                                    }
                                });
                            } else if ("WINDOW".equals(propVal))
                            {
                                uiRenderer.invokeLater(new Runnable()
                                {
                                    public void run() {
                                        myGfx.getWindow().setFullScreen(false);
                                    }
                                });
                            }
                            else
                            {
                                int xidx = propVal.indexOf('x');
                                if (xidx != -1)
                                {
                                    try
                                    {
                                        int w = Integer.parseInt(propVal.substring(0, xidx));
                                        int h = Integer.parseInt(propVal.substring(xidx + 1));
                                        myGfx.getWindow().setSize(w, h);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            retval = 0;
                        }
                        else if ("GFX_FONTSERVER".equals(propName))
                        {
                            if ("TRUE".equalsIgnoreCase(new String(cmdbuffer, 4 + nameLen, valLen)))
                            {
                                fontServer = true;
                                retval = 0;
                            }
                            else
                            {
                                fontServer = false;
                                retval = 0;
                            }
                            propVal = String.valueOf(fontServer);
                        }
                        else if ("ZLIB_COMM_XFER".equals(propName))
                        {
                            zipMode = "TRUE".equalsIgnoreCase(new String(cmdbuffer, 4 + nameLen, valLen));
                            propVal = String.valueOf(zipMode);
                            retval = 0;
                        }
                        else if ("ADVANCED_IMAGE_CACHING".equals(propName))
                        {
                            usesAdvancedImageCaching = "TRUE".equalsIgnoreCase(new String(cmdbuffer, 4 + nameLen, valLen));
                            propVal = String.valueOf(usesAdvancedImageCaching);
                            retval = 0;
                        }
                        else if ("RECONNECT_SUPPORTED".equals(propName))
                        {
                            reconnectAllowed = "TRUE".equalsIgnoreCase(new String(cmdbuffer, 4 + nameLen, valLen));
                            propVal = String.valueOf(reconnectAllowed);
                            retval = 0;
                        }
                        else if ("SUBTITLES_CALLBACKS".equals(propName))
                        {
                            subSupport = "TRUE".equalsIgnoreCase(new String(cmdbuffer, 4 + nameLen, valLen));
                            propVal = String.valueOf(subSupport);
                            retval = 0;
                        }
                        else if ("MENU_HINT".equals(propName))
                        {
                            propVal = new String(cmdbuffer, 4 + nameLen, valLen);
                            menuHint.update(propVal);
                            log.debug("Setting MENU_HINT: {}", menuHint);
                            if (getUiRenderer() != null)
                            {
                                getUiRenderer().onMenuHint(menuHint);
                            }
                            retval = 0;
                        }
                        else if ("GFX_ASPECT".equals(propName))
                        {
                            propVal = new String(cmdbuffer, 4 + nameLen, valLen);
                            try
                            {
                                getUiRenderer().setUIAspectRatio(Float.parseFloat(propVal));
                            }
                            catch (Throwable t)
                            {
                                log.error("Failed to set UI ASPECT of " + propVal, t);
                            }
                            retval = 0;
                        }
                        else if ("VIDEO_ADVANCED_ASPECT".equals(propName))
                        {
                            propVal = new String(cmdbuffer, 4 + nameLen, valLen);
                            retval = 0;
                            if (uiRenderer!=null)
                            {
                                uiRenderer.setVideoAdvancedAspect(propVal);
                            }
                        }
                        else if ("SET_CACHED_AUTH".equals(propName))
                        {
                            // Save this authentication block in the properties
                            // file
                            // First we need to decrypt it with the symmetric
                            // key
                            if (evtEncryptCipher != null && msi != null)
                            {
                                javax.crypto.Cipher decryptCipher = javax.crypto.Cipher.getInstance(evtEncryptCipher.getAlgorithm());
                                decryptCipher.init(javax.crypto.Cipher.DECRYPT_MODE, mySecretKey);
                                String newAuth = new String(decryptCipher.doFinal(cmdbuffer, 4 + nameLen, valLen));

                                log.debug("SET_CACHED_AUTH: " + newAuth);

                                
                                if (msi != null)
                                {
                                    msi.setAuthBlock(newAuth);
                                    msi.save(client.properties());
                                }
                            }
                            retval = 0;
                        }
                        else
                        {
                            retval = 0; // or the error code if it failed the
                        }

                        // set
                        retbuf[0] = (byte) ((retval >> 24) & 0xFF);
                        retbuf[1] = (byte) ((retval >> 16) & 0xFF);
                        retbuf[2] = (byte) ((retval >> 8) & 0xFF);
                        retbuf[3] = (byte) ((retval >> 0) & 0xFF);
                        try
                        {
                            eventChannel.write(SET_PROPERTY_CMD_TYPE); // set
                            // property
                            // reply
                            eventChannel.write(0);
                            eventChannel.writeShort(4);// 3 byte length of 4
                            eventChannel.writeInt(0); // timestamp
                            eventChannel.writeInt(replyCount++);
                            eventChannel.writeInt(0); // pad
                            if (encryptThisReply)
                            {
                                eventChannel.write(evtEncryptCipher.doFinal(retbuf, 0, 4));
                            }
                            else
                            {
                                eventChannel.write(retbuf, 0, 4);
                            }
                            eventChannel.flush();
                        }
                        catch (Exception e)
                        {
                            eventChannelError();
                        }
                    }
                    log.debug("SetProperty {}={}", propName, (propVal == null) ? "(WAS_NULL)" : propVal);

                }
                else if (command == FS_CMD_TYPE)
                {
                    command = (cmdbuffer[0] & 0xFF);
                    processFSCmd(command, len, cmdbuffer);
                }

                // Remove whatever we just processed
                synchronized (gfxSyncVector)
                {
                    gfxSyncVector.remove(0);
                    gfxSyncVector.remove(0);
                    gfxSyncVector.notifyAll();
                }
            }
        }
        catch (Throwable e)
        {
            log.error("Error w/ GFX Thread", e);
        }
        finally
        {
            try
            {
                gfxIs.close();
            }
            catch (Exception e) { }

            try
            {
                eventChannel.close();
            }
            catch (Exception e) { }

            try
            {
                gfxSocket.close();
            }
            catch (Exception e) { }

            if (alive)
            {
                connectionError();
            }
        }
    }

    private String toStringList(List<String> list)
    {
        StringBuilder sb = new StringBuilder();
        for (String s: list)
        {
            if (sb.length()>0)
            {
                sb.append(",");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public void recvCommand(int sageCommandID)
    {
        postSageCommandEvent(sageCommandID);
    }

    public void recvCommand(int sageCommandID, String payload) {
        postSageCommandEvent(sageCommandID);
    }

    public void recvCommand(int sageCommandID, String[] payloads) {
        postSageCommandEvent(sageCommandID);
    }

    public void recvInfrared(byte[] irCode) {
        int coded = 0;
        for (int i = 0; i < irCode.length; i += 4) {
            int currCoded = (irCode[i] & 0xFF) << 24;
            if (i + 1 < irCode.length)
                currCoded |= (irCode[i + 1] & 0xFF) << 16;
            if (i + 2 < irCode.length)
                currCoded |= (irCode[i + 2] & 0xFF) << 8;
            if (i + 3 < irCode.length)
                currCoded |= (irCode[i + 3] & 0xFF);
            coded = coded ^ currCoded;
        }
        postIREvent(coded);
    }

    public void recvKeystroke(char keyChar, int keyCode, int keyModifiers) {
        postKeyEvent(keyCode, keyModifiers, keyChar);
    }

    public void postIREvent(final int IRCode) {
//        if (MiniClient.irKillCode != null && MiniClient.irKillCode.intValue() == IRCode) {
//            System.out.println("IR Exit Code received...terminating");
//            close();
//            return;
//        }
        // if (myGfx != null)
        // myGfx.setHidden(false, false);
        // MiniClientPowerManagement.getInstance().kick();
        if (performingReconnect)
            return;

        if (eventRouterThread == null || eventRouterThread.queue == null) {
            return;
        }

        eventRouterThread.queue.add(new Runnable() {
            @Override
            public void run() {
                synchronized (eventChannel) {
                    try {
                        eventChannel.write(IR_EVENT_REPLY_TYPE); // ir event code
                        eventChannel.write(0);
                        eventChannel.writeShort(4);// 3 byte length of 4
                        eventChannel.writeInt(0); // timestamp
                        eventChannel.writeInt(replyCount++);
                        eventChannel.writeInt(0); // pad
                        if (encryptEvents && evtEncryptCipher != null) {
                            byte[] data = new byte[4];
                            data[0] = (byte) ((IRCode >> 24) & 0xFF);
                            data[1] = (byte) ((IRCode >> 16) & 0xFF);
                            data[2] = (byte) ((IRCode >> 8) & 0xFF);
                            data[3] = (byte) (IRCode & 0xFF);
                            eventChannel.write(evtEncryptCipher.doFinal(data));
                        } else {
                            eventChannel.writeInt(IRCode);
                        }
                        eventChannel.flush();
                    } catch (Exception e) {
                        log.error("Error w/ event thread", e);
                        eventChannelError();
                    }
                }
            }
        });
    }

    public void postSageCommandEvent(final int sageCommand) {
        // if (myGfx != null)
        // myGfx.setHidden(false, false);
        // MiniClientPowerManagement.getInstance().kick();
        if (performingReconnect)
            return;

        if (eventRouterThread == null || eventRouterThread.queue == null) {
            return;
        }

        eventRouterThread.queue.add(new Runnable() {
            @Override
            public void run() {
                log.debug("Begin Sending SageTV Command {}", sageCommand);
                synchronized (eventChannel) {
                    try {
                        eventChannel.write(136); // SageTV Command event code
                        eventChannel.write(0);
                        eventChannel.writeShort(4);// 3 byte length of 4
                        eventChannel.writeInt(0); // timestamp
                        eventChannel.writeInt(replyCount++);
                        eventChannel.writeInt(0); // pad
                        if (encryptEvents && evtEncryptCipher != null) {
                            byte[] data = new byte[4];
                            data[0] = (byte) ((sageCommand >> 24) & 0xFF);
                            data[1] = (byte) ((sageCommand >> 16) & 0xFF);
                            data[2] = (byte) ((sageCommand >> 8) & 0xFF);
                            data[3] = (byte) (sageCommand & 0xFF);
                            eventChannel.write(evtEncryptCipher.doFinal(data));
                        } else {
                            eventChannel.writeInt(sageCommand);
                        }
                        eventChannel.flush();
                    } catch (Exception e) {
                        log.error("Error w/ event thread", e);
                        eventChannelError();
                    }
                }
            }
        });
    }

    public void postKeyEvent(final int keyCode, final int keyModifiers, final char keyChar) {
        // MiniClientPowerManagement.getInstance().kick();
        if (performingReconnect)
            return;

        if (eventRouterThread == null || eventRouterThread.queue == null) {
            return;
        }

        eventRouterThread.queue.add(new Runnable() {
            @Override
            public void run() {
                synchronized (eventChannel) {
                    try {
                        eventChannel.write(KB_EVENT_REPLY_TYPE); // kb event code
                        eventChannel.write(0);
                        eventChannel.writeShort(10);// 3 byte length of 10
                        eventChannel.writeInt(0); // timestamp
                        eventChannel.writeInt(replyCount++);
                        eventChannel.writeInt(0); // pad
                        if (encryptEvents && evtEncryptCipher != null) {
                            byte[] data = new byte[10];
                            data[0] = (byte) ((keyCode >> 24) & 0xFF);
                            data[1] = (byte) ((keyCode >> 16) & 0xFF);
                            data[2] = (byte) ((keyCode >> 8) & 0xFF);
                            data[3] = (byte) (keyCode & 0xFF);
                            data[4] = (byte) ((keyChar >> 8) & 0xFF);
                            data[5] = (byte) (keyChar & 0xFF);
                            data[6] = (byte) ((keyModifiers >> 24) & 0xFF);
                            data[7] = (byte) ((keyModifiers >> 16) & 0xFF);
                            data[8] = (byte) ((keyModifiers >> 8) & 0xFF);
                            data[9] = (byte) (keyModifiers & 0xFF);
                            eventChannel.write(evtEncryptCipher.doFinal(data));
                        } else {
                            eventChannel.writeInt(keyCode);
                            eventChannel.writeChar(keyChar);
                            eventChannel.writeInt(keyModifiers);
                        }
                        eventChannel.flush();
                    } catch (Throwable e) {
                        log.error("Error w/ event thread", e);
                        eventChannelError();
                    }
                }
            }
        });
    }

    public boolean hasEventChannel() {
        return eventChannel != null;
    }

    public void postResizeEvent(Dimension size) {
        if (performingReconnect)
            return;

        if (eventChannel == null) {
            return;
        }

        // NOTE: not sure this needs to use the eventRouterThread... resize events normally happen
        // in the background thread, so I think we are safe to leave this

        synchronized (eventChannel) {
            try {
                eventChannel.write(UI_RESIZE_EVENT_REPLY_TYPE); // resize event
                // code
                eventChannel.write(0);
                eventChannel.writeShort(8);// 3 byte length of 8
                eventChannel.writeInt(0); // timestamp
                eventChannel.writeInt(replyCount++);
                eventChannel.writeInt(0); // pad
                if (encryptEvents && evtEncryptCipher != null) {
                    byte[] data = new byte[8];
                    data[0] = (byte) ((size.width >> 24) & 0xFF);
                    data[1] = (byte) ((size.width >> 16) & 0xFF);
                    data[2] = (byte) ((size.width >> 8) & 0xFF);
                    data[3] = (byte) (size.width & 0xFF);
                    data[4] = (byte) ((size.height >> 24) & 0xFF);
                    data[5] = (byte) ((size.height >> 16) & 0xFF);
                    data[6] = (byte) ((size.height >> 8) & 0xFF);
                    data[7] = (byte) (size.height & 0xFF);
                    eventChannel.write(evtEncryptCipher.doFinal(data));
                } else {
                    eventChannel.writeInt(size.width);
                    eventChannel.writeInt(size.height);
                }
                eventChannel.flush();
            } catch (Exception e) {
                log.error("Error w/ event thread", e);
                eventChannelError();
            }
        }
    }

    public void postRepaintEvent(int x, int y, int w, int h) {
        if (performingReconnect)
            return;

        if (eventChannel == null) {
            return;
        }

        // We should be good to NOT use the eventRouterThread, since repaints happen in the background
        // thread, usually.

        synchronized (eventChannel) {
            try {
                eventChannel.write(UI_REPAINT_EVENT_REPLY_TYPE); // repaint
                // event
                // code
                eventChannel.write(0);
                eventChannel.writeShort(16);// 3 byte length of 16
                eventChannel.writeInt(0); // timestamp
                eventChannel.writeInt(replyCount++);
                eventChannel.writeInt(0); // pad
                if (encryptEvents && evtEncryptCipher != null) {
                    byte[] data = new byte[16];
                    data[0] = (byte) ((x >> 24) & 0xFF);
                    data[1] = (byte) ((x >> 16) & 0xFF);
                    data[2] = (byte) ((x >> 8) & 0xFF);
                    data[3] = (byte) (x & 0xFF);
                    data[4] = (byte) ((y >> 24) & 0xFF);
                    data[5] = (byte) ((y >> 16) & 0xFF);
                    data[6] = (byte) ((y >> 8) & 0xFF);
                    data[7] = (byte) (y & 0xFF);
                    data[8] = (byte) ((w >> 24) & 0xFF);
                    data[9] = (byte) ((w >> 16) & 0xFF);
                    data[10] = (byte) ((w >> 8) & 0xFF);
                    data[11] = (byte) (w & 0xFF);
                    data[12] = (byte) ((h >> 24) & 0xFF);
                    data[13] = (byte) ((h >> 16) & 0xFF);
                    data[14] = (byte) ((h >> 8) & 0xFF);
                    data[15] = (byte) (h & 0xFF);
                    eventChannel.write(evtEncryptCipher.doFinal(data));
                } else {
                    eventChannel.writeInt(x);
                    eventChannel.writeInt(y);
                    eventChannel.writeInt(w);
                    eventChannel.writeInt(h);
                }
                eventChannel.flush();
            } catch (Throwable e) {
                log.error("Error w/ event thread", e);
                eventChannelError();
            }
        }
    }

    public void postImageUnload(int handle) {
        if (performingReconnect)
            return;

        if (eventChannel==null) return;

        synchronized (eventChannel) {

            try {
                eventChannel.write(IMAGE_UNLOAD_REPLY_TYPE); // repaint event
                // code
                eventChannel.write(0);
                eventChannel.writeShort(4);// 3 byte length of 16
                eventChannel.writeInt(0); // timestamp
                eventChannel.writeInt(replyCount++);
                eventChannel.writeInt(0); // pad
                if (encryptEvents && evtEncryptCipher != null) {
                    byte[] data = new byte[4];
                    data[0] = (byte) ((handle >> 24) & 0xFF);
                    data[1] = (byte) ((handle >> 16) & 0xFF);
                    data[2] = (byte) ((handle >> 8) & 0xFF);
                    data[3] = (byte) (handle & 0xFF);
                    eventChannel.write(evtEncryptCipher.doFinal(data));
                } else {
                    eventChannel.writeInt(handle);
                }
                eventChannel.flush();
            } catch (Exception e) {
                log.error("Error w/ event thread", e);
                eventChannelError();
            }
        }
    }

    public void postOfflineCacheChange(boolean addedToCache, String rezID) {
        if (performingReconnect)
            return;

        if (eventChannel==null) return;

        synchronized (eventChannel) {
            try {
                int strlen = rezID.length();
                eventChannel.write(OFFLINE_CACHE_CHANGE_REPLY_TYPE); // repaint
                // event
                // code
                eventChannel.write(0);
                eventChannel.writeShort(5 + strlen);// 3 byte length
                eventChannel.writeInt(0); // timestamp
                eventChannel.writeInt(replyCount++);
                eventChannel.writeInt(0); // pad
                byte[] strBytes = rezID.getBytes(MiniClient.BYTE_CHARSET);
                if (encryptEvents && evtEncryptCipher != null) {
                    byte[] data = new byte[5 + strlen];
                    data[0] = (byte) (addedToCache ? 1 : 0);
                    data[1] = (byte) ((strlen >> 24) & 0xFF);
                    data[2] = (byte) ((strlen >> 16) & 0xFF);
                    data[3] = (byte) ((strlen >> 8) & 0xFF);
                    data[4] = (byte) (strlen & 0xFF);
                    System.arraycopy(strBytes, 0, data, 5, strBytes.length);
                    eventChannel.write(evtEncryptCipher.doFinal(data));
                } else {
                    eventChannel.writeByte(addedToCache ? 1 : 0);
                    eventChannel.writeInt(strlen);
                    eventChannel.write(strBytes);
                }
                eventChannel.flush();
            } catch (Exception e) {
                log.error("Error w/ event thread", e);
                eventChannelError();
            }
        }
    }

    public void postMouseEvent(final MouseEvent evt) {
        // MiniClientPowerManagement.getInstance().kick();
        if (performingReconnect)
            return;

        if (eventRouterThread==null || eventRouterThread.queue == null) {
            // ignore this, since we haven't fully started up yet.
            return;
        }

        eventRouterThread.queue.add(new Runnable() {
            @Override
            public void run() {
                synchronized (eventChannel) {
                    try {
                        if (evt.getID() == MouseEvent.MOUSE_CLICKED)
                            eventChannel.write(MCLICK_EVENT_REPLY_TYPE); // mouse click
                            // event
                            // code
                        else if (evt.getID() == MouseEvent.MOUSE_PRESSED)
                            eventChannel.write(MPRESS_EVENT_REPLY_TYPE); // mouse press
                            // event
                            // code
                        else if (evt.getID() == MouseEvent.MOUSE_RELEASED)
                            eventChannel.write(MRELEASE_EVENT_REPLY_TYPE); // mouse
                            // release
                            // event
                            // code
                        else if (evt.getID() == MouseEvent.MOUSE_DRAGGED)
                            eventChannel.write(MDRAG_EVENT_REPLY_TYPE); // mouse drag
                            // event code
                        else if (evt.getID() == MouseEvent.MOUSE_MOVED)
                            eventChannel.write(MMOVE_EVENT_REPLY_TYPE); // mouse move
                            // event code
                        else if (evt.getID() == MouseEvent.MOUSE_WHEEL)
                            eventChannel.write(MWHEEL_EVENT_REPLY_TYPE); // mouse wheel
                            // event
                            // code
                        else
                            return;
                        eventChannel.write(0);
                        eventChannel.writeShort(14);// 3 byte length of 14
                        eventChannel.writeInt(0); // timestamp
                        eventChannel.writeInt(replyCount++);
                        eventChannel.writeInt(0); // pad
                        if (encryptEvents && evtEncryptCipher != null) {
                            byte[] data = new byte[14];
                            data[0] = (byte) ((evt.getX() >> 24) & 0xFF);
                            data[1] = (byte) ((evt.getX() >> 16) & 0xFF);
                            data[2] = (byte) ((evt.getX() >> 8) & 0xFF);
                            data[3] = (byte) (evt.getX() & 0xFF);
                            data[4] = (byte) ((evt.getY() >> 24) & 0xFF);
                            data[5] = (byte) ((evt.getY() >> 16) & 0xFF);
                            data[6] = (byte) ((evt.getY() >> 8) & 0xFF);
                            data[7] = (byte) (evt.getY() & 0xFF);
                            data[8] = (byte) ((evt.getModifiers() >> 24) & 0xFF);
                            data[9] = (byte) ((evt.getModifiers() >> 16) & 0xFF);
                            data[10] = (byte) ((evt.getModifiers() >> 8) & 0xFF);
                            data[11] = (byte) (evt.getModifiers() & 0xFF);
                            if (evt.getID() == MouseEvent.MOUSE_WHEEL)
                                data[12] = (byte) (evt.getWheelRotation());
                            else
                                data[12] = (byte) evt.getClickCount();
                            data[13] = (byte) evt.getButton();
                            eventChannel.write(evtEncryptCipher.doFinal(data));
                        } else {
                            eventChannel.writeInt(evt.getX());
                            eventChannel.writeInt(evt.getY());
                            eventChannel.writeInt(evt.getModifiers());
                            if (evt.getID() == MouseEvent.MOUSE_WHEEL)
                                eventChannel.write(evt.getWheelRotation());
                            else
                                eventChannel.write(evt.getClickCount());
                            eventChannel.write(evt.getButton());
                        }
                        eventChannel.flush();
                    } catch (Throwable e) {
                        log.error("Error w/ event thread", e);
                        eventChannelError();
                    }
                }
            }
        });

    }

    public void postMediaPlayerUpdateEvent() {
        if (performingReconnect)
            return;
        synchronized (eventChannel) {
            try {
                eventChannel.write(MEDIA_PLAYER_UPDATE_EVENT_REPLY_TYPE); // media
                // player
                // update
                // event
                // code
                eventChannel.write(0);
                eventChannel.writeShort(0);// 3 byte length of 0
                eventChannel.writeInt(0); // timestamp
                eventChannel.writeInt(replyCount++);
                eventChannel.writeInt(0); // pad
                eventChannel.flush();
            } catch (Exception e) {
                log.error("Error w/ event thread", e);
                eventChannelError();
            }
        }
    }

    public void postSubtitleInfo(long pts, long duration, byte[] data, int flags) {
        if (!subSupport)
            return; // don't send events if the other end doesn't support it
        if (performingReconnect)
            return;
        synchronized (eventChannel) {
            try {
                eventChannel.write(SUBTITLE_UPDATE_REPLY_TYPE); // subtitle
                // update event
                // code
                eventChannel.write(0);
                eventChannel.writeShort((short) (14 + ((data == null) ? 0 : data.length)));// 3
                // byte
                // length
                // of
                // 0
                eventChannel.writeInt(0); // timestamp
                eventChannel.writeInt(replyCount++);
                eventChannel.writeInt(0); // pad
                eventChannel.writeInt(flags);
                eventChannel.writeInt((int) pts);
                eventChannel.writeInt((int) duration);
                if (data != null) {
                    eventChannel.writeShort((short) data.length);
                    eventChannel.write(data);
                } else
                    eventChannel.writeShort(0);
                eventChannel.flush();
            } catch (Exception e) {
                log.error("Error w/ event thread", e);
                eventChannelError();
            }
        }
    }

    public void postHotplugEvent(boolean insertion, String devPath, String devDesc) {
        if (eventChannel == null || fsSecurity == HIGH_SECURITY_FS)
            return;
        if (performingReconnect)
            return;
        synchronized (eventChannel) {
            if (encryptEvents && evtEncryptCipher != null) {
                // can't do this while encrypted'
                return;
            }
            try {
                eventChannel
                        .write(insertion ? REMOTE_FS_HOTPLUG_INSERT_EVENT_REPLY_TYPE : REMOTE_FS_HOTPLUG_REMOVE_EVENT_REPLY_TYPE);
                eventChannel.write(0);
                eventChannel.writeShort(4 + devPath.length() + devDesc.length());// 3
                // byte
                // length
                // of
                // the
                // 2
                // strings
                // +
                // count
                eventChannel.writeInt(0); // timestamp
                eventChannel.writeInt(replyCount++);
                eventChannel.writeInt(0); // pad
                eventChannel.writeShort(devPath.length());
                eventChannel.write(devPath.getBytes());
                eventChannel.writeShort(devDesc.length());
                eventChannel.write(devDesc.getBytes());
                eventChannel.flush();
            } catch (Exception e) {
                log.error("Error w/ event thread", e);
                eventChannelError();
            }
        }
    }

    private void processFSCmd(int cmdType, int len, byte[] cmdData) throws java.io.IOException {
        if (encryptEvents && evtEncryptCipher != null) {
            // can't do this while encrypted'
            return;
        }
        // Filesystem commands should not even be seen in this security mode
        if (fsSecurity == HIGH_SECURITY_FS)
            return;
        // System.out.println("MiniClient processing FS Command: " + cmdType);
        byte[][] strRv = null;
        long longRv = 0;
        int intRv = 0;
        boolean isLongRv = false;
        String pathName;
        java.io.File theFile;
        switch (cmdType) {
            case FSCMD_CREATE_DIRECTORY:
                theFile = new java.io.File(getCmdString(cmdData, 4));
                // Check security
                if (fsSecurity == MED_SECURITY_FS && !theFile.isDirectory()) {
//				if (javax.swing.JOptionPane.showConfirmDialog(null,
//						"<html>Would you like to allow the server to create the local directory:<br>" + theFile + "</html>",
//						"File System Security", javax.swing.JOptionPane.YES_NO_OPTION,
//						javax.swing.JOptionPane.WARNING_MESSAGE) != javax.swing.JOptionPane.YES_OPTION) {
//					intRv = FS_RV_NO_PERMISSIONS;
//				}
                }
                if (intRv == 0)
                    intRv = (theFile.isDirectory() || theFile.mkdirs()) ? FS_RV_SUCCESS : FS_RV_ERROR_UNKNOWN;
                break;
            case FSCMD_GET_FILE_SIZE:
                isLongRv = true;
                longRv = new java.io.File(getCmdString(cmdData, 4)).length();
                break;
            case FSCMD_DELETE_FILE:
                theFile = new java.io.File(getCmdString(cmdData, 4));
                if (!theFile.exists())
                    intRv = FS_RV_PATH_DOES_NOT_EXIST;
                else {
                    // Check security
                    if (fsSecurity == MED_SECURITY_FS) {
//					if (javax.swing.JOptionPane.showConfirmDialog(null,
//							"<html>Would you like to allow the server to delete the local file:<br>" + theFile + "</html>",
//							"File System Security", javax.swing.JOptionPane.YES_NO_OPTION,
//							javax.swing.JOptionPane.WARNING_MESSAGE) != javax.swing.JOptionPane.YES_OPTION) {
//						intRv = FS_RV_NO_PERMISSIONS;
//					}
                    }
                    if (intRv == 0 && !theFile.delete())
                        intRv = FS_RV_ERROR_UNKNOWN;
                }
                break;
            case FSCMD_GET_PATH_ATTRIBUTES:
                theFile = new java.io.File(getCmdString(cmdData, 4));
                if (theFile.isHidden())
                    intRv = intRv | FS_PATH_HIDDEN;
                if (theFile.isFile())
                    intRv = intRv | FS_PATH_FILE;
                if (theFile.isDirectory())
                    intRv = intRv | FS_PATH_DIRECTORY;
                break;
            case FSCMD_GET_PATH_MODIFIED_TIME:
                isLongRv = true;
                longRv = new java.io.File(getCmdString(cmdData, 4)).lastModified();
                break;
            case FSCMD_DIR_LIST:
                theFile = new java.io.File(getCmdString(cmdData, 4));
                String[] list = theFile.list();
                strRv = new byte[(list == null) ? 0 : list.length][];
                for (int i = 0; i < strRv.length; i++)
                    strRv[i] = list[i].getBytes("UTF-8");
                break;
            case FSCMD_LIST_ROOTS:
                java.io.File[] rootFiles = java.io.File.listRoots();
                strRv = new byte[(rootFiles == null) ? 0 : rootFiles.length][];
                for (int i = 0; i < strRv.length; i++)
                    strRv[i] = rootFiles[i].toString().getBytes("UTF-8");
                break;
            case FSCMD_DOWNLOAD_FILE:
            case FSCMD_UPLOAD_FILE:
                int secureID = ((cmdData[4] & 0xFF) << 24) | ((cmdData[5] & 0xFF) << 16) | ((cmdData[6] & 0xFF) << 8)
                        | (cmdData[7] & 0xFF);
                long fileOffset = getCmdLong(cmdData, 8);
                long fileSize = getCmdLong(cmdData, 16);
                pathName = getCmdString(cmdData, 24);
                theFile = new java.io.File(pathName);
                if (cmdType == FSCMD_DOWNLOAD_FILE) {
                    // Make sure we're downloading to a valid file that we can write
                    // to
                    if (theFile.exists() && !theFile.canWrite())
                        intRv = FS_RV_NO_PERMISSIONS;
                    else if (theFile.getParentFile() != null && !theFile.getParentFile().isDirectory())
                        intRv = FS_RV_PATH_DOES_NOT_EXIST;
                    else {
                        // Check security
                        if (fsSecurity == MED_SECURITY_FS) {
//						if (javax.swing.JOptionPane.showConfirmDialog(null,
//								"<html>Would you like to allow the server to download to the local file:<br>" + theFile + "</html>",
//								"File System Security", javax.swing.JOptionPane.YES_NO_OPTION,
//								javax.swing.JOptionPane.WARNING_MESSAGE) != javax.swing.JOptionPane.YES_OPTION) {
//							intRv = FS_RV_NO_PERMISSIONS;
//						}
                        }
                        if (intRv == 0) {
                            // Try to create the pathname
                            try {
                                if (!theFile.createNewFile())
                                    intRv = FS_RV_NO_PERMISSIONS;
                            } catch (java.io.IOException e) {
                                intRv = FS_RV_NO_PERMISSIONS;
                            }
                        }
                    }
                } else {
                    // It's an upload; make sure the file is there and can be read
                    if (!theFile.exists())
                        intRv = FS_RV_PATH_DOES_NOT_EXIST;
                    else if (!theFile.canRead())
                        intRv = FS_RV_NO_PERMISSIONS;
                    else if (fsSecurity == MED_SECURITY_FS) {
//					if (javax.swing.JOptionPane.showConfirmDialog(null,
//							"<html>Would you like to allow the server to upload from the local file:<br>" + theFile + "</html>",
//							"File System Security", javax.swing.JOptionPane.YES_NO_OPTION,
//							javax.swing.JOptionPane.WARNING_MESSAGE) != javax.swing.JOptionPane.YES_OPTION) {
//						intRv = FS_RV_NO_PERMISSIONS;
//					}
                    }
                }
                if (intRv == 0) {
                    intRv = startAsyncFSOperation(cmdType == FSCMD_DOWNLOAD_FILE, secureID, fileOffset, fileSize, theFile);
                }
                break;
        }
        synchronized (eventChannel) {
            eventChannel.write(FS_CMD_TYPE);
            eventChannel.write(0);
            if (strRv != null) {
                // System.out.println("MiniClient returning file list of size "
                // + strRv.length);
                // find the total length
                int totalLen = 0;
                for (int i = 0; i < strRv.length; i++)
                    totalLen += strRv[i].length + 2;
                eventChannel.writeShort(totalLen + 2);// 3 byte length of 0
            } else if (isLongRv) {
                // System.out.println("MiniClient returning 64-bit FS RV of " +
                // longRv);
                eventChannel.writeShort(8);
            } else {
                // System.out.println("MiniClient returning 32-bit FS RV of " +
                // intRv);
                eventChannel.writeShort(4);
            }
            eventChannel.writeInt(0); // timestamp
            eventChannel.writeInt(replyCount++);
            eventChannel.writeInt(0); // pad
            if (strRv != null) {
                eventChannel.writeShort(strRv.length);
                for (int i = 0; i < strRv.length; i++) {
                    eventChannel.writeShort(strRv[i].length);
                    eventChannel.write(strRv[i]);
                }
            } else if (isLongRv)
                eventChannel.writeLong(longRv);
            else
                eventChannel.writeInt(intRv);
            eventChannel.flush();
        }
    }

    // Connects back to the server to initiate a remote FS operation; returns 0
    // if this starts up OK
    private int startAsyncFSOperation(boolean download, int secureID, long fileOffset, long fileSize, java.io.File theFile) {
        log.debug("Attempting to connect bak to server on FS channel");
        java.net.Socket sake = null;
        java.io.OutputStream fsOut = null;
        java.io.InputStream fsIn = null;
        java.io.RandomAccessFile raf = null;
        try {
            sake = EstablishServerConnection(2);
            sake.setSoTimeout(30000);
            fsOut = sake.getOutputStream();
            fsIn = sake.getInputStream();
            byte[] secureBytes = new byte[4];
            secureBytes[0] = (byte) ((secureID >> 24) & 0xFF);
            secureBytes[1] = (byte) ((secureID >> 16) & 0xFF);
            secureBytes[2] = (byte) ((secureID >> 8) & 0xFF);
            secureBytes[3] = (byte) (secureID & 0xFF);
            fsOut.write(secureBytes);
            raf = new java.io.RandomAccessFile(theFile, download ? "rw" : "r");
            if (fileOffset > 0)
                raf.seek(fileOffset);
            // Now start the real async operation
            asyncFSXfer(download, sake, fsOut, fsIn, fileOffset, fileSize, raf);
        } catch (java.io.IOException e) {
            if (raf != null)
                try {
                    raf.close();
                } catch (Exception e1) {
                }
            if (sake != null)
                try {
                    sake.close();
                } catch (Exception e1) {
                }
            if (fsOut != null)
                try {
                    fsOut.close();
                } catch (Exception e1) {
                }
            if (fsIn != null)
                try {
                    fsIn.close();
                } catch (Exception e1) {
                }
            return FS_RV_ERROR_UNKNOWN;
        }
        return FS_RV_SUCCESS;
    }

    private void asyncFSXfer(final boolean download, final java.net.Socket sake, final java.io.OutputStream fsOut,
                             final java.io.InputStream fsIn, final long fileOffset, final long fileSize, final java.io.RandomAccessFile localFile) {
        Thread t = new Thread() {
            public void run() {
                byte[] fsBuffer = new byte[16384];
                try {
                    long xferSize = fileSize;
                    while (xferSize > 0) {
                        int currSize = (int) Math.min(xferSize, fsBuffer.length);
                        if (!download) {
                            localFile.readFully(fsBuffer, 0, currSize);
                            fsOut.write(fsBuffer, 0, currSize);
                        } else {
                            currSize = fsIn.read(fsBuffer, 0, currSize);
                            if (currSize < 0)
                                throw new java.io.EOFException();
                            localFile.write(fsBuffer, 0, currSize);
                        }
                        xferSize -= currSize;
                        // System.out.println("xferSize rem " + xferSize);
                    }
                    if (!download)
                        fsOut.flush();
                    else {
                        localFile.close();
                        fsOut.write(0);
                        fsOut.write(0);
                        fsOut.write(0);
                        fsOut.write(0);
                    }
                    log.debug("Finished Remote FS operation!");
                } catch (Exception e) {
                    log.error("ERROR w/ remote FS operation", e);
                } finally {
                    if (sake != null)
                        try {
                            sake.close();
                        } catch (Exception e) {
                        }
                    if (fsOut != null)
                        try {
                            fsOut.close();
                        } catch (Exception e) {
                        }
                    if (fsIn != null)
                        try {
                            fsIn.close();
                        } catch (Exception e) {
                        }
                    if (localFile != null)
                        try {
                            localFile.close();
                        } catch (Exception e) {
                        }
                }
            }
        };
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    private void MediaThread() {
        byte[] cmdbuffer = new byte[65536];

        java.io.OutputStream os = null;
        java.io.DataInputStream is = null;
        while (alive) {
            myMedia = new MediaCmd(client);
            try {
                os = mediaSocket.getOutputStream();
                is = new java.io.DataInputStream(mediaSocket.getInputStream());
                while (alive) {
                    byte[] cmd = new byte[4];
                    int command, len;
                    int retval;
                    byte[] retbuf = new byte[16];
                    is.readFully(cmd);

                    command = (cmd[0] & 0xFF);
                    len = ((cmd[1] & 0xFF) << 16) | ((cmd[2] & 0xFF) << 8) | (cmd[3] & 0xFF);
                    if (cmdbuffer.length < len) {
                        cmdbuffer = new byte[len];
                    }
                    is.readFully(cmdbuffer, 0, len);

                    retval = myMedia.ExecuteMediaCommand(command, len, cmdbuffer, retbuf);

                    if (retval > 0) {
                        os.write(retbuf, 0, retval);
                        os.flush();
                    }
                }
            } catch (Exception e) {
                log.error("Error w/ Media Thread", e);
            } finally {
                try {
                    os.close();
                } catch (Exception e) {
                }
                os = null;
                try {
                    is.close();
                } catch (Exception e) {
                }
                is = null;
                try {
                    mediaSocket.close();
                } catch (Exception e) {
                }
                mediaSocket = null;
            }
            if (!alive)
                break;
            try {
                mediaSocket = EstablishServerConnection(1);
            } catch (Exception e) {
                connectionError();
            }
            if (mediaSocket == null) {
                // System.out.println("couldn't connect to media server,
                // retrying in 1 secs.");
                // try{Thread.sleep(1000);}catch(InterruptedException e){}
                connectionError();
            }
        }
    }

    private void connectionError() {
        close();
    }

    private void eventChannelError() {
        if (reconnectAllowed && alive && !encryptEvents && firstFrameStarted) {
            // close the gfx sockets; this'll cause an error in the GFX loop
            // which'll then cause it to do a reconnect
            log.warn("Event channel error occurred...closing other sockets to force reconnect...");
            try {
                gfxSocket.close();
            } catch (Exception e) {
            }
        } else
            close();
    }

    public String getServerName() {
        return msi.address;
    }

    public void addTimerTask(java.util.TimerTask addMe, long delay, long period) {
        if (uiTimer == null)
            uiTimer = new java.util.Timer(true);
        if (period == 0)
            uiTimer.schedule(addMe, delay);
        else
            uiTimer.schedule(addMe, delay, period);
    }

    public MediaCmd getMediaCmd() {
        return myMedia;
    }
    public GFXCMD2 getGfxCmd() {
        return myGfx;
    }

    public boolean hasFontServer() {
        return fontServer;
    }

    public boolean isLocahostConnection() {
        return ("127.0.0.1".equals(msi.address) || "localhost".equals(msi.address));
    }


    public UIRenderer<?> getUiRenderer() {
        return uiRenderer;
    }

    public MiniPlayerPlugin newPlayerPlugin( String urlString) {
        return uiRenderer.newPlayerPlugin(this, urlString);
    }

    public boolean doesUseAdvancedImageCaching() {
        return usesAdvancedImageCaching;
    }

    /**
     * Android requires that all network activity happen in a background thread.  The EventRouterThread
     * is a blocking queue of events that get processed on a separate thread when communicating to the server.
     */
    public class EventRouterThread extends Thread {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(100);
        Runnable event = null;

        public EventRouterThread(String evtRouter) {
            super(evtRouter);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // blocks until an event is ready
                    event = queue.take();
                    if (!performingReconnect) {
                        event.run();
                    }
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    log.warn("EventRouterThread is shutting down");
                    return;
                } catch (Throwable t) {
                    log.warn("Event Processing Error", t);
                    // event likely caused an error, ignore it for now
                }
            }
        }
    }

    public boolean isVideoCodecSupported(String codecName)
    {
        return true;
    }
    
    public boolean isAudioCodecSupported(String codecName)
    {
        return true;
    }
    
}
