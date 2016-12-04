package sagex.miniclient.android.video.ijkplayer;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.Locale;

import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.prefs.CodecAdapter;
import sagex.miniclient.prefs.PrefStore;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaCodecInfo;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by seans on 03/12/16.
 */

public class CodecSelector implements IjkMediaPlayer.OnMediaCodecSelectListener {
    private final static String TAG = "IJKCodecSelector";

    public static final CodecSelector sInstance = new CodecSelector();

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public String onMediaCodecSelect(IMediaPlayer mp, String mimeType, int profile, int level) {
        Log.d(TAG, String.format(Locale.US, "onSelectCodec: mime=%s, profile=%d, level=%d", mimeType, profile, level));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            return null;

        if (TextUtils.isEmpty(mimeType))
            return null;

        if (MiniclientApplication.get().getClient().properties().getBoolean(PrefStore.Keys.disable_hardware_decoders, false)) {
            Log.d(TAG, "Hardware Decoders are disabled.  Using software only decoders.");
            return null;
        }

        // Log.i(TAG, String.format(Locale.US, "onSelectCodec: mime=%s, profile=%d, level=%d", mimeType, profile, level));
        ArrayList<IjkMediaCodecInfo> candidateCodecList = new ArrayList<IjkMediaCodecInfo>();
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            Log.d(TAG, String.format(Locale.US, "  found codec: %s", codecInfo.getName()));
            if (codecInfo.isEncoder())
                continue;

            if (MiniclientApplication.get().getClient().properties().getBoolean(CodecAdapter.getCodecKey(codecInfo.getName()), false)) {
                Log.d(TAG, "Skipping BLOCKED Codec " + codecInfo.getName());
                continue;
            }

            if (!isCodecUsableDecoder(codecInfo, codecInfo.getName(), false)) {
                Log.d(TAG, "Skipping BROKEN/UNUSABLE Codec " + codecInfo.getName());
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            if (types == null)
                continue;

            for(String type: types) {
                if (TextUtils.isEmpty(type))
                    continue;

                Log.d(TAG, String.format(Locale.US, "    mime: %s", type));
                if (!type.equalsIgnoreCase(mimeType))
                    continue;

                IjkMediaCodecInfo candidate = IjkMediaCodecInfo.setupCandidate(codecInfo, mimeType);
                if (candidate == null)
                    continue;

                candidateCodecList.add(candidate);
                Log.i(TAG, String.format(Locale.US, "candidate codec: %s rank=%d", codecInfo.getName(), candidate.mRank));
                candidate.dumpProfileLevels(mimeType);
            }
        }

        if (candidateCodecList.isEmpty()) {
            return null;
        }

        IjkMediaCodecInfo bestCodec = candidateCodecList.get(0);

        for (IjkMediaCodecInfo codec : candidateCodecList) {
            if (codec.mRank > bestCodec.mRank) {
                bestCodec = codec;
            }
        }

        // Ijk will rank codecs anything less than 600 means we use should use software decoder
        if (bestCodec.mRank < IjkMediaCodecInfo.RANK_LAST_CHANCE) {
            if (MiniclientApplication.get().getClient().properties().getBoolean(PrefStore.Keys.prefer_android_software_decoders, false)) {
                Log.w(TAG, String.format(Locale.US, "codec is likely a software codec, but we'll try it: %s; Rank: %d", bestCodec.mCodecInfo.getName(), bestCodec.mRank));
            } else {
                Log.w(TAG, String.format(Locale.US, "unacceptable codec: %s; Rank: %d", bestCodec.mCodecInfo.getName(), bestCodec.mRank));
                return null;
            }
        }

        Log.i(TAG, String.format(Locale.US, "selected codec: %s rank=%d", bestCodec.mCodecInfo.getName(), bestCodec.mRank));
        return bestCodec.mCodecInfo.getName();
    }

    /**
     * Copied from ExoPlayer, since, it does a good job of blacklisting some decoders
     * https://github.com/google/ExoPlayer/blob/7d991cef305e95cae5cd2a9feadf4af8858b284b/library/src/main/java/com/google/android/exoplayer2/mediacodec/MediaCodecUtil.java
     * @param info codec info
     * @param name codec name
     * @param secureDecodersExplicit Whether the decoder is required to support secure decryption. Always pass false
     *     unless secure decryption really is required.
     * @return
     */
    private static boolean isCodecUsableDecoder(android.media.MediaCodecInfo info, String name,
                                                boolean secureDecodersExplicit) {
        if (info.isEncoder() || (!secureDecodersExplicit && name.endsWith(".secure"))) {
            return false;
        }

        // Work around broken audio decoders.
        if (Util.SDK_INT < 21
                && ("CIPAACDecoder".equals(name)
                || "CIPMP3Decoder".equals(name)
                || "CIPVorbisDecoder".equals(name)
                || "AACDecoder".equals(name)
                || "MP3Decoder".equals(name))) {
            return false;
        }
        // Work around https://github.com/google/ExoPlayer/issues/398
        if (Util.SDK_INT < 18 && "OMX.SEC.MP3.Decoder".equals(name)) {
            return false;
        }
        // Work around https://github.com/google/ExoPlayer/issues/1528
        if (Util.SDK_INT < 18 && "OMX.MTK.AUDIO.DECODER.AAC".equals(name)
                && "a70".equals(Util.DEVICE)) {
            return false;
        }

        // Work around an issue where querying/creating a particular MP3 decoder on some devices on
        // platform API version 16 fails.
        if (Util.SDK_INT == 16
                && "OMX.qcom.audio.decoder.mp3".equals(name)
                && ("dlxu".equals(Util.DEVICE) // HTC Butterfly
                || "protou".equals(Util.DEVICE) // HTC Desire X
                || "ville".equals(Util.DEVICE) // HTC One S
                || "villeplus".equals(Util.DEVICE)
                || "villec2".equals(Util.DEVICE)
                || Util.DEVICE.startsWith("gee") // LGE Optimus G
                || "C6602".equals(Util.DEVICE) // Sony Xperia Z
                || "C6603".equals(Util.DEVICE)
                || "C6606".equals(Util.DEVICE)
                || "C6616".equals(Util.DEVICE)
                || "L36h".equals(Util.DEVICE)
                || "SO-02E".equals(Util.DEVICE))) {
            return false;
        }

        // Work around an issue where large timestamps are not propagated correctly.
        if (Util.SDK_INT == 16
                && "OMX.qcom.audio.decoder.aac".equals(name)
                && ("C1504".equals(Util.DEVICE) // Sony Xperia E
                || "C1505".equals(Util.DEVICE)
                || "C1604".equals(Util.DEVICE) // Sony Xperia E dual
                || "C1605".equals(Util.DEVICE))) {
            return false;
        }

        // Work around https://github.com/google/ExoPlayer/issues/548
        // VP8 decoder on Samsung Galaxy S3/S4/S4 Mini/Tab 3 does not render video.
        if (Util.SDK_INT <= 19
                && (Util.DEVICE.startsWith("d2") || Util.DEVICE.startsWith("serrano")
                || Util.DEVICE.startsWith("jflte") || Util.DEVICE.startsWith("santos"))
                && "samsung".equals(Util.MANUFACTURER) && "OMX.SEC.vp8.dec".equals(name)) {
            return false;
        }
        // VP8 decoder on Samsung Galaxy S4 cannot be queried.
        if (Util.SDK_INT <= 19 && Util.DEVICE.startsWith("jflte")
                && "OMX.qcom.video.decoder.vp8".equals(name)) {
            return false;
        }

        return true;
    }
}
