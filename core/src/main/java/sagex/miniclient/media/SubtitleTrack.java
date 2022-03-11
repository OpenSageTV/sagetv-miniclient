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
        return getIndex() + " - " + getSubtitleCodec().getName() + ", " + getLanguage();
    }

    public int getIndex()
    {
        return index;
    }

    public String  getLanguage()
    {
        return language;
    }

    public SubtitleCodec getSubtitleCodec()
    {
        return this.codec;
    }

    public String getLabel()
    {
        return label;
    }

    public boolean isSupported()
    {
        return supported;
    }
}
