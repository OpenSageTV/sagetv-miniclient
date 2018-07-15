package sagex.miniclient.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

import sagex.miniclient.SageCommand;

public class TouchPreferences
{
    private Context context;
    private SharedPreferences preferences;

    public TouchPreferences(Context context)
    {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    public SageCommand getSingleSwipeRight()
    {
        String key = preferences.getString("swipe_right", SageCommand.RIGHT.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getSingleSwipeLeft()
    {
        String key = preferences.getString("swipe_left", SageCommand.LEFT.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getSingleSwipeUp()
    {
        String key = preferences.getString("swipe_up", SageCommand.UP.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getSingleSwipeDown()
    {
        String key = preferences.getString("swipe_down", SageCommand.DOWN.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getDoubleSwipeRight()
    {
        String key = preferences.getString("swipe_right_2", SageCommand.SCROLL_RIGHT.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getDoubleSwipeLeft()
    {
        String key = preferences.getString("swipe_left_2", SageCommand.SCROLL_LEFT.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getDoubleSwipeUp()
    {
        String key = preferences.getString("swipe_up_2", SageCommand.SCROLL_UP.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getDoubleSwipeDown()
    {
        String key = preferences.getString("swipe_down_2", SageCommand.SCROLL_DOWN.getKey());

        return SageCommand.parseByKey(key);
    }


    public SageCommand getTripleSwipeRight()
    {
        String key = preferences.getString("swipe_right_3", SageCommand.HOME.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getTripleSwipeLeft()
    {
        String key = preferences.getString("swipe_left_3", SageCommand.BACK.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getTrippleSwipeUp()
    {
        String key = preferences.getString("swipe_up_3", SageCommand.OPTIONS.getKey());

        return SageCommand.parseByKey(key);
    }

    public SageCommand getTripleSwipeDown()
    {
        String key = preferences.getString("swipe_down_3", SageCommand.INFO.getKey());

        return SageCommand.parseByKey(key);
    }
}
