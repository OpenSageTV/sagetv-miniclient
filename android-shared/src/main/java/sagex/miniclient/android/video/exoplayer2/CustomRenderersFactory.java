package sagex.miniclient.android.video.exoplayer2;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomRenderersFactory extends DefaultRenderersFactory
{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    public CustomRenderersFactory(Context context)
    {
        super(context);
        //super.buildAudioRenderers();s();

    }

    @Override
    public Renderer[] createRenderers(Handler eventHandler,
                                      VideoRendererEventListener videoRendererEventListener,
                                      AudioRendererEventListener audioRendererEventListener,
                                      TextOutput textRendererOutput,
                                      MetadataOutput metadataRendererOutput)
    {



        Renderer [] renderers = super.createRenderers(eventHandler,videoRendererEventListener,audioRendererEventListener,textRendererOutput,metadataRendererOutput);

        for(int i = 0; i < renderers.length; i++)
        {
            //log.debug("JVL - Renderer -> {} {}", i , renderers[i].toString());
        }

        return renderers;
    }



}
