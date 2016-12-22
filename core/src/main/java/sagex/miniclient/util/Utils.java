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

    public static float toMB(float value) {
        return round2(value/1024f/1024f, 2);
    }

    public static float round2(float number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        float tmp = number * pow;
        return (float) (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
    }
}
