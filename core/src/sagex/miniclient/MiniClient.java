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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import sagex.miniclient.httpbridge.SageTVHttpMediaServerBridge;

/**
 * MiniClient is the central access point to all things MiniClient.  It must be initialized, at least once, using
 * MiniClient.get().init(configDir, cacheDir).
 */
public class MiniClient {
    public static final Logger log = LoggerFactory.getLogger(MiniClient.class);
    public static final String BYTE_CHARSET = "ISO8859_1";
    private static final MiniClient INSTANCE = new MiniClient();
    private static final String PLACESHIFTER_PROPERTIES = "SageTVPlaceshifter.properties";
    ServerDiscovery serverDiscovery;
    Servers servers;
    Thread connectionThread = null;
    SageTVHttpMediaServerBridge httpBridge = null;
    private String MACAddress;
    private MiniClientConnection currentConnection;
    private sagex.miniclient.uibridge.UIRenderer<?> UIRenderer;
    private java.util.Properties myProperties;
    private SageLocatorService locatorService;
    private File cacheDir;
    private File configDir;
    private String cryptoFormats;
    private boolean initialized = false;
    private boolean usingHttpBridge = true;

    public MiniClient() {
        this.myProperties = new Properties();
        this.serverDiscovery = new ServerDiscovery();
        this.servers = new Servers(this);
    }

    public static MiniClient get() {
        return INSTANCE;
    }

    public boolean isUsingHttpBridge() {
        return usingHttpBridge;
    }

    public void setUsingHttpBridge(boolean usingHttpBridge) {
        this.usingHttpBridge = usingHttpBridge;
    }

    public SageTVHttpMediaServerBridge getHttpBridge() {
        if (!isUsingHttpBridge()) return null;
        if (httpBridge == null) {
            httpBridge = new SageTVHttpMediaServerBridge(this, 9991);
            try {
                httpBridge.start(1000, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return httpBridge;
    }

    public ServerDiscovery getServerDiscovery() {
        return serverDiscovery;
    }

    public Servers getServers() {
        return servers;
    }

    public void saveConfig() {
        java.io.OutputStream os = null;
        try {
            log.debug("Saving Configuration");
            os = new java.io.FileOutputStream(new java.io.File(configDir, PLACESHIFTER_PROPERTIES + ".tmp"));
            myProperties.store(os, "SageTV Placeshifter Properties");
            os.close();
            new java.io.File(configDir, PLACESHIFTER_PROPERTIES).delete();
            new java.io.File(configDir, PLACESHIFTER_PROPERTIES + ".tmp")
                    .renameTo(new java.io.File(configDir, PLACESHIFTER_PROPERTIES));
        } catch (java.io.IOException e) {
            log.error("Error saving configuration properties", e);
        } finally {
            try {
                if (os != null)
                    os.close();
            } catch (Exception e) {
            }
            os = null;
        }
    }

    public void init(File configDir, File cacheDir) {
        if (initialized) {
            destroy();
        }
        this.configDir = configDir;
        this.cacheDir = cacheDir;
        configDir.mkdirs();
        cacheDir.mkdirs();

        log.info("MiniClient starting with configDir: {} and cacheDir: {}", configDir, cacheDir);

        myProperties = new java.util.Properties();

        // If the properties file is in the working directory; then use that one
        // and save it back there. Otherwise
        // use the one in the user's home directory
        java.io.File propFile = new java.io.File(configDir, PLACESHIFTER_PROPERTIES);
        if (propFile.isFile()) {
            java.io.InputStream is = null;
            try {
                is = new java.io.FileInputStream(propFile);
                myProperties.load(is);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null)
                        is.close();
                } catch (Exception e) {
                }
                is = null;
            }
        }

        log.debug("Miniclient Configuration Loaded");


        try {
            javax.crypto.Cipher.getInstance("RSA");
            cryptoFormats = "RSA,Blowfish,DH,DES";
        } catch (Exception e) {
            // If we don't do RSA, then we use DH for the key exchange and DES
            // for the secret stuff
            cryptoFormats = "DH,DES";
        }
        log.info("Detecting cryptography support.  Formats {}", cryptoFormats);
        initialized = true;
    }

    private void destroy() {
        shutdown();
        myProperties.clear();
        initialized = false;
    }

    public Properties properties() {
        return myProperties;
    }

    public boolean isUsingOpenGL() {
        return getBooleanProperty("opengl", "true");
    }

    public boolean getBooleanProperty(String prop, String def) {
        String v = getProperty(prop, def);
        return v.equalsIgnoreCase("true") || v.equals("1") || v.equals("yes");
    }

    public String getProperty(String prop, String def) {
        if (!initialized)
            throw new RuntimeException("MiniClient.get().init() must be called before getProperty()");
        return myProperties.getProperty(prop, def);
    }

    public void setProperty(String prop, String value) {
        log.debug("Setting MiniClient Property {}='{}'", prop, value);
        myProperties.setProperty(prop, value);
    }

    public String getCryptoFormats() {
        return cryptoFormats;
    }

    public String getMACAddress() {
        log.warn("TODO: Implement getMACAddress()");
        return MACAddress;
    }

    public MiniClientConnection getCurrentConnection() {
        return currentConnection;
    }

    public void setCurrentConnection(MiniClientConnection currentConnection) {
        this.currentConnection = currentConnection;
    }

    public boolean isConnected() {
        return currentConnection != null && currentConnection.isConnected();
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public sagex.miniclient.uibridge.UIRenderer<?> getUIRenderer() {
        return UIRenderer;
    }

    public void setUIRenderer(sagex.miniclient.uibridge.UIRenderer<?> UIRenderer) {
        this.UIRenderer = UIRenderer;
    }

    public void connect(ServerInfo si, MACAddressResolver macAddressResolver) throws IOException {
        MiniClientConnection connection = new MiniClientConnection(this, si.name, macAddressResolver.getMACAddress(), false, si);
        connection.connect();
    }

    public void closeConnection() {
        if (currentConnection != null) {
            currentConnection.close();
            currentConnection = null;
        }
    }

    public void shutdown() {
        if (isUsingHttpBridge() && httpBridge != null) {
            httpBridge.stop();
            this.httpBridge = null;
        }
        if (currentConnection != null) {
            closeConnection();
        }
        if (this.UIRenderer != null) {
            this.UIRenderer.close();
            this.UIRenderer = null;
        }
    }
}
