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

import sagex.miniclient.uibridge.Dimension;
import sagex.miniclient.uibridge.ImageHolder;
import sagex.miniclient.uibridge.Rectangle;
import sagex.miniclient.uibridge.UIRenderer;

public class GFXCMD2 {
    public static final boolean ENABLE_MOUSE_MOTION_EVENTS = true;
    public static final String[] CMD_NAMES = {"", "INIT", "DEINIT", "", "", "", "", "", "", "", "", "", "", "", "", "", "DRAWRECT",
            "FILLRECT", "CLEARRECT", "DRAWOVAL", "FILLOVAL", "DRAWROUNDRECT", "FILLROUNDRECT", "DRAWTEXT", "DRAWTEXTURED",
            "DRAWLINE", "LOADIMAGE", "UNLOADIMAGE", "LOADFONT", "UNLOADFONT", "FLIPBUFFER", "STARTFRAME", "LOADIMAGELINE",
            "PREPIMAGE", "LOADIMAGECOMPRESSED", "XFMIMAGE", "LOADFONTSTREAM", "CREATESURFACE", "SETTARGETSURFACE", "",
            "DRAWTEXTUREDDIFFUSED", "PUSHTRANSFORM", "POPTRANSFORM", "TEXTUREBATCH", "LOADCACHEDIMAGE", "LOADIMAGETARGETED",
            "PREPIMAGETARGETED"};
    public static final int GFXCMD_INIT = 1;
    public static final int GFXCMD_DEINIT = 2;
    public static final int GFXCMD_DRAWRECT = 16;
    public static final int GFXCMD_FILLRECT = 17;
    // x, y, width, height, thickness, argbTL, argbTR, argbBR, argbBL
    public static final int GFXCMD_CLEARRECT = 18;
    // x, y, width, height, argbTL, argbTR, argbBR, argbBL
    public static final int GFXCMD_DRAWOVAL = 19;
    // x, y, width, height, argbTL, argbTR, argbBR, argbBL
    public static final int GFXCMD_FILLOVAL = 20;
    // x, y, width, height, thickness, argbTL, argbTR, argbBR, argbBL,
    // clipX, clipY, clipW, clipH
    public static final int GFXCMD_DRAWROUNDRECT = 21;
    // x, y, width, height, argbTL, argbTR, argbBR, argbBL,
    // clipX, clipY, clipW, clipH
    public static final int GFXCMD_FILLROUNDRECT = 22;
    // x, y, width, height, thickness, arcRadius, argbTL, argbTR, argbBR,
    // argbBL,
    // clipX, clipY, clipW, clipH
    public static final int GFXCMD_DRAWTEXT = 23;
    // x, y, width, height, arcRadius, argbTL, argbTR, argbBR, argbBL,
    // clipX, clipY, clipW, clipH
    public static final int GFXCMD_DRAWTEXTURED = 24;
    // x, y, len, text, handle, argb, clipX, clipY, clipW, clipH
    public static final int GFXCMD_DRAWLINE = 25;
    // x, y, width, height, handle, srcx, srcy, srcwidth, srcheight, blend
    public static final int GFXCMD_LOADIMAGE = 26;
    // x1, y1, x2, y2, argb1, argb2
    public static final int GFXCMD_UNLOADIMAGE = 27;
    // width, height
    public static final int GFXCMD_LOADFONT = 28;
    // handle
    public static final int GFXCMD_UNLOADFONT = 29;
    // namelen, name, style, size
    public static final int GFXCMD_FLIPBUFFER = 30;
    // handle
    public static final int GFXCMD_STARTFRAME = 31;
    public static final int GFXCMD_LOADIMAGELINE = 32;
    public static final int GFXCMD_PREPIMAGE = 33;
    // handle, line, len, data
    public static final int GFXCMD_LOADIMAGECOMPRESSED = 34;
    // width, height
    public static final int GFXCMD_XFMIMAGE = 35;
    // handle, len, data
    public static final int GFXCMD_LOADFONTSTREAM = 36;
    // srcHandle, destHandle, destWidth, destHeight, maskCornerArc
    public static final int GFXCMD_CREATESURFACE = 37;
    // namelen, name, len, data
    public static final int GFXCMD_SETTARGETSURFACE = 38;
    // width, height
    public static final int GFXCMD_DRAWTEXTUREDDIFFUSE = 40;
    // handle
    public static final int GFXCMD_PUSHTRANSFORM = 41;
    // x, y, width, height, handle, srcx, srcy, srcwidth, srcheight, blend,
    // diffhandle, diffsrcx, diffsrcy, diffsrcwidth, diffsrcheight
    public static final int GFXCMD_POPTRANSFORM = 42;
    // v'= matrix * v
    // sent by row, then col, 12 values (skip the 4th column since its fixed)
    public static final int GFXCMD_TEXTUREBATCH = 43;
    public static final int GFXCMD_LOADCACHEDIMAGE = 44;
    // count, size
    public static final int GFXCMD_LOADIMAGETARGETED = 45;
    // handle, width, height, cacheResourceID
    public static final int GFXCMD_PREPIMAGETARGETED = 46;
    // handle, width, height, [format]
    public static final int GFXCMD_SETVIDEOPROP = 130;
    // handle, width, height, [cache resource id] (but this will never actually
    // load from the offline cache, this is only for knowing where to cache it)
    private static final Logger log = LoggerFactory.getLogger(GFXCMD2.class);
    private final MiniClient client;
    // mode, sx, sy, swidth, sheight, ox, oy, owidth, oheight, alpha, activewin
    protected long offlineImageCacheLimit;
    protected java.io.File cacheDir;
    private UIRenderer<?> windowManager;
    private MiniClientConnectionGateway myConn;
    private java.util.Map<Integer, Long> lruImageMap = new java.util.HashMap<Integer, Long>();
    private boolean usesAdvancedImageCaching;
    private java.util.Map<Integer, sagex.miniclient.uibridge.ImageHolder> imageMap = new java.util.HashMap<Integer, sagex.miniclient.uibridge.ImageHolder>();
    private int handleCount = 2;
    private long imageCacheSize;
    private long imageCacheLimit;
    private boolean cursorHidden;
    private String lastImageResourceID;
    private int lastImageResourceIDHandle;

    public GFXCMD2(MiniClient client) {
        this.client = client;
        this.windowManager = client.getUIRenderer();
        this.myConn = client.getCurrentConnection();
        imageCacheLimit = 32000000;
        try {
            imageCacheLimit = Integer.parseInt(client.getProperty("image_cache_size", "32000000"));
        } catch (Exception e) {
            log.error("Invalid image_cache_size property", e);
        }

        offlineImageCacheLimit = Integer.parseInt(client.getProperty("disk_image_cache_size", "100000000"));
        if ("true".equals(client.getProperty("cache_images_on_disk", "true"))) {
            cacheDir = new java.io.File(client.getCacheDir(), "imgcache");
            cacheDir.mkdir();
        } else
            cacheDir = null;
    }

    public static int readInt(int pos, byte[] cmddata) {
        pos += 4; // for the 4 bytes for the header
        return ((cmddata[pos + 0] & 0xFF) << 24) | ((cmddata[pos + 1] & 0xFF) << 16) | ((cmddata[pos + 2] & 0xFF) << 8)
                | (cmddata[pos + 3] & 0xFF);
    }

    public static float readFloat(int pos, byte[] cmddata) {
        pos += 4; // for the 4 bytes for the header
        return Float.intBitsToFloat(((cmddata[pos + 0] & 0xFF) << 24) | ((cmddata[pos + 1] & 0xFF) << 16)
                | ((cmddata[pos + 2] & 0xFF) << 8) | (cmddata[pos + 3] & 0xFF));
    }

    public static int readIntSwapped(int pos, byte[] cmddata) {
        pos += 4; // for the 4 bytes for the header
        return ((cmddata[pos + 3] & 0xFF) << 24) | ((cmddata[pos + 2] & 0xFF) << 16) | ((cmddata[pos + 1] & 0xFF) << 8)
                | (cmddata[pos + 0] & 0xFF);
    }

    public static short readShort(int pos, byte[] cmddata) {
        pos += 4; // for the 4 bytes for the header
        return (short) (((cmddata[pos + 0] & 0xFF) << 8) | (cmddata[pos + 1] & 0xFF));
    }

    public static short readShortSwapped(int pos, byte[] cmddata) {
        pos += 4; // for the 4 bytes for the header
        return (short) (((cmddata[pos + 1] & 0xFF) << 8) | (cmddata[pos + 0] & 0xFF));
    }

    public void close() {
        windowManager.close();
    }

    public void refresh() {
        windowManager.refresh();
    }

    public int ExecuteGFXCommand(int cmd, int len, byte[] cmddata, int[] hasret) {
        len -= 4; // for the 4 byte header
        hasret[0] = 0; // Nothing to return by default
        // System.out.println("GFXCMD=" + ((cmd >= 0 && cmd < CMD_NAMES.length)
        // ? CMD_NAMES[cmd] : ("UnknownCmd " + cmd)));

        if (!windowManager.hasGraphicsCanvas()) {
            switch (cmd) {
                case GFXCMD_INIT:
                case GFXCMD_DEINIT:
                case GFXCMD_STARTFRAME:
                case GFXCMD_FLIPBUFFER:
                    windowManager.hideCursor();
                    break;
                case GFXCMD_DRAWRECT:
                case GFXCMD_FILLRECT:
                case GFXCMD_CLEARRECT:
                case GFXCMD_DRAWOVAL:
                case GFXCMD_FILLOVAL:
                case GFXCMD_DRAWROUNDRECT:
                case GFXCMD_FILLROUNDRECT:
                case GFXCMD_DRAWTEXT:
                case GFXCMD_DRAWTEXTURED:
                case GFXCMD_DRAWLINE:
                case GFXCMD_LOADIMAGE:
                case GFXCMD_LOADIMAGETARGETED:
                case GFXCMD_UNLOADIMAGE:
                case GFXCMD_LOADFONT:
                case GFXCMD_UNLOADFONT:
                case GFXCMD_SETTARGETSURFACE:
                case GFXCMD_CREATESURFACE:
                    break;
                case GFXCMD_PREPIMAGE:
                case GFXCMD_LOADIMAGELINE:
                case GFXCMD_LOADIMAGECOMPRESSED:
                case GFXCMD_XFMIMAGE:
                case GFXCMD_LOADCACHEDIMAGE:
                case GFXCMD_PREPIMAGETARGETED:
                    if (!cursorHidden)
                        windowManager.showBusyCursor();
                    break;
            }
        }
        switch (cmd) {
            case GFXCMD_INIT:
                hasret[0] = 1;
                windowManager.GFXCMD_INIT();
                return 1;
            case GFXCMD_DEINIT:
                windowManager.GFXCMD_DEINIT();
                break;
            case GFXCMD_DRAWRECT:
                if (len == 36) {
                    int x, y, width, height, thickness, argbTL, argbTR, argbBR, argbBL;
                    x = readInt(0, cmddata);
                    y = readInt(4, cmddata);
                    width = readInt(8, cmddata);
                    height = readInt(12, cmddata);
                    thickness = readInt(16, cmddata);
                    argbTL = readInt(20, cmddata);
                    argbTR = readInt(24, cmddata);
                    argbBR = readInt(28, cmddata);
                    argbBL = readInt(32, cmddata);

                    windowManager.drawRect(x, y, width, height, thickness, argbTL, argbTR, argbBR, argbBL);
                } else {
                    log.warn("Invalid len for GFXCMD_DRAWRECT: {}", len);
                }
                break;
            case GFXCMD_FILLRECT:
                // x, y, width, height, argbTL, argbTR, argbBR, argbBL
                if (len == 32) {
                    int x, y, width, height, argbTL, argbTR, argbBR, argbBL;
                    x = readInt(0, cmddata);
                    y = readInt(4, cmddata);
                    width = readInt(8, cmddata);
                    height = readInt(12, cmddata);
                    argbTL = readInt(16, cmddata);
                    argbTR = readInt(20, cmddata);
                    argbBR = readInt(24, cmddata);
                    argbBL = readInt(28, cmddata);
                    windowManager.fillRect(x, y, width, height, argbTL, argbTR, argbBR, argbBL);
                } else {
                    log.warn("Invalid len for GFXCMD_FILLRECT: {}", len);
                }
                break;
            case GFXCMD_CLEARRECT:
                // x, y, width, height, argbTL, argbTR, argbBR, argbBL
                if (len == 32) {
                    int x, y, width, height, argbTL, argbTR, argbBR, argbBL;
                    x = readInt(0, cmddata);
                    y = readInt(4, cmddata);
                    width = readInt(8, cmddata);
                    height = readInt(12, cmddata);
                    argbTL = readInt(16, cmddata);
                    argbTR = readInt(20, cmddata);
                    argbBR = readInt(24, cmddata);
                    argbBL = readInt(28, cmddata);
                    windowManager.clearRect(x, y, width, height, argbTL, argbTR, argbBR, argbBL);
                } else {
                    log.warn("Invalid len for GFXCMD_CLEARRECT: {}", len);
                }
                break;
            case GFXCMD_DRAWOVAL:
                // x, y, width, height, thickness, argbTL, argbTR, argbBR, argbBL,
                // clipX, clipY, clipW, clipH
                if (len == 52) {
                    int x, y, width, height, thickness, argbTL, argbTR, argbBR, argbBL, clipX, clipY, clipW, clipH;
                    x = readInt(0, cmddata);
                    y = readInt(4, cmddata);
                    width = readInt(8, cmddata);
                    height = readInt(12, cmddata);
                    thickness = readInt(16, cmddata);
                    argbTL = readInt(20, cmddata);
                    argbTR = readInt(24, cmddata);
                    argbBR = readInt(28, cmddata);
                    argbBL = readInt(32, cmddata);
                    clipX = readInt(36, cmddata);
                    clipY = readInt(40, cmddata);
                    clipW = readInt(44, cmddata);
                    clipH = readInt(48, cmddata);
                    windowManager.drawOval(x, y, width, height, thickness, argbTL, argbTR, argbBR, argbBL, clipX, clipY, clipW, clipH);
                } else {
                    log.warn("Invalid len for GFXCMD_DRAWOVAL: {}", len);
                }

                break;
            case GFXCMD_FILLOVAL:
                // x, y, width, height, argbTL, argbTR, argbBR, argbBL,
                // clipX, clipY, clipW, clipH
                if (len == 48) {
                    int x, y, width, height, argbTL, argbTR, argbBR, argbBL, clipX, clipY, clipW, clipH;
                    x = readInt(0, cmddata);
                    y = readInt(4, cmddata);
                    width = readInt(8, cmddata);
                    height = readInt(12, cmddata);
                    argbTL = readInt(16, cmddata);
                    argbTR = readInt(20, cmddata);
                    argbBR = readInt(24, cmddata);
                    argbBL = readInt(28, cmddata);
                    clipX = readInt(32, cmddata);
                    clipY = readInt(36, cmddata);
                    clipW = readInt(40, cmddata);
                    clipH = readInt(44, cmddata);
                    windowManager.fillOval(x, y, width, height, argbTL, argbTR, argbBR, argbBL, clipX, clipY, clipW, clipH);
                } else {
                    log.warn("Invalid len for GFXCMD_FILLOVAL: {}", len);
                }
                break;
            case GFXCMD_DRAWROUNDRECT:
                // x, y, width, height, thickness, arcRadius, argbTL, argbTR,
                // argbBR, argbBL,
                // clipX, clipY, clipW, clipH
                if (len == 56) {
                    int x, y, width, height, thickness, arcRadius, argbTL, argbTR, argbBR, argbBL, clipX, clipY, clipW, clipH;
                    x = readInt(0, cmddata);
                    y = readInt(4, cmddata);
                    width = readInt(8, cmddata);
                    height = readInt(12, cmddata);
                    thickness = readInt(16, cmddata);
                    arcRadius = readInt(20, cmddata) * 2;
                    argbTL = readInt(24, cmddata);
                    argbTR = readInt(28, cmddata);
                    argbBR = readInt(32, cmddata);
                    argbBL = readInt(36, cmddata);
                    clipX = readInt(40, cmddata);
                    clipY = readInt(44, cmddata);
                    clipW = readInt(48, cmddata);
                    clipH = readInt(52, cmddata);
                    windowManager.drawRoundRect(x, y, width, height, thickness, arcRadius, argbTL, argbTR, argbBR, argbBL, clipX, clipY,
                            clipW, clipH);
                } else {
                    log.warn("Invalid len for GFXCMD_DRAWROUNDRECT: {}", len);
                }
                break;
            case GFXCMD_FILLROUNDRECT:
                // x, y, width, height, arcRadius, argbTL, argbTR, argbBR, argbBL,
                // clipX, clipY, clipW, clipH
                if (len == 52) {
                    int x, y, width, height, arcRadius, argbTL, argbTR, argbBR, argbBL, clipX, clipY, clipW, clipH;
                    x = readInt(0, cmddata);
                    y = readInt(4, cmddata);
                    width = readInt(8, cmddata);
                    height = readInt(12, cmddata);
                    arcRadius = readInt(16, cmddata) * 2;
                    argbTL = readInt(20, cmddata);
                    argbTR = readInt(24, cmddata);
                    argbBR = readInt(28, cmddata);
                    argbBL = readInt(32, cmddata);
                    clipX = readInt(36, cmddata);
                    clipY = readInt(40, cmddata);
                    clipW = readInt(44, cmddata);
                    clipH = readInt(48, cmddata);
                    windowManager.fillRoundRect(x, y, width, height, arcRadius, argbTL, argbTR, argbBR, argbBL, clipX, clipY, clipW,
                            clipH);
                } else {
                    log.warn("Invalid len for GFXCMD_FILLROUNDRECT: {}", len);
                }
                break;
            case GFXCMD_DRAWTEXT:
                // x, y, len, text, handle, argb, clipX, clipY, clipW, clipH
//			if (len >= 36 && len >= (36 + readInt(8, cmddata) * 2)) {
//				int x, y, textlen, fontHandle, argb, clipX, clipY, clipW, clipH;
//				StringBuffer text = new StringBuffer();
//				int i;
//
//				x = readInt(0, cmddata);
//				y = readInt(4, cmddata);
//				textlen = readInt(8, cmddata);
//				for (i = 0; i < textlen; i++) {
//					text.append((char) readShort(12 + i * 2, cmddata));
//				}
//				fontHandle = readInt(textlen * 2 + 12, cmddata);
//				argb = readInt(textlen * 2 + 16, cmddata);
//				clipX = readInt(textlen * 2 + 20, cmddata);
//				clipY = readInt(textlen * 2 + 24, cmddata);
//				clipW = readInt(textlen * 2 + 28, cmddata);
//				clipH = readInt(textlen * 2 + 32, cmddata);
//				if (System.getProperty("java.version").startsWith("1.4"))
//					clipW = clipW * 5 / 4;
//				windowManager.drawText(x, y, textlen, text.toString(), fontHandle, fontMap.get(fontHandle), argb, clipX, clipY, clipW, clipH);
//			} else {
//				System.out.println("Invalid len for GFXCMD_DRAWTEXT : " + len);
//			}
                log.warn("DRAWTEXT Not Implemented");
                break;
            case GFXCMD_DRAWTEXTURED:
                // x, y, width, height, handle, srcx, srcy, srcwidth, srcheight,
                // blend
                if (len == 40) {
                    int x, y, width, height, handle, srcx, srcy, srcwidth, srcheight, blend;
                    x = readInt(0, cmddata);
                    y = readInt(4, cmddata);
                    width = readInt(8, cmddata);
                    height = readInt(12, cmddata);
                    handle = readInt(16, cmddata);
                    srcx = readInt(20, cmddata);
                    srcy = readInt(24, cmddata);
                    srcwidth = readInt(28, cmddata);
                    srcheight = readInt(32, cmddata);
                    blend = readInt(36, cmddata);
                    windowManager.drawTexture(x, y, width, height, handle, imageMap.get(handle), srcx, srcy, srcwidth, srcheight, blend);
                    registerImageAccess(handle);
                } else {
                    log.warn("Invalid len for GFXCMD_DRAWTEXTURED: {}", len);
                }
                break;
            case GFXCMD_DRAWLINE:
                // x1, y1, x2, y2, argb1, argb2
                if (len == 24) {
                    int x1, y1, x2, y2, argb1, argb2;
                    x1 = readInt(0, cmddata);
                    y1 = readInt(4, cmddata);
                    x2 = readInt(8, cmddata);
                    y2 = readInt(12, cmddata);
                    argb1 = readInt(16, cmddata);
                    argb2 = readInt(20, cmddata);
                    windowManager.drawLine(x1, y1, x2, y2, argb1, argb2);
                } else {
                    log.warn("Invalid len for GFXCMD_DRAWLINE: {}", len);
                }
                break;
            case GFXCMD_LOADIMAGE:
                // width, height
                if (len >= 8) {
                    int width, height;
                    int imghandle = handleCount++;
                    width = readInt(0, cmddata);
                    height = readInt(4, cmddata);
                    if (width * height * 4 + imageCacheSize > imageCacheLimit)
                        imghandle = 0;
                    else {
                        sagex.miniclient.uibridge.ImageHolder<?> img = windowManager.loadImage(width, height);
                        imageMap.put(imghandle, img);
                        imageCacheSize += width * height * 4;
                    }
                    // imghandle=STBGFX.GFX_loadImage(width, height);
                    hasret[0] = 1;
                    return imghandle;
                } else {
                    log.warn("Invalid len for GFXCMD_LOADIMAGE: {}", len);
                }
                break;
            case GFXCMD_LOADIMAGETARGETED:
                // handle, width, height // Not used unless we do uncompressed
                // images
                if (len >= 12) {
                    int width, height;
                    int imghandle = readInt(0, cmddata);
                    width = readInt(4, cmddata);
                    height = readInt(8, cmddata);
                    while (width * height * 4 + imageCacheSize > imageCacheLimit) {
                        // Keep freeing the oldest image until we have enough memory
                        // to do this
                        int oldestImage = getOldestImage();
                        if (oldestImage != 0) {
                            log.debug("Freeing image to make room in cache");
                            unloadImage(oldestImage);
                            postImageUnload(oldestImage);
                        } else {
                            log.error("ERROR cannot free enough from the cache to support loading a new image!!!");
                            break;
                        }
                    }
                    sagex.miniclient.uibridge.ImageHolder<?> img = windowManager.loadImage(width, height);
                    imageMap.put(imghandle, img);
                    imageCacheSize += width * height * 4;
                    registerImageAccess(imghandle);
                    hasret[0] = 0;
                } else {
                    log.warn("Invalid len for GFXCMD_LOADIMAGETARGETED: {}", len);
                }
                break;
            case GFXCMD_CREATESURFACE:
                // width, height
                if (len >= 8) {
                    int width, height;
                    int imghandle = handleCount++;
                    width = readInt(0, cmddata);
                    height = readInt(4, cmddata);
                    sagex.miniclient.uibridge.ImageHolder<?> img = windowManager.createSurface(imghandle, width, height);
                    imageMap.put(new Integer(imghandle), img);
                    // imghandle=STBGFX.GFX_loadImage(width, height);
                    hasret[0] = 1;
                    return imghandle;
                } else {
                    log.warn("Invalid len for GFXCMD_LOADIMAGE: {}", len);
                }
                break;
            case GFXCMD_PREPIMAGE:
                // width, height
                if (len >= 8) {
                    int width, height;
                    // int imghandle = handleCount++;;
                    width = readInt(0, cmddata);
                    height = readInt(4, cmddata);
                    // java.awt.Image img = new java.awt.image.BufferedImage(width,
                    // height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    // imageMap.put(new Integer(imghandle), img);
                    // We don't actually use this, it's just for being sure we have
                    // enough room for allocation
                    int imghandle = 1;
                    if (width * height * 4 + imageCacheSize > imageCacheLimit)
                        imghandle = 0;
                    else if (len >= 12) {
                        // We've got enough room for it and there's a cache ID,
                        // check if we've got it cached locally
                        int strlen = readInt(8, cmddata);
                        if (strlen > 1) {
                            try {
                                String rezName = new String(cmddata, 16, strlen - 1);
                                lastImageResourceID = rezName;
                                // We use this hashcode to match it up on the
                                // loadCompressedImage call so we know we're caching
                                // the right thing
                                lastImageResourceIDHandle = imghandle = Math.abs(lastImageResourceID.hashCode());
                                java.io.File cachedFile = getCachedImageFile(rezName);
                                if (cachedFile != null) {
                                    // We've got it locally in our cache! Read it
                                    // from there.
                                    sagex.miniclient.uibridge.ImageHolder<?> bi = windowManager.readImage(cachedFile);
                                    if (bi == null || bi.getWidth() != width || bi.getHeight() != height) {
                                        if (bi != null) {
                                            // It doesn't match the cache
                                            log.debug("CACHE ID verification failed for rezName=" + rezName + " cacheSize="
                                                    + bi.getWidth() + "x" + bi.getHeight() + " reqSize=" + width + "x" + height);
                                            bi.flush();
                                            cachedFile.delete();
                                        }
                                        // else we failed loading it from the cache
                                        // so we want it for sure!
                                    } else {
                                        imghandle = handleCount++;
                                        imageMap.put(imghandle, bi);
                                        imageCacheSize += width * height * 4;
                                        hasret[0] = 1;
                                        return -1 * imghandle;
                                    }
                                }
                            } catch (Exception e) {
                                log.error("ERROR loading compressed image", e);
                                e.printStackTrace();
                            }
                        }
                    }
                    // imghandle=STBGFX.GFX_loadImage(width, height);
                    hasret[0] = 1;
                    return imghandle;
                } else {
                    log.error("Invalid len for GFXCMD_PREPIMAGE: {}", len);
                }
                break;
            case GFXCMD_PREPIMAGETARGETED:
                // handle, width, height, [rezID]
                if (len >= 12) {
                    int imghandle, width, height;
                    imghandle = readInt(0, cmddata);
                    width = readInt(4, cmddata);
                    height = readInt(8, cmddata);
                    int strlen = readInt(12, cmddata);
                    while (width * height * 4 + imageCacheSize > imageCacheLimit) {
                        // Keep freeing the oldest image until we have enough memory
                        // to do this
                        int oldestImage = getOldestImage();
                        if (oldestImage != 0) {
                            log.debug("Freeing image to make room in cache");
                            unloadImage(oldestImage);
                            postImageUnload(oldestImage);
                        } else {
                            log.error("ERROR cannot free enough from the cache to support loading a new image!!!");
                            break;
                        }
                    }
                    if (len >= 16) {
                        // We will not have this cached locally...but setup our vars
                        // to track it
                        String rezName = new String(cmddata, 20, strlen - 1);
                        lastImageResourceID = rezName;
                        lastImageResourceIDHandle = imghandle;
                        log.debug("Prepped targeted image with handle " + imghandle + " resource=" + rezName);
                    }
                    registerImageAccess(imghandle);
                    hasret[0] = 0;
                } else {
                    log.warn("Invalid len for GFXCMD_PREPIMAGE: {}", len);
                }
                break;
            case GFXCMD_LOADCACHEDIMAGE:
                // width, height
                if (len >= 18) {
                    int width, height, imghandle;
                    imghandle = readInt(0, cmddata);
                    width = readInt(4, cmddata);
                    height = readInt(8, cmddata);
                    int strlen = readInt(12, cmddata);
                    String rezName = new String(cmddata, 20, strlen - 1);
                    log.debug("imghandle=" + imghandle + " width=" + width + " height=" + height + " strlen=" + strlen
                            + " rezName=" + rezName);
                    while (width * height * 4 + imageCacheSize > imageCacheLimit) {
                        // Keep freeing the oldest image until we have enough memory
                        // to do this
                        int oldestImage = getOldestImage();
                        if (oldestImage != 0) {
                            log.debug("Freeing image to make room in cache");
                            unloadImage(oldestImage);
                            postImageUnload(oldestImage);
                        } else {
                            log.error("ERROR cannot free enough from the cache to support loading a new image!!!");
                            break;
                        }
                    }
                    registerImageAccess(imghandle);
                    try {
                        log.debug("Loading resource from cache: {}", rezName);
                        java.io.File cachedFile = getCachedImageFile(rezName);
                        if (cachedFile != null) {
                            // We've got it locally in our cache! Read it from
                            // there.
                            log.debug("Image found in cache!");

                            // We've got it locally in our cache! Read it from
                            // there.
                            sagex.miniclient.uibridge.ImageHolder<?> bi = windowManager.readImage(cachedFile);
                            if (bi == null || bi.getWidth() != width || bi.getHeight() != height) {
                                if (bi != null) {
                                    // It doesn't match the cache
                                    log.debug("CACHE ID verification failed for rezName=" + rezName + " cacheSize="
                                            + bi.getWidth() + "x" + bi.getHeight() + " reqSize=" + width + "x" + height);
                                    bi.flush();
                                    cachedFile.delete();
                                }
                                // else we failed loading it from the cache so we
                                // want it for sure!
                                // This load failed but the server thought it would
                                // succeed, so we need to inform it that the image
                                // is no longer loaded.
                                postImageUnload(imghandle);
                                myConn.postOfflineCacheChange(false, rezName);
                            } else {
                                imageMap.put(imghandle, bi);
                                imageCacheSize += width * height * 4;
                                hasret[0] = 0;
                            }
                        } else {
                            log.error("ERROR Image not found in cache that should be there! rezName={}", rezName);
                            // This load failed but the server thought it would
                            // succeed, so we need to inform it that the image is no
                            // longer loaded.
                            postImageUnload(imghandle);
                            myConn.postOfflineCacheChange(false, rezName);
                        }
                    } catch (Exception e) {
                        log.error("ERROR loading compressed image", e);
                    }
                    hasret[0] = 0;
                } else {
                    log.error("Invalid len for GFXCMD_PREPIMAGE: {}", len);
                }
                break;
            case GFXCMD_UNLOADIMAGE:
                // handle
                if (len == 4) {
                    int handle;
                    handle = readInt(0, cmddata);
                    unloadImage(handle);
                    clearImageAccess(handle);
                } else {
                    log.error("Invalid len for GFXCMD_UNLOADIMAGE: {}", len);
                }
                break;
            case GFXCMD_SETTARGETSURFACE:
                // handle
                if (len == 4) {
                    int handle;
                    handle = readInt(0, cmddata);
                    windowManager.setTargetSurface(handle, (handle != 0) ? imageMap.get(handle) : null);
                } else {
                    log.error("Invalid len for GFXCMD_SETTARGETSURFACE: {}", len);
                }
                break;
            case GFXCMD_LOADFONT:
                // namelen, name, style, size
//			if (len >= 12 && len >= (12 + readInt(0, cmddata))) {
//				int namelen, style, size;
//				StringBuffer name = new StringBuffer();
//				int i;
//				int fonthandle = handleCount++;
//
//				namelen = readInt(0, cmddata);
//				for (i = 0; i < namelen - 1; i++) // skip the terminating \0
//													// character
//				{
//					name.append((char) cmddata[8 + i]); // an extra 4 for the
//														// header
//				}
//				style = readInt(namelen + 4, cmddata);
//				size = readInt(namelen + 8, cmddata);
//				FontHolder<?> fonty = windowManager.loadFont(name.toString(), style, size);
//				String cacheName = name.toString() + "-" + style;
//				if (myConn.hasFontServer()) {
//					// Check in the cache for the font since we load them from
//					// the server
//					if (!cachedFontMap.containsKey(cacheName)) {
//						// Fonts in the Dialog family are the default ones
//						// that come with Java that cause problems, so
//						// load this from the other way if it's here.
//						java.io.File cachedFile = getCachedImageFile(cacheName + "-" + myConn.getServerName());
//						if (cachedFile.isFile() && cachedFile.length() > 0) {
//							System.out.println("Loading font from cache for " + cacheName);
//							// We've got it locally in our cache! Read it
//							// from there.
//							java.io.FileInputStream fis = null;
//							try {
//								fis = new java.io.FileInputStream(cachedFile);
//								FontHolder<?> cacheFont = windowManager.createFont(fis);
//								fis.close();
//								fis = null;
//								cachedFontMap.put(cacheName, cacheFont);
//							} catch (java.io.IOException e1) {
//								System.out.println("ERROR loading font of:" + e1);
//							}
//						}
//					}
//
//					FontHolder<?> cachedFont = cachedFontMap.get(cacheName);
//					if (cachedFont != null) {
//						// Narflex: 5/11/06 - I'm not all that sure about
//						// this....but the data for character widths line up
//						// correctly
//						// when not applying the style here and don't line
//						// up if we do (for the default Java fonts).
//						// It makes sense because we're already caching
//						// fonts based on name + style so why would we need
//						// to
//						// re-apply the style? Unless there's the same font
//						// file for both...but that's the case with the Java
//						// fonts
//						// and applying the style there causes incorrect
//						// widths. Interesting...
//						fonty = windowManager.deriveFont(cachedFont, /* style, */(float) size);
//					} else {
//						// Return that we don't have this font so it'll load
//						// it into our cache
//						hasret[0] = 1;
//						return 0;
//					}
//				}
//				System.out.println("Loaded Font=" + fonty);
//				fontMap.put(fonthandle, fonty);
//				// fonthandle=STBGFX.GFX_loadFont(name.toString(), style, size);
//				hasret[0] = 1;
//				return fonthandle;
//			} else {
//				System.out.println("Invalid len for GFXCMD_LOADFONT : " + len);
//			}
                log.error("LOADFONT Not Implemented");
                break;
            case GFXCMD_UNLOADFONT:
                // handle
//			if (len == 4) {
//				int handle;
//				handle = readInt(0, cmddata);
//				// STBGFX.GFX_unloadFont(handle);
//				fontMap.remove(handle);
//			} else {
//				System.out.println("Invalid len for GFXCMD_UNLOADFONT : " + len);
//			}
                log.error("UNLOADFONT Not Implemented");
                break;
            case GFXCMD_LOADFONTSTREAM:
                // namelen, name, len, data
                if (len >= 8) {
                    StringBuffer name = new StringBuffer();
                    int namelen = readInt(0, cmddata);
                    for (int i = 0; i < namelen - 1; i++) // skip the terminating \0
                    // character
                    {
                        name.append((char) cmddata[8 + i]); // an extra 4 for the
                        // header
                    }
                    int datalen = readInt(4 + namelen, cmddata);
                    if (len >= datalen + 8 + namelen) {
                        log.debug("Saving font {} to cache", name);
                        saveCacheData(name.toString() + "-" + myConn.getServerName(), cmddata, 12 + namelen, datalen);
                    }
                } else {
                    log.error("Invalid len for GFXCMD_LOADFONTSTREAM: {}", len);
                }
                break;
            case GFXCMD_FLIPBUFFER:
                hasret[0] = 1;
                // STBGFX.GFX_flipBuffer();
                windowManager.flipBuffer();
                return 0;
            case GFXCMD_STARTFRAME:
                windowManager.startFrame();
                break;
            case GFXCMD_LOADIMAGELINE:
                // handle, line, len, data
                if (len >= 12 && len >= (12 + readInt(8, cmddata))) {
                    int handle, line, len2;
                    // unsigned char *data=&cmddata[12];
                    handle = readInt(0, cmddata);
                    line = readInt(4, cmddata);
                    len2 = readInt(8, cmddata);
                    windowManager.loadImageLine(handle, imageMap.get(handle), line, len2, cmddata);
                    registerImageAccess(handle);
                } else {
                    log.error("Invalid len for GFXCMD_LOADIMAGELINE: {}", len);
                }
                break;
            case GFXCMD_LOADIMAGECOMPRESSED:
                // handle, line, len, data
                if (len >= 8 && len >= (8 + readInt(4, cmddata))) {
                    int handle, len2;
                    handle = readInt(0, cmddata);
                    len2 = readInt(4, cmddata);
                    java.io.File cacheFile = null;
                    // Save this image to our disk cache
                    java.io.FileOutputStream fos = null;
                    boolean deleteCacheFile = false;
                    String resID = null;
                    try {
                        boolean cacheAdd = false;
                        if (lastImageResourceID != null && lastImageResourceIDHandle == handle) {
                            cacheFile = getCachedImageFile(lastImageResourceID, false);
                            resID = lastImageResourceID;
                            if (cacheFile != null && (!cacheFile.isFile() || cacheFile.length() == 0))
                                cacheAdd = true;
                        }
                        if (cacheFile == null) {
                            cacheFile = java.io.File.createTempFile("stv", "img");
                            deleteCacheFile = true;
                        }
                        fos = new java.io.FileOutputStream(cacheFile);
                        fos.write(cmddata, 12, len2); // an extra 4 for the header
                        if (cacheAdd) {
                            myConn.postOfflineCacheChange(true, lastImageResourceID);
                        }
                    } catch (java.io.IOException e) {
                        log.error("ERROR writing to cache file {}", cacheFile, e);
                        cacheFile = null;
                    } finally {
                        try {
                            if (fos != null)
                                fos.close();
                        } catch (Exception e) {
                        }
                        fos = null;
                    }
                    registerImageAccess(handle);
                    if (cacheFile != null) {
                        if (!doesUseAdvancedImageCaching()) {
                            handle = handleCount++;
                            hasret[0] = 1;
                        } else
                            hasret[0] = 0;
                        try {
                            sagex.miniclient.uibridge.ImageHolder<?> img = null;
                            // TODO Need a way to get real image size back
                            img = windowManager.readImage(cacheFile);
                            imageMap.put(handle, img);
                            imageCacheSize += img.getWidth() * img.getHeight() * 4;
                            if (deleteCacheFile)
                                cacheFile.delete();
                            return handle;
                        } catch (Exception e) {
                            log.error("ERROR loading compressed image", e);
                        }
                    }
                    if (deleteCacheFile && cacheFile != null)
                        cacheFile.delete();
                } else {
                    log.error("Invalid len for GFXCMD_LOADIMAGECOMPRESSED: {}", len);
                }
                break;
            case GFXCMD_XFMIMAGE:
                // srcHandle, destHandle, destWidth, destHeight, maskCornerArc
                if (len >= 20) {
                    int srcHandle, destHandle, destWidth, destHeight, maskCornerArc;
                    srcHandle = readInt(0, cmddata);
                    destHandle = readInt(4, cmddata);
                    destWidth = readInt(8, cmddata);
                    destHeight = readInt(12, cmddata);
                    maskCornerArc = readInt(16, cmddata);
                    int rvHandle = destHandle;
                    if (!doesUseAdvancedImageCaching()) {
                        rvHandle = handleCount++;
                        hasret[0] = 1;
                    } else
                        hasret[0] = 0;
                    sagex.miniclient.uibridge.ImageHolder srcImg = imageMap.get(srcHandle);
                    if ((hasret[0] == 1 && destWidth * destHeight * 4 + imageCacheSize > imageCacheLimit) || srcImg == null)
                        rvHandle = 0;
                    else {
                        sagex.miniclient.uibridge.ImageHolder destImg = windowManager.newImage(destWidth, destHeight);
                        imageMap.put(rvHandle, destImg);
                        imageCacheSize += destWidth * destHeight * 4;
                        windowManager.xfmImage(srcHandle, srcImg, destHandle, destImg, destWidth, destHeight, maskCornerArc);
                    }
                    return rvHandle;
                } else {
                    log.error("Invalid len for GFXCMD_XFMIMAGE: {}", len);
                }
                break;
            case GFXCMD_SETVIDEOPROP:
                if (len >= 40) {
                    Rectangle srcRect = new Rectangle(readInt(4, cmddata), readInt(8, cmddata),
                            readInt(12, cmddata),
                            readInt(16, cmddata));
                    Rectangle destRect = new Rectangle(readInt(20, cmddata), readInt(24,
                            cmddata),
                            readInt(28, cmddata), readInt(32, cmddata));
                    MediaCmd mc = client.getCurrentConnection().getMediaCmd();
                    if (mc != null) {
                        MiniPlayerPlugin playa = mc.getPlaya();
                        if (playa != null)
                            playa.setVideoRectangles(srcRect, destRect, false);
                    }
                    setVideoBounds(srcRect, destRect);
                } else {
                    log.error("Invalid len for GFXCMD_SETVIDEOPROP {}", len);
                }
                break;
            case GFXCMD_TEXTUREBATCH:
                if (len >= 8) {
                    int numCmds = readInt(0, cmddata);
                    int sizeCmds = readInt(4, cmddata);
                    log.debug("Texture batch command received count={} size={}", numCmds, sizeCmds);
                }
                break;
            default:
                return -1;
        }
        return 0;
    }

    private void postImageUnload(int oldestImage) {
        myConn.postImageUnload(oldestImage);
    }

    public boolean createVideo(int width, int height, int format) {
        return windowManager.createVideo(width, height, format);
    }

    public boolean updateVideo(int frametype, java.nio.ByteBuffer buf) {
        return windowManager.updateVideo(frametype, buf);
    }

    private void unloadImage(int handle) {
        ImageHolder bi = imageMap.get(handle);
        if (bi != null)
            imageCacheSize -= bi.getWidth() * bi.getHeight() * 4;
        imageMap.remove(handle);
        if (bi != null)
            bi.flush();
        clearImageAccess(handle);
        windowManager.unloadImage(handle, bi);
    }

    public String getVideoOutParams() {
        return null;
    }

    public java.io.File getCachedImageFile(String resourceID) {
        return getCachedImageFile(resourceID, true);
    }

    public java.io.File getCachedImageFile(String resourceID, boolean verify) {
        if (cacheDir == null)
            return null;
        java.io.File cachedFile = new java.io.File(cacheDir, resourceID);
        return (!verify || (cachedFile.isFile() && cachedFile.length() > 0)) ? cachedFile : null;
    }

    public void saveCacheData(String resourceID, byte[] data, int offset, int length) {
        if (cacheDir == null)
            return;
        java.io.FileOutputStream fos = null;
        try {
            log.debug("Writing Cached Image: {}", resourceID);
            fos = new java.io.FileOutputStream(new java.io.File(cacheDir, resourceID));
            fos.write(data, offset, length);
            fos.flush();
        } catch (java.io.IOException ioe) {
            log.error("ERROR writing cache data to file", ioe);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private String getOfflineCacheList() {
        if (cacheDir == null)
            return "";
        StringBuffer sb = new StringBuffer();
        java.io.File[] cacheFiles = cacheDir.listFiles();
        for (int i = 0; cacheFiles != null && i < cacheFiles.length; i++) {
            sb.append(cacheFiles[i].getName());
            sb.append("|");
        }
        return sb.toString();
    }

    private void cleanupOfflineCache() {
        // Cleanup the offline cache...just dump the oldest half of it
        java.io.File[] cacheFiles = cacheDir.listFiles();
        long size = 0;
        for (int i = 0; i < cacheFiles.length; i++) {
            size += cacheFiles[i].length();
            if (size > offlineImageCacheLimit) {
                log.info("Dumping offline image cache because it's exceeded the maximum size");
                java.util.Arrays.sort(cacheFiles, new java.util.Comparator() {
                    public int compare(Object o1, Object o2) {
                        java.io.File f1 = (java.io.File) o1;
                        java.io.File f2 = (java.io.File) o2;
                        long l1 = f1.lastModified();
                        long l2 = f2.lastModified();
                        if (l1 < l2)
                            return -1;
                        else if (l1 > l2)
                            return 1;
                        else
                            return 0;
                    }
                });
                for (int j = 0; j < cacheFiles.length / 2; j++)
                    cacheFiles[j].delete();
                break;
            }
        }
    }

    public void registerImageAccess(int handle) {
        lruImageMap.put(new Integer(handle), new Long(System.currentTimeMillis()));
    }

    public void clearImageAccess(int handle) {
        lruImageMap.remove(new Integer(handle));
    }

    public int getOldestImage() {
        java.util.Iterator walker = lruImageMap.entrySet().iterator();
        Integer oldestHandle = null;
        long oldestTime = Long.MAX_VALUE;
        while (walker.hasNext()) {
            java.util.Map.Entry ent = (java.util.Map.Entry) walker.next();
            long currTime = ((Long) ent.getValue()).longValue();
            if (currTime < oldestTime) {
                oldestTime = currTime;
                oldestHandle = (Integer) ent.getKey();
            }
        }
        return (oldestHandle == null) ? 0 : oldestHandle.intValue();
    }

    public boolean doesUseAdvancedImageCaching() {
        return usesAdvancedImageCaching;
    }

    public Dimension getScreenSize() {
        return this.windowManager.getScreenSize();
    }

    public UIRenderer<?> getWindow() {
        return windowManager;
    }

    public void setVideoBounds(Object o, Object o1) {
        windowManager.setVideoBounds(o, o1);
    }
}
