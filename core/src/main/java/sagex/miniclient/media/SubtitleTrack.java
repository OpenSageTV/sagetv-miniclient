package sagex.miniclient.media;

public class SubtitleTrack
{

    private SubtitleCodec codec;
    private int index;
    private String language;
    private String label;
    private boolean supported;

    public SubtitleTrack(int index, SubtitleCodec codec, String language, String label, boolean supported)
    {
        this.index = index;
        this.codec = codec;
        this.language = language;
        this.label = label;
        this.supported = supported;
    }

    @Override
    public String toString()
    {
        String output = "";

        if (language.equalsIgnoreCase(""))
        {
            output += "Unknown";
        }
        else
        {
            output += getLanguage();
        }

        output += " (" + getSubtitleCodec() + ")";

        if(!getLabel().equalsIgnoreCase(""))
        {
            output += " " + label;
        }

        if(!isSupported())
        {
            output += " NOT SUPPORTED";
        }

        return output;
    }

    public int getIndex()
    {
        return index;
    }

    public String  getLanguage()
    {
        if(language == null)
        {
            return "";
        }
        else
        {
            return language;
        }
    }

    public SubtitleCodec getSubtitleCodec()
    {
        return this.codec;
    }

    public String getLabel()
    {
        if(label == null)
        {
            return "";
        }
        else
        {
            return label;
        }


    }

    public boolean isSupported()
    {
        return supported;
    }
}
