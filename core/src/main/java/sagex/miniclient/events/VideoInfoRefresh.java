package sagex.miniclient.events;

/**
 * Message to request current information about current video.  Generally this will result in a
 * VideoInfoResponse being sent as a reply.
 */
public class VideoInfoRefresh {
    public static final VideoInfoRefresh INSTANCE = new VideoInfoRefresh();
}
