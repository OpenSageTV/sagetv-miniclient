package sagex.miniclient.android.video.exoplayer;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.util.PlayerControl;

/**
 * Created by seans on 17/12/15.
 */
public class PushPlayerControl extends PlayerControl {
    public PushPlayerControl(ExoPlayer exoPlayer) {
        super(exoPlayer);
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }
}
