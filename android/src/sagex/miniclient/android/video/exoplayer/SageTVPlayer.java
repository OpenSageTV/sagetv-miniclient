package sagex.miniclient.android.video.exoplayer;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.demo.player.DemoPlayer;
import com.google.android.exoplayer.extractor.SageTVExtractorSampleSource;

/**
 * A wrapper around {@link ExoPlayer} that provides a higher level interface. It can be prepared
 * with one of a number of {@link RendererBuilder} classes to suit different use cases (e.g. DASH,
 * SmoothStreaming and so on).
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
}