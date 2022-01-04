package sagex.miniclient.media;

import java.util.ArrayList;
import java.util.List;

public enum AudioCodec
{
    MPEGL1("MPEG1", new String[]{"MPEG1"}, new String[]{"audio/mpeg-l1"}, new int []{}),
    MPEGL2("MP2", new String[]{"MP2","MPG1L2"}, new String[]{"audio/mpeg-l2"}, new int []{}),
    MP3("MP3", new String[]{"MP3","MPG1L3"}, new String[]{"audio/mpeg"}, new int []{AndroidAudioEncoding.ENCODING_MP3}),
    WMA("WMA", new String[]{"WMA","WMA7","WMA8","WMAPRO","WMA9Lossless"}, new String[]{"audio/x-ms-wma"}, new int []{}),
    VORBIS("VORBIS", new String[]{"VORBIS"}, new String[]{"audio/vorbis"}, new int []{}),
    AAC("AAC", new String[]{"AAC","AAC-HE"}, new String[]{"audio/mp4a-latm"}, new int []{AndroidAudioEncoding.ENCODING_AAC_LC, AndroidAudioEncoding.ENCODING_AAC_HE_V1, AndroidAudioEncoding.ENCODING_AAC_HE_V2, AndroidAudioEncoding.ENCODING_AAC_ELD, AndroidAudioEncoding.ENCODING_AAC_XHE}),
    FLAC("FLAC", new String[]{"FLAC"}, new String[]{"audio/flac"}, new int []{}),
    ALAC("ALAC", new String[]{"ALAC"}, new String[]{"audio/alac"}, new int []{}),
    PCM("PCM", new String[]{"PCM","PCM_S16LE"}, new String[]{"audio/raw"}, new int []{AndroidAudioEncoding.ENCODING_PCM_8BIT, AndroidAudioEncoding.ENCODING_PCM_16BIT, AndroidAudioEncoding.ENCODING_PCM_FLOAT}),
    DTS("DTS", new String[]{"DTS","DCA"}, new String[]{"audio/vnd.dts"}, new int []{AndroidAudioEncoding.ENCODING_DTS}),
    DTSHD("DTS-HD", new String[]{"DTS-HD","DTS-MA"}, new String[]{"audio/vnd.dts.hd"}, new int []{AndroidAudioEncoding.ENCODING_DTS_HD}),
    AC3("AC3", new String[]{"AC3"}, new String[]{"audio/ac3"}, new int []{AndroidAudioEncoding.ENCODING_AC3}),
    AC4("AC4", new String[]{"AC4"}, new String[]{"audio/ac4"}, new int []{AndroidAudioEncoding.ENCODING_AC4}),
    EAC3("EAC3", new String[]{"EAC3","EC-3"}, new String[]{"audio/eac3"}, new int []{AndroidAudioEncoding.ENCODING_E_AC3, AndroidAudioEncoding.ENCODING_E_AC3_JOC}),
    DOLBYTRUEHD("DOLBYTRUEHD", new String[]{"DOLBYTRUEHD"}, new String[]{"audio/true-hd"}, new int []{AndroidAudioEncoding.ENCODING_DOLBY_TRUEHD}),
    OPUS("OPUS", new String[]{"OPUS"}, new String[]{"audio/opus"}, new int []{});

    private String name;
    private String [] sageTVNames;
    private String [] androidMimeTypes;
    private int [] supportedAndroidDeviceEncodings;



    private AudioCodec(String name, String [] sageTVNames, String [] androidMimeTypes, int [] supportedAndroidDeviceEncodings)
    {
        this.name = name;
        this.sageTVNames = sageTVNames;
        this.androidMimeTypes = androidMimeTypes;
        this.supportedAndroidDeviceEncodings = supportedAndroidDeviceEncodings;
    }

    public String getName()
    {
        return name;
    }

    public String [] getSageTVNames()
    {
        return sageTVNames;
    }

    public String [] getAndroidMimeTypes()
    {
        return androidMimeTypes;
    }

    public int [] getAndroidAudioEncodings() { return supportedAndroidDeviceEncodings; }

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
