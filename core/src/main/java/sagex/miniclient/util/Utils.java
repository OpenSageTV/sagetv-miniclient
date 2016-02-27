package sagex.miniclient.util;

/**
 * Created by seans on 06/02/16.
 */
public class Utils {
    /**
     * Returns true if the passed in ID is a SageTV GUID
     *
     * @param id
     * @return
     */
    public static boolean isGUID(String id) {
        return id != null && id.length() == 19 && id.split("-").length == 4;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }
}
