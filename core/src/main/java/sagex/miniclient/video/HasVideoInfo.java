package sagex.miniclient.video;

/**
 * Created by seans on 19/12/16.
 */

public interface HasVideoInfo {
    /**
     * Return Null if no Video Info, other return the current Video information
     *
     * @return
     */
    VideoInfoResponse getVideoInfo();
}
