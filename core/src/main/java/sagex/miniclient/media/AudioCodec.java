package sagex.miniclient.media;

import java.util.ArrayList;
import java.util.List;

public enum AudioCodec
{
    MPEGL1("MPEG1", "MPEG1 Layer I", new String[]{"MPEG1"}, "audio/mpeg-l1", new int []{}),
    MPEGL2("MP2", "MPEG1 Layer II (MP2)", new String[]{"MP2","MPG1L2"}, "audio/mpeg-l2", new int []{}),
    MP3("MP3", "MP3", new String[]{"MP3","MPG1L3"}, "audio/mpeg", new int []{AndroidAudioEncoding.ENCODING_MP3}),
    WMA("WMA", "Windows Media Audio (WMA)", new String[]{"WMA","WMA7","WMA8","WMAPRO","WMA9Lossless"}, "audio/x-ms-wma", new int []{}),
    VORBIS("VORBIS", "Vorbis", new String[]{"VORBIS"}, "audio/vorbis", new int []{}),
    AAC("AAC", "Advanced Audio Codeing (AAC)", new String[]{"AAC","AAC-HE"}, "audio/mp4a-latm", new int []{AndroidAudioEncoding.ENCODING_AAC_LC, AndroidAudioEncoding.ENCODING_AAC_HE_V1, AndroidAudioEncoding.ENCODING_AAC_HE_V2, AndroidAudioEncoding.ENCODING_AAC_ELD, AndroidAudioEncoding.ENCODING_AAC_XHE}),
    FLAC("FLAC", "Free Lossless Audio Codec (FLAC)", new String[]{"FLAC"}, "audio/flac", new int []{}),
    ALAC("ALAC", "Apple Lossless Audio Codec (ALAC)", new String[]{"ALAC"}, "audio/alac", new int []{}),
    PCM("PCM", "Pulse-code mudulation (PCM)", new String[]{"PCM","PCM_S16LE"}, "audio/raw", new int []{AndroidAudioEncoding.ENCODING_PCM_8BIT, AndroidAudioEncoding.ENCODING_PCM_16BIT, AndroidAudioEncoding.ENCODING_PCM_FLOAT}),
    DTS("DTS", "DTS", new String[]{"DTS","DCA"}, "audio/vnd.dts", new int []{AndroidAudioEncoding.ENCODING_DTS}),
    DTSHD("DTS-HD", "DTS High Resolution Audio (DTS-HD)", new String[]{"DTS-HD","DTS-MA"}, "audio/vnd.dts.hd", new int []{AndroidAudioEncoding.ENCODING_DTS_HD}),
    AC3("AC3", "Dolby AC-3 (AC3)", new String[]{"AC3"}, "audio/ac3", new int []{AndroidAudioEncoding.ENCODING_AC3}),
    AC4("AC4", "Dolby AC-4 (AC4)", new String[]{"AC4"}, "audio/ac4", new int []{AndroidAudioEncoding.ENCODING_AC4}),
    EAC3("EAC3", "Doubly Digital Plus (EAC3)", new String[]{"EAC3","EC-3"}, "audio/eac3", new int []{AndroidAudioEncoding.ENCODING_E_AC3, AndroidAudioEncoding.ENCODING_E_AC3_JOC}),
    DOLBYTRUEHD("DOLBYTRUEHD", "Dolby TrueHD", new String[]{"DOLBYTRUEHD"}, "audio/true-hd", new int []{AndroidAudioEncoding.ENCODING_DOLBY_TRUEHD}),
    OPUS("OPUS", "Opus", new String[]{"OPUS"}, "audio/opus", new int []{});

    private String name;
    private String description;
    private String [] sageTVNames;
    private String androidMimeType;
    private int [] supportedAndroidDeviceEncodings;



    private AudioCodec(String name, String description,  String [] sageTVNames, String androidMimeType, int [] supportedAndroidDeviceEncodings)
    {
        this.name = name;
        this.description = description;
        this.sageTVNames = sageTVNames;
        this.androidMimeType = androidMimeType;
        this.supportedAndroidDeviceEncodings = supportedAndroidDeviceEncodings;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription() { return description; }

    public String [] getSageTVNames()
    {
        return sageTVNames;
    }

    public String getAndroidMimeType()
    {
        return androidMimeType;
    }

    public int [] getAndroidAudioEncodings() { return supportedAndroidDeviceEncodings; }

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
        AudioCodec[] all = AudioCodec.values();
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
