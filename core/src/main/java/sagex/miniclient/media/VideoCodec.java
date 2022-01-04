package sagex.miniclient.media;

import java.util.ArrayList;
import java.util.List;

public enum VideoCodec
{
    MPEG1("MPEG1", new String[]{"MPEG1-VIDEO"}, new String[]{"video/mpeg"}),
    MPEG2("MPEG2", new String[]{"MPEG2-VIDEO", "MPEG2-VIDEO@HL"}, new String[]{"video/mpeg2"}),
    H263("H.263", new String[]{"H.263"}, new String[]{"video/3gpp"}),
    MPEG4("MPEG4", new String[]{"MPEG4-VIDEO", "MSMPEG4-VIDEO"}, new String[]{"video/mp4v-es"}),
    H264("H.264", new String[]{"H.264"}, new String[]{"video/avc"}),
    VC1("VC1", new String[]{"VC1"}, new String[]{"video/x-ms-wmv","video/wvc1"}),
    HEVC("HEVC", new String[]{"HEVC"}, new String[]{"video/hevc"}),
    MJPEG("MJPEG", new String[]{"MJPEG"}, new String[]{"video/mjpeg"}),
    VP8("VP8", new String[]{"VP8"}, new String[]{"video/x-vnd.on2.vp8"}),
    VP9("VP9", new String[]{"VP9"}, new String[]{"video/x-vnd.on2.vp9"}),
    /* TODO: Need to investigate further if WMC7,WMC8,WMC9 are supported by video/x-ms-wmv.  If so I will add it */
    WMV7("WMV7", new String[]{"WMV7"}, new String[]{}),
    WMV8("WMV8", new String[]{"WMV8"}, new String[]{}),
    WMV9("WMV9", new String[]{"WMV9"}, new String[]{}),
    UNKNOWN("UNKNOWN", new String[]{"0X0000"}, new String[]{});;

    private String name;
    private String [] sageTVNames;
    private String [] androidMimeTypes;

    private VideoCodec(String name, String [] sageTVNames, String [] androidMimeTypes)
    {
        this.name = name;
        this.sageTVNames = sageTVNames;
        this.androidMimeTypes = androidMimeTypes;
    }

    public String getName()
    {
        return name;
    }

    public String [] sageTVNames()
    {
        return sageTVNames;
    }

    public String [] getAndroidMimeTypes()
    {
        return androidMimeTypes;
    }

    public boolean hasAndroidMimeType(String mime_type)
    {
        for(int i = 0; i < this.androidMimeTypes.length; i++)
        {
            if(this.androidMimeTypes[i].equalsIgnoreCase(mime_type))
            {
                return true;
            }
        }

        return false;
    }

    public static String getAllSageTVNamesString()
    {
        List<String> all = getAllSageTVNames();
        String list = "";


        for(int i = 0; i < all.size(); i++)
        {
            list += all.get(i) + ",";
        }

        if(list.endsWith(","))
        {
            list = list.substring(0, list.length() - 1);
        }

        return list;
    }

    public static List<String> getAllSageTVNames()
    {
        VideoCodec[] all = VideoCodec.values();
        List<String> list = new ArrayList<String>();

        for(int i = 0; i < all.length; i++)
        {
            for(int j = 0; j < all[i].sageTVNames.length; j++)
            {
                list.add(all[i].sageTVNames[j]);
            }
        }

        return list;
    }
}
