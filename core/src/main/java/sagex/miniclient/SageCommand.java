package sagex.miniclient;

import java.util.ArrayList;

/**
 * This class is used to represent a sage user action.
 *
 * @author jvl711
 */

public enum SageCommand
{
    FORCE_QUIT(-5, "FORCE_QUIT", "FORCE_QUIT", "FORCE_QUIT", -1, false),
    KEYBOARD_OSD(-4, "KEYBOARD_OSD", "KEYBOARD_OSD", "Show Keyboard", -1, true),
    NAV_OSD(-3, "NAV_OSD", "NAV_OSD", "Show OSD Navigation", -1, true),
    NONE(-2, "NONE", "NONE", "NONE", -1, true),
    UNKNOWN(-1, "?", "?", "?", -1, false),
    RAW_KEYBOARD(0, "RAW_KEYBOARD", "RAW_KEYBOARD", "RAW_KEYBOARD", -1, false), //Command is raw keyboard
    RAW_IR(1, "RAW_IR", "RAW_IR", "RAW_IR", -1, false), //Commnad is raw IR code
    LEFT(2, "left", "Command_Left", "Left", -8616, true),
    RIGHT(3, "right", "Command_Right", "Right", -8612, true),
    UP(4, "up", "Command_Up", "Up", -8624, true),
    DOWN(5, "down", "Command_Down", "Down", -8620, true),
    PAUSE(6, "pause", "Command_Pause", "Pause", -8512, true), // s
    PLAY(7, "play", "Command_Play", "Play", -8492, true), // d
    FF(8, "ff", "Command_Skip_Fwd_Page_Right", "Skip Fwd/Page Right", -8496, true), // f
    REW(9, "rew", "Command_Skip_Bkwd_Page_Left", "Skip Bkwd/Page Left", -8504, true), // g
    CHANNEL_UP(11, "ch_up", "Command_Channel_Up_Page_Up","Channel Up/Page Up", -8576, true), // PgUp
    CHANNEL_DOWN(12, "ch_down", "Command_Channel_Down_Page_Down", "Channel Down/Page Down", -8572, true), // PgDn
    VOLUME_UP(13, "vol_up", "Command_Volume_Up", "Volume Up", -8640, true), // r
    VOLUME_DOWN(14, "vol_down", "Command_Volume_Down", "Volume Down", -8636, true), // e
    
    TV(15, "tv", "Command_TV", "TV", -8592, true), // v
    FASTER(16, "faster", "Command_Play_Faster", "Play Faster", -1, true), // m
    SLOWER(17, "slower", "Command_Play_Slower", "Play Slower", -1, true), // n
    GUIDE(18, "guide", "Command_Guide", "Guide", -8596, true), // x
    POWER(19, "power", "Command_Power", "Power", -8460, true), // z
    SELECT(20, "select", "Command_Select", "Select", -8556, true), // Enter
    WATCHED(21, "watched", "Command_Watched", "Watched", -8540, true), // w
    RATE_UP(22, "like", "Command_Favorite", "Favorite", -8520, true), // k
    RATE_DOWN(23,"dont_like", "Command_Dont_Like", "Don't Like", -8660, true), // j
    INFO(24, "info", "Command_Info", "Info", -8652, true), // i
    RECORD(25, "record", "Command_Record", "Record", -8484, true), // t
    MUTE(26, "mute", "Command_Mute", "Mute", -8644, true), //
    FULL_SCREEN(27, "full_screen", "Command_Full_Screen", "Full Screen", -1, true), // Ctrl-F
    HOME(28, "home", "Command_Home", "Home", -8468, true), // home
    OPTIONS(29, "options", "Command_Options", "Options", -8480, true), // o
    
    NUM0(30, "0", "Command_Num_0", "Num 0", -8704, true),
    NUM1(31, "1", "Command_Num_1", "Num 1", -8700, true),
    NUM2(32, "2", "Command_Num_2", "Num 2", -8696, true),
    NUM3(33, "3", "Command_Num_3", "Num 3", -8692, true),
    NUM4(34, "4", "Command_Num_4", "Num 4", -8688, true),
    NUM5(35, "5", "Command_Num_5", "Num 5", -8684, true),
    NUM6(36, "6", "Command_Num_6", "Num 6", -8680, true),
    NUM7(37, "7", "Command_Num_7", "Num 7", -8676, true),
    NUM8(38, "8", "Command_Num_8", "Num 8", -8672, true),
    NUM9(39, "9", "Command_Num_9", "Num 9", -8668, true),
    SEARCH(40, "search", "Command_Search", "Search", -1, true),
    SETUP(41, "setup", "Command_Setup", "Setup", -1, true),
    LIBRARY(42, "library", "Command_Library", "Library", -1, true),
    POWER_ON(43, "power_on", "Command_Power_On", "Power On", -1, true),
    POWER_OFF(44, "power_off", "Command_Power_Off", "Power Off", -1, true),
    MUTE_ON(45, "mute_on", "Command_Mute_On", "Mute On", -1, true),
    MUTE_OFF(46, "mute_off", "Command_Mute_Off", "Mute Off", -1, true),
    AR_FILL(47, "ar_fill", "Command_Aspect_Ratio_Fill", "Aspect Ratio Fill", -1, true),
    AR_4X3(48, "ar_4x3", "Command_Aspect_Ratio_4x3", "Aspect Ratio 4x3", -1, true),
    AR_16X9(49, "ar_16x9", "Command_Aspect_Ratio_16x9", "Aspect Ratio 16x9", -1, true),
    AR_SOURCE(50, "ar_source", "Command_Aspect_Ratio_Source", "Aspect Ratio Source", -1, true),
    VOLUME_UP2(51, "vol_up2", "Command_Right_Volume_Up", "Right/Volume Up", -1, true),
    VOLUME_DOWN2(52, "vol_down2", "Command_Page_Down", "Left/Volume Down", -1, true),
    CHANNEL_UP2(53, "ch_up2","Command_Up_Channel_Up", "Up/Channel Up", -1, true),
    CHANNEL_DOWN2(54, "ch_down2", "Command_Down_Channel_Down", "Down/Channel Down", -1, true),
    PAGE_UP(55, "page_up", "Command_Page_Up", "Page Up", -1, true),
    PAGE_DOWN(56, "page_down", "Command_Page_Down", "Page Down", -1, true),
    PAGE_RIGHT(57, "page_right", "Command_Page_Right", "Page Right", -1, true),
    PAGE_LEFT(58, "page_left", "Command_Page_Left", "Page Left", -1, true),
    PLAY_PAUSE(59, "play_pause", "Command_Play_Pause", "Play/Pause", -1, true),
    PREV_CHANNEL(60, "prev_channel", "Command_Previous_Channel", "Previous Channel", -8632, true),
    FF_2(61, "ff_2", "Command_Skip_Fwd_2", "Skip Fwd #2", -8584, true),
    REW_2(62, "rew_2", "Command_Skip_Bkwd_2", "Skip Bkwd #2", -8560, true),
    LIVE_TV(63, "live_tv", "Command_Live_TV", "Live TV", -1, true),
    DVD_REVERSE_PLAY(64, "dvd_reverse", "Command_DVD_Reverse_Play", "DVD Reverse Play", -1, true),
    DVD_CHAPTER_NEXT(65, "dvd_chapter_up", "Command_DVD_Next_Chapter", "DVD Next Chapter", -1, true),
    DVD_CHAPTER_PREV(66, "dvd_chapter_down", "Command_DVD_Prev_Chapter", "DVD Prev Chapter", -1, true),
    DVD_MENU(67, "dvd_menu", "Command_DVD_Menu", "DVD Menu", -1, true),
    DVD_TITLE_MENU(68, "dvd_title_menu", "Command_DVD_Title_Menu", "DVD Title Menu", -1, true),
    DVD_RETURN(69, "dvd_return", "Command_DVD_Return", "DVD Return", -1, true),
    DVD_SUBTITLE_CHANGE(70, "dvd_subtitle_change", "Command_DVD_Subtitle_Change", "DVD Subtitle Change", -1, true),
    DVD_SUBTITLE_TOGGLE(71, "dvd_subtitle_toggle", "Command_DVD_Subtitle_Toggle", "DVD Subtitle Toggle", -1, true),
    DVD_AUDIO_CHANGE(72, "dvd_audio_change", "Command_DVD_Audio_Change", "DVD Audio Change", -1, true),
    DVD_ANGLE_CHANGE(73, "dvd_angle_change", "Command_DVD_Angle_Change", "DVD Angle Change", -1, true),
    DVD(74, "dvd", "Command_DVD", "DVD", -1, true),
    BACK(75, "back", "Command_Back", "Back", -8580, true),
    FORWARD(76, "forward", "Command_Forward", "Forward", -1, true),
    CUSTOMIZE(77, "customize", "Command_Customize", "Customize", -1, true),
    CUSTOM1(78, "custom1", "Command_Custom1", "Custom1", -1, true),
    CUSTOM2(79, "custom2", "Command_Custom2", "Custom2", -1, true),
    CUSTOM3(80, "custom3", "Command_Custom3", "Custom3", -1, true),
    CUSTOM4(81, "custom4", "Command_Custom4", "Custom4", -1, true),
    CUSTOM5(82, "custom5", "Command_Custom5", "Custom5", -1, true),
    DELETE(83, "delete", "Command_Delete", "Delete", -1, true),
    MUSIC(84, "music", "Command_Music_Jukebox", "Music Jukebox", -8604, true),
    SCHEDULE(85, "schedule", "Command_Recording_Schedule", "Recording Schedule", -1, true),
    RECORDINGS(86, "recordings", "Command_SageTV_Recording", "SageTV Recordings", -8608, true),
    PICTURE_LIBRARY(87, "picture_library", "Command_Picture_Library", "Picture Library", -8600, true),
    VIDEO_LIBRARY(88, "video_library", "Command_Video_Library", "Video Library", -1, true),
    STOP(89, "stop", "Command_Stop", "Stop", -8488, true),
    EJECT(90, "eject", "Command_Eject", "Eject", -1, true),
    STOP_EJECT(91, "stop_eject", "Command_Stop_Eject", "Stop/Eject", -1, true),
    INPUT(92, "input", "Command_Input", "Input", -1, true),
    SMOOTH_FF(93, "smooth_ff", "Command_Smooth_FF", "Smooth Fast Forward", -1, true),
    SMOOTH_REW(94, "smooth_rew", "Command_Smooth_Rew", "Smooth Rewind", -1, true),
    DASH(95, "dash", "-", "-", -1, true),
    AR_TOGGLE(96, "ar_toggle", "Command_Aspect_Ratio_Toggle", "Aspect Ratio Toggle", -1, true),
    FULL_SCREEN_ON(97, "full_screen_on", "Command_Full_Screen_On", "Full Screen On", -1, true),
    FULL_SCREEN_OFF(98, "full_screen_off", "Command_Full_Screen_Off", "Full Screen Off", -1, true),
    RIGHT_FF(99, "right_ff", "Command_Right_Skip_Fwd", "Right/Skip Fwd", -1, true),
    LEFT_REW(100, "right_rew", "Command_Left_Skip_Bkwd", "Left/Skip Bkwd", -1, true),
    UP_VOL_UP(101, "up_vol_up", "Command_Up_Volume_Up", "Up/Volume Up", -1, true),
    DOWN_VOL_DOWN(102, "down_vol_down", "Command_Down_Volume_Down", "Down/Volume Down", -1, true),
    ONLINE(103, "online", "Command_Online", "Online", -1, true),
    VIDEO_OUTPUT(104, "video_output", "Command_Video_Output", "Video Output", -1, true),
    SCROLL_LEFT(105, "scroll_left", "Command_Scroll_Left", "Scroll Left", -1, true),
    SCROLL_RIGHT(106, "scroll_right", "Command_Scroll_Right", "Scroll Right", -1, true),
    SCROLL_UP(107, "scroll_up", "Command_Scroll_Up", "Scroll Up", -1, true),
    SCROLL_DOWN(108, "scroll_down", "Command_Scroll_Down", "Scroll Down", -1, true),
    ANYTHING(109, "anything", "Command_Anything", "Anything", -1, true);

    private static String [] keysCacheAll;
    private static String [] displayNamesCacheAll;
    private static String [] keysCacheDisplay;
    private static String [] displayNamesCacheDisplay;

    private final int eventid;
    private final String name;
    private final String translation_uenames;
    private final String displayName;
    private final int ir_code;
    private boolean display;

    
    private SageCommand(int eventid, String name, String translation_uenames, String displayName, int ir_code, boolean display)
    {
        this.eventid = eventid;
        this.name = name;
        this.translation_uenames = translation_uenames;
        this.displayName = displayName;
        this.ir_code = ir_code;
        this.display = display;

    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public boolean isDisplayable()
    {
        return display;
    }

    public String getKey()
    {
        return this.name;
    }

    public int getEventCode()
    {
        return this.eventid;
    }

    public static String [] getDisplayNames()
    {
        return getDisplayNames(true);
    }

    public static String [] getDisplayNames(boolean includeHidden)
    {

        if(includeHidden)
        {
            if (SageCommand.displayNamesCacheAll == null)
            {

                SageCommand[] commands = SageCommand.values();
                String[] displayNames = new String[commands.length];

                for (int i = 0; i < commands.length; i++)
                {
                    displayNames[i] = commands[i].getDisplayName();
                }

                SageCommand.displayNamesCacheAll = displayNames;
                return displayNames;
            }
            else
            {
                return SageCommand.displayNamesCacheAll;
            }
        }
        else
        {
            if (SageCommand.displayNamesCacheDisplay == null)
            {
                ArrayList<String> values = new ArrayList<String>();
                SageCommand[] commands = SageCommand.values();

                for (int i = 0; i < commands.length; i++)
                {
                    if(commands[i].isDisplayable())
                    {
                        values.add(commands[i].getDisplayName());
                    }
                }

                SageCommand.displayNamesCacheDisplay = new String[values.size()];
                SageCommand.displayNamesCacheDisplay = values.toArray(displayNamesCacheDisplay);
                return  SageCommand.displayNamesCacheDisplay;
            }
            else
            {
                return SageCommand.displayNamesCacheDisplay;
            }
        }
    }

    public static String [] getKeys()
    {
        return getKeys(true);
    }

    public static String [] getKeys(boolean includeHidden)
    {
        if(includeHidden)
        {

            if(SageCommand.keysCacheAll == null)
            {
                SageCommand[] commands = SageCommand.values();
                String[] keys = new String[commands.length];

                for (int i = 0; i < commands.length; i++)
                {
                    keys[i] = commands[i].getKey();
                }

                SageCommand.keysCacheAll = keys;

                return keys;
            }
            else
            {
                return  SageCommand.keysCacheAll;
            }
        }
        else
        {
            if(SageCommand.keysCacheDisplay == null)
            {
                SageCommand[] commands = SageCommand.values();
                ArrayList<String> values = new ArrayList<String>();

                for(int i = 0; i < commands.length; i++)
                {
                    if(commands[i].isDisplayable())
                    {
                        values.add(commands[i].getKey());
                    }
                }

                SageCommand.keysCacheDisplay = new String[values.size()];
                SageCommand.keysCacheDisplay = values.toArray(SageCommand.keysCacheDisplay);
                return SageCommand.keysCacheDisplay;
            }
            else
            {
               return SageCommand.keysCacheDisplay;
            }
        }
    }

    public static SageCommand parseByKey(String key)
    {
        SageCommand parsed = SageCommand.UNKNOWN;
        
        SageCommand [] commands = SageCommand.values();
        
        for(int i = 0 ; i < commands.length; i++)
        {
            if(key.equalsIgnoreCase(commands[i].getKey()))
            {
                parsed = commands[i];
            }
        }
        
        return parsed;
    }
    
    public static SageCommand parseByID(int id)
    {
        SageCommand parsed = SageCommand.UNKNOWN;
        
        SageCommand [] commands = SageCommand.values();
        
        for(int i = 0 ; i < commands.length; i++)
        {
            if(id == commands[i].eventid)
            {
                parsed = commands[i];
            }
        }
        
        return parsed;
    }
    
}

