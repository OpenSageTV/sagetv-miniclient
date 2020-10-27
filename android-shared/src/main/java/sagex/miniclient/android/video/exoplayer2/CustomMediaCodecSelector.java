package sagex.miniclient.android.video.exoplayer2;

import android.support.annotation.Nullable;

import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.util.VerboseLogging;

import java.util.List;

public class CustomMediaCodecSelector implements MediaCodecSelector
{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    

    
    @Override
    public List<MediaCodecInfo> getDecoderInfos(String mimeType, boolean requiresSecureDecoder, boolean requiresTunnelingDecoder) throws MediaCodecUtil.DecoderQueryException
    {
        //List<MediaCodecInfo> codecs = MediaCodecSelector.DEFAULT.getDecoderInfos(mimeType, requiresSecureDecoder);
        List<MediaCodecInfo> codecs =  MediaCodecSelector.DEFAULT.getDecoderInfos(mimeType, requiresSecureDecoder, requiresTunnelingDecoder);
            
                log.debug("JVL - getDecoderInfos called MimeType={} ", mimeType);
        for(int i = 0; i < codecs.size(); i++)
        {
            log.debug("JVL - DecoderIndex {}, Decoder Name = {}", i, codecs.get(i).name);
        }
    
        return codecs;
    }
       /*
    @Nullable
    @Override

    public MediaCodecInfo getPassthroughDecoderInfo() throws MediaCodecUtil.DecoderQueryException
    {
        MediaCodecInfo codec = MediaCodecSelector.DEFAULT.getPassthroughDecoderInfo();
    
        log.debug("JVL - getPassthroughDecoderInfo called Decoder Name={} ", codec.name);
        
        return codec;
    }

     */
}
