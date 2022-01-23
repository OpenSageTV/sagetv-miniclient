package sagex.miniclient.media;

import java.util.ArrayList;
import java.util.List;

public enum VideoCodec
{
    MPEG1("MPEG1", "MPEG-1", new String[]{"MPEG1-VIDEO"}, "video/mpeg"),
    MPEG2("MPEG2", "MPEG-2", new String[]{"MPEG2-VIDEO", "MPEG2-VIDEO@HL"}, "video/mpeg2"),
    H263("H.263", "H.263", new String[]{"H.263"}, "video/3gpp"),
    MPEG4("MPEG4", "MPEG-4", new String[]{"MPEG4-VIDEO", "MSMPEG4-VIDEO"}, "video/mp4v-es"),
    H264("H.264", "Advanced Video Coding, MPEG-4 Part 10 (H.264)", new String[]{"H.264"}, "video/avc"),
    VC1("VC1", "VC-1", new String[]{"VC1", "WMV7", "WMV8", "WMV9"}, "video/wvc1"),
    HEVC("HEVC", "High Efficiency Video Coding, H.265 (HEVC)", new String[]{"HEVC"}, "video/hevc"),
    MJPEG("MJPEG", "Motion JPEG (MJPEG)", new String[]{"MJPEG"}, ""),
    VP8("VP8", "VP8", new String[]{"VP8"}, "video/x-vnd.on2.vp8"),
    VP9("VP9", "VP9", new String[]{"VP9"}, "video/x-vnd.on2.vp9"),
    UNKNOWN("UNKNOWN", "", new String[]{"0X0000"}, "");

    private String name;
    private String [] sageTVNames;
    private String androidMimeType;
    private String description;

    private VideoCodec(String name, String description, String [] sageTVNames, String androidMimeType)
    {
        this.name = name;
        this.sageTVNames = sageTVNames;
        this.androidMimeType = androidMimeType;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String [] sageTVNames()
    {
        return sageTVNames;
    }

    public String getAndroidMimeType()
    {
        return androidMimeType;
    }

    public boolean hasAndroidMimeType(String mime_type)
    {

        if(this.androidMimeType.equalsIgnoreCase(mime_type))
        {
            return true;
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
