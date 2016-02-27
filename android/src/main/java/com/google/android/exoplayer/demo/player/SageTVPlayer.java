package com.google.android.exoplayer.demo.player;

import android.os.Handler;

import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.upstream.BandwidthMeter;

/**
 * The SageTVPlayer provides a simple facade to the DemoPlayer exposing parts that we need
 */
public class SageTVPlayer extends DemoPlayer {
    public SageTVPlayer(RendererBuilder rendererBuilder) {
        super(rendererBuilder);
    }

    public void flush() {
        /**
         * Hack to get Exo to flush it's buffers
         */
        //SageTVExtractorSampleSource.FORCE_DISCONTINUITY_READ = true;
        //seekTo(Long.MAX_VALUE);
        seekTo(Long.MIN_VALUE);
    }

    @Override
    public Handler getMainHandler() {
        return super.getMainHandler();
    }

    @Override
    public void onRenderers(TrackRenderer[] renderers, BandwidthMeter bandwidthMeter) {
        super.onRenderers(renderers, bandwidthMeter);
    }
}