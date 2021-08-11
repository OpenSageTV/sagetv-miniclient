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
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sagex.miniclient.events.ConnectedEvent;
import sagex.miniclient.prefs.PrefStore;

/**
 * MiniClient is the central access point to all things MiniClient.
 */
public class MiniClient
{
    public static final Logger log = LoggerFactory.getLogger(MiniClient.class);
    public static final String BYTE_CHARSET = "ISO8859_1";

    private final MiniClientOptions options;

    ServerDiscovery serverDiscovery;
    Servers servers;
    Thread connectionThread = null;
    private String MACAddress;
    private MiniClientConnection currentConnection;
    private sagex.miniclient.uibridge.UIRenderer<?> UIRenderer;
    private SageTVLocatorService locatorService;
    private String cryptoFormats;
    private boolean initialized = false;
    private IBus eventBus;
    private ExecutorService backgroundService = null;
    private ServerInfo connectedServer;
    private ImageCache imageCache;

    public MiniClient(MiniClientOptions options)
    {
        this.options = options;
        eventBus = options.getBus();
        if (eventBus == null)
        {
            log.warn("Using a DeadBus for the event bus, since a bus was not provided in the MiniClientOptions");
            eventBus = DeadBus.INSTANCE;
        }
        this.backgroundService = Executors.newFixedThreadPool(5);
        this.serverDiscovery = new ServerDiscovery();
        this.servers = new Servers(this);
        init();
    }

    public ServerInfo getConnectedServerInfo() {
        return connectedServer;
    }

    /**
     * Returns the Executor Service used for submitting background tasks
     *
     * @return
     */
    public ExecutorService getBackgroundService() {
        return backgroundService;
    }

    public ServerDiscovery getServerDiscovery() {
        return serverDiscovery;
    }

    public Servers getServers() {
        return servers;
    }

    private void init()
    {
        if (initialized)
        {
            destroy();
        }

        options.getCacheDir().mkdirs();
        options.getConfigDir().mkdirs();

        log.info("MiniClient v{} starting on date/time {}", Version.VERSION, new Date());
        log.info("MiniClient cacheDir: {}", options.getCacheDir());

        try
        {
            javax.crypto.Cipher.getInstance("RSA");
            cryptoFormats = "RSA,Blowfish,DH,DES";
        }
        catch (Exception e)
        {
            // If we don't do RSA, then we use DH for the key exchange and DES
            // for the secret stuff
            cryptoFormats = "DH,DES";
        }
        log.info("Detecting cryptography support.  Formats {}", cryptoFormats);

        initialized = true;
    }

    private void destroy()
    {
        shutdown();
        initialized = false;
    }

    public PrefStore properties()
    {
        return options.getPrefs();
    }

    public MiniClientOptions options()
    {
        return options;
    }

    public IBus eventbus() {
        return eventBus;
    }

    public String getCryptoFormats() {
        return cryptoFormats;
    }

    public String getMACAddress()
    {
        log.warn("TODO: Implement getMACAddress()");
        return MACAddress;
    }

    public MiniClientConnection getCurrentConnection() {
        return currentConnection;
    }

    public void setCurrentConnection(MiniClientConnection currentConnection)
    {
        this.currentConnection = currentConnection;
    }

    public boolean isConnected()
    {
        return currentConnection != null && currentConnection.isConnected();
    }

    public sagex.miniclient.uibridge.UIRenderer<?> getUIRenderer() {
        return UIRenderer;
    }

    public void setUIRenderer(sagex.miniclient.uibridge.UIRenderer<?> UIRenderer)
    {
        this.UIRenderer = UIRenderer;
    }

    public void connect(ServerInfo si, MACAddressResolver macAddressResolver) throws IOException
    {
        if (si.isLocatorOnly() || si.forceLocator)
        {
            log.debug("Resolving Server Address from GUID {}", si);
            String address = SageTVLocatorService.lookupIPForGuid(si.locatorID);
            si = si.clone();
            si.address = address;
            log.debug("Server Address Lookup complete {}", si);
        }

        this.connectedServer=si;

        if (imageCache!=null)
        {
            imageCache.cleanUp();
        }

        imageCache = new ImageCache(this);
        MiniClientConnection connection = new MiniClientConnection(this, macAddressResolver.getMACAddress(), si);
        connection.connect();
        eventbus().post(new ConnectedEvent());
    }

    public void closeConnection()
    {
        if (currentConnection != null)
        {
            currentConnection.close();
            currentConnection = null;
        }
        if (imageCache!=null)
        {
            imageCache.cleanUp();
        }
    }

    public void shutdown()
    {
        if (backgroundService != null)
        {
            backgroundService.shutdownNow();
        }
        if (currentConnection != null)
        {
            closeConnection();
        }
        if (this.UIRenderer != null)
        {
            this.UIRenderer.close();
            this.UIRenderer = null;
        }
    }

    public MiniPlayerPlugin getPlayer()
    {
        return currentConnection.getMediaCmd().getPlaya();
    }

    public boolean isVideoPlaying()
    {
        return isVideoVisible() && currentConnection.getMediaCmd().getPlaya().getState() == MiniPlayerPlugin.PLAY_STATE;
    }

    public boolean isVideoPaused()
    {
        if(currentConnection.getMediaCmd().getPlaya() != null)
        {
            log.debug("isVideoPaused: " + " isVideoVisible: " + isVideoVisible() + " player state:" + currentConnection.getMediaCmd().getPlaya().getState() + " PAUSE: MiniPlayerPlugin.PAUSE_STATE;");
        }

        return isVideoVisible() && currentConnection.getMediaCmd().getPlaya().getState() == MiniPlayerPlugin.PAUSE_STATE;
    }

    public boolean isVideoVisible()
    {
        return currentConnection != null && currentConnection.getMediaCmd() != null && currentConnection.getMediaCmd().getPlaya() != null;
    }

    public void prepareCodecs(List<String> videoCodecs, List<String> audioCodecs, List<String> pushFormats, List<String> pullFormats, Properties codecs)
    {
        options.prepareCodecs(videoCodecs, audioCodecs, pushFormats, pullFormats, codecs);
    }

    public ImageCache getImageCache()
    {
        return imageCache;
    }

    public boolean isReady()
    {
        return isConnected() && getCurrentConnection().hasEventChannel();
    }
}
