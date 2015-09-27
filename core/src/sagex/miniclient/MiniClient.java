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

public class MiniClient {
	public static final String BYTE_CHARSET = "ISO8859_1";
	public static boolean WINDOWS_OS = false;
	public static boolean MAC_OS_X = false;
	public static boolean LINUX_OS = false;
	public static java.util.Properties myProperties;
	public static boolean fsStartup = false;
	public static Integer irKillCode = null;
	public static String cryptoFormats = "";
	public static String[] mainArgs;
	public static java.text.DateFormat DF = new java.text.SimpleDateFormat("EE M/d H:mm:ss.SSS");
	public static boolean forcedServer = false;
	public static String forcedMAC = null;
	private static boolean online = false;
	private static int ConnectionError = 0;
	private static java.io.File configDir;
	private static boolean shuttingDown = false;
	private static SageLocatorService locatorService;

	static {
		System.out.println("Starting MiniClient");
		WINDOWS_OS = System.getProperty("os.name").toLowerCase().indexOf("windows") != -1;
		MAC_OS_X = System.getProperty("os.name").toLowerCase().indexOf("mac os x") != -1;
		LINUX_OS = !WINDOWS_OS && !MAC_OS_X;
	}

	public static final String df() {
		return df(System.currentTimeMillis());
	}

	public static final String df(long time) {
		synchronized (DF) {
			return DF.format(new java.util.Date(time));
		}
	}

	public static void saveConfig() {
		java.io.OutputStream os = null;
		// java.io.File configDir = new
		// java.io.File(System.getProperty("user.home"), ".sagetv");
		try {
			os = new java.io.FileOutputStream(new java.io.File(configDir, "SageTVPlaceshifter.properties.tmp"));
			myProperties.store(os, "SageTV Placeshifter Properties");
			os.close();
			new java.io.File(configDir, "SageTVPlaceshifter.properties").delete();
			new java.io.File(configDir, "SageTVPlaceshifter.properties.tmp")
					.renameTo(new java.io.File(configDir, "SageTVPlaceshifter.properties"));
		} catch (java.io.IOException e) {
			// Attempting to show a dialog in the shutdown hook is a bad idea,
			// so don't do that here
			System.out.println("Error saving configuration properties of:" + e.toString());
		} finally {
			try {
				if (os != null)
					os.close();
			} catch (Exception e) {
			}
			os = null;
		}
	}

	public static void startup(String[] args) {
		if (MAC_OS_X) {
			try {
				System.loadLibrary("MiniClient");
			} catch (Throwable t) {
				System.out.println("Exception occured loading MiniClient library: " + t);
			}
		}

		myProperties = new java.util.Properties();
		if (new java.io.File("SageTVPlaceshifter.properties").isFile())
			configDir = new java.io.File(System.getProperty("user.dir"));
		else
			configDir = new java.io.File(System.getProperty("user.home"), ".sagetv");
		configDir.mkdirs();
		// If the properties file is in the working directory; then use that one
		// and save it back there. Otherwise
		// use the one in the user's home directory
		java.io.File propFile = new java.io.File(configDir, "SageTVPlaceshifter.properties");
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
		} else {
			// act like we're running opengl on Mac OS X
			if (MAC_OS_X)
				myProperties.setProperty("opengl", "true");
		}

		java.io.PrintStream redir = null;
		redir = new java.io.PrintStream(new java.io.OutputStream() {
			public void write(int b) {
			}
		}, true) {

			public synchronized void println(String s) {
				System.err.println(df() + " " + s);
			}
		};

		System.setOut(redir);
		// System.setErr(redir);
		mainArgs = args;
		boolean noretries = false;
		for (int i = 0; args != null && i < args.length; i++) {
			if (args[i].equals("-mac") && i < args.length - 1) {
				forcedMAC = args[++i];
			} else if (args[i].equals("-fullscreen"))
				fsStartup = true;
			else if (args[i].equals("-noretry"))
				noretries = true;
			else if (args[i].equals("-irexitcode") && i < args.length - 1) {
				try {
					irKillCode = new Integer(args[++i]);
				} catch (NumberFormatException e) {
					System.out.println("ERROR: Invalid irexitcode parameter of: " + args[i]);
				}
			}
		}
		
		System.out.println("Detecting cryptography support...");
		try {
			javax.crypto.Cipher.getInstance("RSA");
			cryptoFormats = "RSA,Blowfish,DH,DES";
		} catch (Exception e) {
			// If we don't do RSA, then we use DH for the key exchange and DES
			// for the secret stuff
			cryptoFormats = "DH,DES";
		}

		online = true;
	}

	public static boolean isUsingOpenGL() {
		return getBooleanProperty("opengl", "true");
	}

	public static boolean getBooleanProperty(String prop, String def) {
		String v = getProperty(prop, def);
		return v.equalsIgnoreCase("true") || v.equals("1") || v.equals("yes");
	}

	public static String getProperty(String prop, String def) {
		if (!online)
			throw new RuntimeException("MiniClient.startup() must be called before getProperty()");
		return myProperties.getProperty(prop, def);
	}


	private static void startupPM() {
//		sage.PowerManagement pm = MiniClientPowerManagement.getInstance();
//		pm.setLogging(false);
//		pm.setWaitTime(30000);
//		pm.setPrerollTime(120000);
//		pm.setIdleTimeout(120000);
//		Thread t = new Thread(pm, "PowerManagement");
//		t.setDaemon(true);
//		t.setPriority(Thread.MIN_PRIORITY);
//		t.start();
	}

	private static void startupDeviceDetector() {
//		MiniStorageDeviceDetector dd = new MiniStorageDeviceDetector();
//		Thread t = new Thread(dd, "DeviceDetector");
//		t.setDaemon(true);
//		t.setPriority(Thread.MIN_PRIORITY);
//		t.start();
	}

	public static void safeExit(final int state) {
		// This can cause deadlocks on some platforms, so execute it in another
		// thread
		new Thread(new Runnable() {
			public void run() {
				System.exit(state);
			}
		}).start();
	}

}
