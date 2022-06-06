package sagex.miniclient.media;

import java.util.ArrayList;
import java.util.List;

public enum SubtitleCodec
{
    SUBRIP("SUBRIP", "SubRip Subtitles", new String[]{"SRT"}, "application/x-subrip"),
    PGS("PGSSUB", "PGS Subtitle", new String[]{"PGSSUB"}, "application/pgs"),
    CEA608("CEA-608", "cea-608 Closed Captions", new String[]{""}, "application/cea-608"),
    CEA708("CEA-708", "cea-708 Closed Captions", new String[]{""}, "application/cea-708");

    private String name;
    private String [] sageTVNames;
    private String androidMimeType;
    private String description;

    private SubtitleCodec(String name, String description, String [] sageTVNames, String androidMimeType)
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
        SubtitleCodec[] all = SubtitleCodec.values();
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

    public static SubtitleCodec parse(String mimeType)
    {
        if(mimeType.equalsIgnoreCase(SubtitleCodec.SUBRIP.getAndroidMimeType()))
        {
            return SubtitleCodec.SUBRIP;
        }
        else if(mimeType.equalsIgnoreCase(SubtitleCodec.PGS.getAndroidMimeType()))
        {
            return SubtitleCodec.PGS;
        }
        else if(mimeType.equalsIgnoreCase(SubtitleCodec.CEA608.getAndroidMimeType()))
        {
            return SubtitleCodec.CEA608;
        }
        else if(mimeType.equalsIgnoreCase(SubtitleCodec.CEA708.getAndroidMimeType()))
        {
            return SubtitleCodec.CEA708;
        }

        return null;
    }
}
