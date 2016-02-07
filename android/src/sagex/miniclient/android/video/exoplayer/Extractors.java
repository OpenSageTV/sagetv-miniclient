package sagex.miniclient.android.video.exoplayer;

import android.net.Uri;

import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.extractor.flv.FlvExtractor;
import com.google.android.exoplayer.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer.extractor.mp4.FragmentedMp4Extractor;
import com.google.android.exoplayer.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer.extractor.ts.AdtsExtractor;
import com.google.android.exoplayer.extractor.ts.PsExtractor;
import com.google.android.exoplayer.extractor.ts.TsExtractor;
import com.google.android.exoplayer.extractor.webm.WebmExtractor;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by seans on 30/01/16.
 */
public class Extractors {
    static Logger log = LoggerFactory.getLogger(Extractors.class);

    public static ExtractorSampleSource newExtractorSampleSource(Uri uri, DataSource dataSource, Allocator allocator, int requestedBufferSize) {
        // TODO: we should limit the extractors for PUSH, since it's just overhead
        if (dataSource instanceof ExoPushDataSource) {
            log.info("Using PUSH extractors");
            return new ExtractorSampleSource(uri, dataSource, allocator, requestedBufferSize);
        } else {
            log.info("Using PULL extractors");
            return new ExtractorSampleSource(uri, dataSource, allocator, requestedBufferSize);
        }
    }

    static Extractor[] getPushExtractors() {
        return new Extractor[]{
                new TsExtractor(),
                new PsExtractor(),
                new AdtsExtractor()
        };
    }

    static Extractor[] getPullExtractors() {
        return new Extractor[]{
                new Mp4Extractor(),
                new WebmExtractor(),
                new FragmentedMp4Extractor(),
                new FlvExtractor(),
                new TsExtractor(),
                new PsExtractor(),
                new AdtsExtractor(),
                new Mp3Extractor()
        };
    }
}
