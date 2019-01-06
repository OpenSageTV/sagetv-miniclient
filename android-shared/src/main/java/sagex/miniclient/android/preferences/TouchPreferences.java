package sagex.miniclient.android.preferences;

import sagex.miniclient.SageCommand;
import sagex.miniclient.prefs.PrefStore;

public class TouchPreferences {
    private PrefStore preferences;

    public TouchPreferences(PrefStore store) {
        preferences = store;
    }


    public int getEdgeSizePixels() {
        return preferences.getInt("edge_size", 50);
    }

    public int getHotspotSizePixels() {
        return preferences.getInt("hotspot_size", 50);
    }

    public SageCommand getOnDoubleTap() {
        String key = preferences.getString("double_tap", SageCommand.OPTIONS.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getOnDoubleTap2() {
        String key = preferences.getString("double_tap_2", SageCommand.OPTIONS.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getOnDoubleTap3() {
        String key = preferences.getString("double_tap_3", SageCommand.OPTIONS.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getSingleSwipeRight() {
        String key = preferences.getString("swipe_right", SageCommand.RIGHT.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getSingleSwipeLeft() {
        String key = preferences.getString("swipe_left", SageCommand.LEFT.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getSingleSwipeUp() {
        String key = preferences.getString("swipe_up", SageCommand.UP.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getSingleSwipeDown() {
        String key = preferences.getString("swipe_down", SageCommand.DOWN.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getDoubleSwipeRight() {
        String key = preferences.getString("swipe_right_2", SageCommand.SCROLL_RIGHT.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getDoubleSwipeLeft() {
        String key = preferences.getString("swipe_left_2", SageCommand.SCROLL_LEFT.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getDoubleSwipeUp() {
        String key = preferences.getString("swipe_up_2", SageCommand.SCROLL_UP.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getDoubleSwipeDown() {
        String key = preferences.getString("swipe_down_2", SageCommand.SCROLL_DOWN.getKey());

        return SageCommand.parseByKey(key);
    }


    public SageCommand getTripleSwipeRight() {
        String key = preferences.getString("swipe_right_3", SageCommand.HOME.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getTripleSwipeLeft() {
        String key = preferences.getString("swipe_left_3", SageCommand.BACK.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getTrippleSwipeUp() {
        String key = preferences.getString("swipe_up_3", SageCommand.OPTIONS.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getTripleSwipeDown() {
        String key = preferences.getString("swipe_down_3", SageCommand.INFO.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getEdgeSwipeLeft() {
        String key = preferences.getString("edge_swipe_left", SageCommand.NAV_OSD.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getEdgeSwipeRight() {
        String key = preferences.getString("edge_swipe_right", SageCommand.NONE.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getEdgeSwipeTop() {
        String key = preferences.getString("edge_swipe_top", SageCommand.NONE.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getEdgeSwipeBottom() {
        String key = preferences.getString("edge_swipe_bottom", SageCommand.KEYBOARD_OSD.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getHotspotTopLeft() {
        String key = preferences.getString("hotspot_top_left", SageCommand.KEYBOARD_OSD.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getHotspotTopRight() {
        String key = preferences.getString("hotspot_top_right", SageCommand.NONE.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getHotspotBottomRight() {
        String key = preferences.getString("hotspot_bottom_right", SageCommand.OPTIONS.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getHotspotBottomLeft() {
        String key = preferences.getString("hotspot_bottom_left", SageCommand.NAV_OSD.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getLongPress() {
        String key = preferences.getString("long_press", SageCommand.OPTIONS.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getDoubleLongPress() {
        String key = preferences.getString("long_press_2", SageCommand.NONE.getKey());
        return SageCommand.parseByKey(key);
    }

    public SageCommand getTripleLongPress() {
        String key = preferences.getString("long_press_3", SageCommand.NONE.getKey());
        return SageCommand.parseByKey(key);
    }
}
