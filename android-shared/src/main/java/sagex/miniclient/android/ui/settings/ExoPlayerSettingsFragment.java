package sagex.miniclient.android.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.util.Pair;

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.ArrayList;

import sagex.miniclient.android.R;

public class ExoPlayerSettingsFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.exoplayer_prefs);

        final Preference exodecoders = findPreference("show_exo_decoders");
        exodecoders.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                showExoPlayerCodecInfo();
                return true;
            }

        });

        final Preference exoversion = findPreference("exoversion");
        exoversion.setSummary(ExoPlayerLibraryInfo.VERSION);
    }

    private void showExoPlayerCodecInfo()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.exo_decoders);
        StringBuilder sb = new StringBuilder();
        ArrayList<Pair> videoCodecs = new ArrayList<Pair>();
        ArrayList<Pair> audioCodecs = new ArrayList<Pair>();
        MediaCodecInfo mediaCodecInfo;

        videoCodecs.add(Pair.create(MimeTypes.VIDEO_H263, "H.263"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_H264, "H.264"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_H265, "H.265"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_DIVX, "DIVX"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_FLV, "FLV"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_MP4, "MP4"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_MP4V, "MP4V"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_MPEG, "MPEG"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_MPEG2, "MPEG2"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_VC1, "VC1"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_VP8, "VP8"));
        videoCodecs.add(Pair.create(MimeTypes.VIDEO_VP9, "VP9"));

        sb.append("<B>").append("Video Decoders").append("</B><br/>\n");

        for(int i = 0; i < videoCodecs.size(); i++)
        {
            try
            {
                mediaCodecInfo = MediaCodecUtil.getDecoderInfo((String)videoCodecs.get(i).first, false, false);


            }
            catch (Exception ex)
            {
                mediaCodecInfo = null;
            }

            if (mediaCodecInfo != null)
            {
                sb.append("").append((String)videoCodecs.get(i).second).append(": ").append(mediaCodecInfo.name).append("<br/>\n");
            }
            else
            {
                sb.append("").append((String)videoCodecs.get(i).second).append(": ").append("<i>Unsupported</i>").append("<br/>\n");
            }
        }

        audioCodecs.add(Pair.create(MimeTypes.AUDIO_AAC, "AAC"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_AC3, "AC3"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_AC4, "AC4"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_DTS, "DTS"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_DTS_EXPRESS, "DTS Express"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_DTS_HD, "DTS HD"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_E_AC3, "EAC3"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_E_AC3_JOC, "EAC3 JOC"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_MP4, "MP4"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_FLAC, "FLAC"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_OPUS, "OPUS"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_MPEG, "MPEG"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_MPEG_L1, "MPEG L1"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_MPEG_L2, "MPEG_L2"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_VORBIS, "VORBIS"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_RAW, "EAC3"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_TRUEHD, "TRUEHD"));
        audioCodecs.add(Pair.create(MimeTypes.AUDIO_RAW, "RAW"));

        sb.append("<B>").append("Audio Decoders").append("</B><br/>\n");

        for(int i = 0; i < audioCodecs.size(); i++)
        {
            try
            {
                mediaCodecInfo = MediaCodecUtil.getDecoderInfo((String)audioCodecs.get(i).first, false, false);
            }
            catch (Exception ex)
            {
                mediaCodecInfo = null;
            }

            if (mediaCodecInfo != null)
            {
                sb.append("").append((String)audioCodecs.get(i).second).append(": ").append(mediaCodecInfo.name).append("<br/>\n");
            }
            else
            {
                sb.append("").append((String)audioCodecs.get(i).second).append(": ").append("<i>Unsupported</i>").append("<br/>\n");
            }
        }

        sb.append("<B>").append("Audio Decoders (FFMPEG)").append("</B><br/>\n");


        if(FfmpegLibrary.isAvailable())
        {

            sb.append("DTS: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_DTS)).append("<br/>");
            sb.append("DTS HD: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_DTS_HD)).append("<br/>");
            sb.append("DTS Express: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_DTS_EXPRESS)).append("<br/>");
            sb.append("AC3: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_AC3)).append("<br/>");
            sb.append("EAC3: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_E_AC3)).append("<br/>");
            sb.append("EAC3 JOC: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_E_AC3_JOC)).append("<br/>");
            sb.append("TRUEHD: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_TRUEHD)).append("<br/>");
            sb.append("FLAC: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_FLAC)).append("<br/>");
            sb.append("OPUS: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_OPUS)).append("<br/>");
            sb.append("VORBIS: ").append(FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_VORBIS)).append("<br/>");
        }
        else
        {
            sb.append("Library is not available");
        }


        builder.setMessage(Html.fromHtml(sb.toString()));
        builder.setCancelable(true);
        builder.show();
    }
}
