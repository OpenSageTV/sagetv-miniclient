package sagex.miniclient.android.ui.settings;

import static android.media.AudioFormat.ENCODING_INVALID;

import android.content.ContentResolver;
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
import sagex.miniclient.media.VideoCodec;

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
        MediaCodecInfo mediaCodecInfo;

        sb.append("<B>").append("Video Decoders").append("</B><br/>\n");

        VideoCodec [] videoCodecs = VideoCodec.values();

        for(int i = 0; i < videoCodecs.length; i++)
        {
            try
            {
                mediaCodecInfo = MediaCodecUtil.getDecoderInfo(videoCodecs[i].getAndroidMimeType(), false, false);
            }
            catch (Exception ex)
            {
                mediaCodecInfo = null;
            }

            if (mediaCodecInfo != null)
            {
                sb.append(videoCodecs[i].getName()).append(": ").append(mediaCodecInfo.name).append("<br/>\n");
            }
            else
            {
                sb.append(videoCodecs[i].getName()).append(": ").append("<i>Unsupported</i>").append("<br/>\n");
            }
        }

        AudioCodec [] audioCodecs = AudioCodec.values();
        boolean hardwareSupport = false;
        AudioCapabilities capabilities = AudioCapabilities.getCapabilities(getActivity());

        sb.append("<B>").append("Audio Decoders").append("</B><br/>\n");

        for(int i = 0; i < audioCodecs.length; i++)
        {
            try
            {
                mediaCodecInfo = MediaCodecUtil.getDecoderInfo(audioCodecs[i].getAndroidMimeType(), false, false);
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

            sb.append("<B>" + audioCodecs[i].getName() + ":</B> ");

            if (mediaCodecInfo != null)
            {
                sb.append(mediaCodecInfo.name );
            }
            else
            {
                if(FfmpegLibrary.isAvailable() && FfmpegLibrary.supportsFormat(audioCodecs[i].getAndroidMimeType()))
                {
                    sb.append("FFmpeg");
                }
                else
                {
                    sb.append("<i>Unsupported</i>");
                }

            }
            if(hardwareSupport)
            {
                sb.append("&nbsp;&nbsp;&nbsp;&nbsp;Passthrough: true");
            }
            sb.append("<br>\n");
        }

        if(!FfmpegLibrary.isAvailable())
        {
            sb.append("<B>").append("Audio Decoders (FFMPEG)").append("</B><br/>\n");
            sb.append("Library is not available");
        }

        builder.setMessage(Html.fromHtml(sb.toString()));
        builder.setCancelable(true);
        builder.show();
    }
}
