package sagex.miniclient.android.ui.settings;

import static android.media.AudioFormat.ENCODING_INVALID;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.Html;
import android.util.Pair;

import androidx.appcompat.app.AlertDialog;

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.audio.AudioCapabilities;

import java.util.ArrayList;

import sagex.miniclient.android.R;
import sagex.miniclient.media.AudioCodec;

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
        //ArrayList<Pair> audioCodecs = new ArrayList<Pair>();
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

        AudioCodec [] audioCodecs = AudioCodec.values();
        boolean hardwareSupport = false;
        AudioCapabilities capabilities = AudioCapabilities.getCapabilities(getActivity());

        sb.append("<B>").append("Audio Decoders").append("</B><br/>\n");
        sb.append("<table>");

        sb.append("<tr>");
        sb.append("<td><b>Name</b></td>");
        sb.append("<td><b>Support</b></t>");
        sb.append("<td><b>Passthrough</b></td>");
        sb.append("</tr>");

        for(int i = 0; i < audioCodecs.length; i++)
        {
            try
            {
                mediaCodecInfo = MediaCodecUtil.getDecoderInfo(audioCodecs[i].getAndroidMimeTypes()[0], false, false);
                hardwareSupport = false;

                for(int j = 0; j < audioCodecs[i].getAndroidAudioEncodings().length; j++)
                {
                    if(capabilities.supportsEncoding(audioCodecs[i].getAndroidAudioEncodings()[j]))
                    {
                        hardwareSupport = true;
                    }
                }

            }
            catch (Exception ex)
            {
                mediaCodecInfo = null;
            }

            if (mediaCodecInfo != null)
            {
                sb.append("<tr>");
                sb.append("<td>" + audioCodecs[i].getName() + "</td>");
                sb.append("<td>" + mediaCodecInfo.name + "</td>");
                sb.append("<td>" + hardwareSupport + "</td>");
                sb.append("</tr>");
            }
            else
            {
                sb.append("<tr>");
                sb.append("<td>" + audioCodecs[i].getName() + "</td>");
                sb.append("<td>Not Supported</td>");
                sb.append("<td>" + hardwareSupport + "</td>");
                sb.append("</tr>");
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
