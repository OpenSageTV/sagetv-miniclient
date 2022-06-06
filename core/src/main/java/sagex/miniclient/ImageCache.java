package sagex.miniclient;

import java.io.File;

import sagex.miniclient.logging.ILogger;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.ImageHolder;
import sagex.miniclient.util.Utils;
import sagex.miniclient.util.VerboseLogging;

/**
 * Encapsulate Image Caching into a single manager
 */
public class ImageCache
{
    private long imageCacheLimit;
    private long offlineImageCacheLimit;
    private File cacheDir;
    private final MiniClient client;
    private long imageCacheSize;
    private java.util.Map<Integer, Long> lruImageMap = new java.util.HashMap<Integer, Long>();
    private java.util.Map<Integer, sagex.miniclient.uibridge.ImageHolder> imageMap = new java.util.HashMap<Integer, sagex.miniclient.uibridge.ImageHolder>();
    private ILogger log;

    public ImageCache(MiniClient client, ILogger log)
    {
        this.log = log;
        this.client=client;
        reloadSettings();
    }

    /**
     * NOTE: in the cases where this is used, if this returns false, then sagetv wills start sending
     * unload image requests to make room.
     *
     * @param width
     * @param height
     * @return
     */
    public boolean canCache(int width, int height)
    {
        boolean canDo =  (width * height * 4 + imageCacheSize) <= imageCacheLimit;
        if (!canDo)
        {
            log.logDebug("Can't cache {" + width + " }x{" + height + "} (" + Utils.toMB(width*height*4) + "mb).  Not enough room.  Cache Size: " + Utils.toMB(imageCacheSize) + "; Cache Limit: " + Utils.toMB(imageCacheLimit));
        }
        return canDo;
    }

    public void cleanUp()
    {
        log.logDebug("Resetting up in-memory cache states");
        imageCacheSize=0;
        lruImageMap.clear();
        if (imageMap.size()>0)
        {
            for (ImageHolder ih : imageMap.values())
            {
                ih.dispose();
            }
        }
        imageMap.clear();
        cleanupOfflineCache();
    }

    public ImageHolder get(int handle)
    {
        ImageHolder h = imageMap.get(handle);
        if (h != null && handle!=h.getHandle())
        {
            log.logError("ImageCache: Error: We asked for " + handle +", but got" + h.getHandle());
        }
        return h;
    }

    public void put(int imghandle, ImageHolder<?> img, int width, int height) {

        if (img.getHandle()!= imghandle)
        {
            log.logWarning("ImageCache.put(" + imghandle + ") has image with different handle " + img.getHandle(), new Exception());
        }
        imageMap.put(imghandle, img);
        imageCacheSize += width * height * 4;

        if (VerboseLogging.DETAILED_IMAGE_CACHE)
        {
            log.logDebug("Added " + imghandle + " with size: " + width + "x" + height + " (" + Utils.toMB(width*height*4) + "mb); Cache " + Utils.toMB(imageCacheSize) + "mb/" + Utils.toMB(imageCacheLimit)+ "mb)");
        }
    }

    public void makeRoom(int width, int height)
    {
        if (VerboseLogging.DETAILED_IMAGE_CACHE)
        {
            if (!canCache(width, height))
            {
                log.logDebug("Can't cache " + width + "x" + height + " (" + Utils.toMB(width*height*4)+ "mb).  Will make room.");
            }
        }

        while (width * height * 4 + imageCacheSize > imageCacheLimit)
        {
            // Keep freeing the oldest image until we have enough memory
            // to do this
            int oldestImage = getOldestImage();
            if (oldestImage != 0)
            {
                if (VerboseLogging.DETAILED_IMAGE_CACHE)
                {
                    log.logDebug("Freeing image to make room in cache");
                }
                unloadImage(oldestImage);
                postImageUnload(oldestImage);
                clearImageAccess(oldestImage);
            }
            else
            {
                log.logError("ERROR cannot free enough from the cache to support loading a new image!!!");
                break;
            }
        }
        if (VerboseLogging.DETAILED_IMAGE_CACHE)
        {
            log.logDebug("Cache " + Utils.toMB(imageCacheSize) + "mb/" + Utils.toMB(imageCacheSize) + "mb");
        }
    }

    public void postImageUnload(int oldestImage)
    {
        client.getCurrentConnection().postImageUnload(oldestImage);
    }

    public void unloadImage(int handle) {
        ImageHolder bi = imageMap.get(handle);
        imageMap.remove(handle);
        if (bi != null) {
            imageCacheSize -= (bi.getWidth() * bi.getHeight() * 4);
            if (VerboseLogging.DETAILED_IMAGE_CACHE) {

                log.logDebug("Unloaded " + handle + " with size: " + bi.getWidth() + "x" +  bi.getHeight() + " (" + Utils.toMB(bi.getWidth() *bi.getHeight()*4) + "mb); Cache " + Utils.toMB(imageCacheSize) + "mb/" + Utils.toMB(imageCacheLimit)+ "mb)");
            }
            bi.dispose();
        } else {
            if (VerboseLogging.DETAILED_IMAGE_CACHE) {
                log.logDebug("Unloaded: " + handle + ", but was not in the cache" );
            }
        }
        clearImageAccess(handle);
        client.getUIRenderer().unloadImage(handle, bi);
    }

    public java.io.File getCachedImageFile(String resourceID)
    {
        return getCachedImageFile(resourceID, true);
    }

    public java.io.File getCachedImageFile(String resourceID, boolean verify)
    {
        if (cacheDir == null)
        {
            return null;
        }

        java.io.File cachedFile = new java.io.File(cacheDir, resourceID);

        return (!verify || (cachedFile.isFile() && cachedFile.length() > 0)) ? cachedFile : null;
    }

    public void saveCacheData(String resourceID, byte[] data, int offset, int length)
    {
        if (cacheDir == null)
        {
            return;
        }

        java.io.FileOutputStream fos = null;

        try
        {
            if (VerboseLogging.DETAILED_IMAGE_CACHE)
            {
                log.logDebug("Writing Cached Image: " + resourceID);
            }

            fos = new java.io.FileOutputStream(new java.io.File(cacheDir, resourceID));
            fos.write(data, offset, length);
            fos.flush();

        }
        catch (java.io.IOException ioe)
        {
            log.logError("ERROR writing cache data to file", ioe);
        }
        finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    public String getOfflineCacheList()
    {
        if (cacheDir == null)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        java.io.File[] cacheFiles = cacheDir.listFiles();

        for (int i = 0; cacheFiles != null && i < cacheFiles.length; i++)
        {
            sb.append(cacheFiles[i].getName());
            sb.append("|");
        }
        return sb.toString();
    }

    public void cleanupOfflineCache()
    {
        // Cleanup the offline cache...just dump the oldest half of it
        java.io.File[] cacheFiles = cacheDir.listFiles();

        if (cacheFiles==null) return;
        long size = 0;

        for (int i = 0; i < cacheFiles.length; i++)
        {
            size += cacheFiles[i].length();

            if (size > offlineImageCacheLimit)
            {
                log.logInfo("Dumping offline image cache because it's exceeded the maximum size");

                java.util.Arrays.sort(cacheFiles, new java.util.Comparator()
                {
                    public int compare(Object o1, Object o2)
                    {
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
                {
                    if (VerboseLogging.DETAILED_IMAGE_CACHE)
                    {
                        log.logDebug("Removing Image From Disk: " + cacheFiles[j]);
                    }
                    cacheFiles[j].delete();
                }
                break;
            }
        }
    }

    public void registerImageAccess(int handle)
    {
        lruImageMap.put(handle, System.currentTimeMillis());
    }

    public void clearImageAccess(int handle)
    {
        lruImageMap.remove(handle);
    }

    public int getOldestImage()
    {
        java.util.Iterator walker = lruImageMap.entrySet().iterator();
        Integer oldestHandle = null;
        long oldestTime = Long.MAX_VALUE;

        while (walker.hasNext())
        {
            java.util.Map.Entry ent = (java.util.Map.Entry) walker.next();
            long currTime = ((Long) ent.getValue()).longValue();

            if (currTime < oldestTime)
            {
                oldestTime = currTime;
                oldestHandle = (Integer) ent.getKey();
            }
        }

        return (oldestHandle == null) ? 0 : oldestHandle;
    }

    public void reloadSettings()
    {
        imageCacheLimit = client.properties().getLong(PrefStore.Keys.image_cache_size_mb, 64)*1024*1024;
        offlineImageCacheLimit = client.properties().getLong(PrefStore.Keys.disk_image_cache_size_mb, 512) * 1024*1024;

        if (client.properties().getBoolean(PrefStore.Keys.cache_images_on_disk, true))
        {
            cacheDir = new java.io.File(client.options().getCacheDir(), "imgcache");
            cacheDir.mkdir();
        }
        else
        {
            cacheDir = null;
        }

        // make sure caches are cleared
        cleanUp();

        log.logInfo("GFXCMD2 Created:  Mem Cache Size: " + Utils.toMB(imageCacheLimit) + "mb, Disk Cache Size: " + Utils.toMB(offlineImageCacheLimit) + "mb, Cache Location: " + cacheDir);
        log.logDebug("Disk Cache Contents: " + getOfflineCacheList());
        log.logDebug("Max Memory: " + Utils.toMB(Runtime.getRuntime().maxMemory()) + "mb");
    }
}
