package sagex.miniclient.media;

import java.util.ArrayList;
import java.util.List;

public enum Container
{
    MATROSKA("MATROSKA", "Matroska", new String[]{"MATROSKA"}),
    MPEG2TS("MPEG2-TS", "MPEG2 transport stream (MPEG2-TS)", new String[]{"MPEG2-TS"}),
    MPEG2PS("MPEG2-PS", "MPEG2 program stream (MPEG2-PS)", new String[]{"MPEG2-PS", "MPEG"}),
    MPEG1PS("MPEG1-PS", "MPEG1 program stream (MPEG1-PS)", new String[]{"MPEG1-PS"}),
    MP4("MP4", "MPEG-4 Part 14 (MP4) / Quicktime", new String[]{"MP4", "QUICKTIME"}),
    AVI("AVI", "Audio Video Interleave (AVI)", new String[]{"AVI"}),
    FLASHVIDEO("FLASHVIDEO", "Flash Video (FLV)", new String[]{"FLASHVIDEO"}),
    //QUICKTIME("QUICKTIME", "Quicktime", new String[]{"QUICKTIME"}),
    OGG("OGG", "Ogg", new String[]{"OGG"}),
    MP3("MP3", "Mp3", new String[]{"MP3"}),
    AAC("AAC", "Advanced Audio Codec (AAC)", new String[]{"AAC"}),
    ASF("ASF", "Advanced Systems Format (ASF)", new String[]{"ASF"}),
    FLAC("FLAC", "Free Lossless Audio Codec (FLAC)", new String[]{"FLAC"}),
    WAV("WAV", "Waveform Audio File Format (WAV)", new String[]{"WAV"}),
    AC3("AC3", "AC3", new String[]{"AC3"});


    String name;
    String description;
    String [] sageTVNames;

    private Container(String name, String description, String [] sageTVNames)
    {
        this.name = name;
        this.description = description;
        this.sageTVNames = sageTVNames;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String[] getSageTVNames()
    {
        return sageTVNames;
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
        Container[] all = Container.values();
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
