package sagex.miniclient.android;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sagex.miniclient.MiniClient;
import sagex.miniclient.MiniPlayerPlugin;
import sagex.miniclient.SageCommand;
import sagex.miniclient.android.events.BackPressedEvent;
import sagex.miniclient.android.events.ChangePlayerOneTime;
import sagex.miniclient.android.events.CloseAppEvent;
import sagex.miniclient.android.events.HideNavigationEvent;
import sagex.miniclient.android.events.HideSystemUIEvent;
import sagex.miniclient.android.events.ToggleAspectRatioEvent;
import sagex.miniclient.android.preferences.MediaMappingPreferences;
import sagex.miniclient.events.ShowKeyboardEvent;
import sagex.miniclient.events.VideoInfoShow;
import sagex.miniclient.media.SubtitleTrack;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.uibridge.EventRouter;

/**
 * Created by seans on 05/12/15.
 */
public class NavigationFragment extends DialogFragment
{
    static final Logger log = LoggerFactory.getLogger(NavigationFragment.class);
    private final MiniClient client;

    private View navView;

    View navOptions = null;

    View navPause = null;

    ImageView navSmartRemote;
    MediaMappingPreferences prefs;

    public NavigationFragment()
    {
        this.client = MiniclientApplication.get().getClient();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        AppUtil.hideSystemUI(getActivity());
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        AppUtil.hideSystemUI(this.getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Dialog_DoNotDim);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        navView = inflater.inflate(R.layout.navigation, container, false);

        prefs = new MediaMappingPreferences(this.client.properties());

        navOptions = navView.findViewById(R.id.nav_options);
        navPause = navView.findViewById(R.id.nav_media_pause);
        navSmartRemote = (ImageView) navView.findViewById(R.id.nav_remote_mode);

        View.OnClickListener buttonClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClick(v);
            }
        };

        for (int id : new int[]{R.id.nav_up, R.id.nav_down, R.id.nav_left, R.id.nav_right, R.id.nav_select, R.id.nav_pgdn, R.id.nav_pgup,
                R.id.nav_options, R.id.nav_home, R.id.nav_media_pause, R.id.nav_media_play, R.id.nav_media_skip_back, R.id.nav_media_skip_back_2,
                R.id.nav_media_skip_forward, R.id.nav_media_skip_forward_2,
                R.id.nav_media_stop, R.id.nav_back, R.id.nav_info, R.id.nav_video_info}) {
            try
            {
                navView.findViewById(id).setOnClickListener(buttonClickListener);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }

        navView.findViewById(R.id.nav_switch_player).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onSwitchPlayer();
            }
        });

        navView.findViewById(R.id.nav_toggle_ar).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onToggleAspectRatio();
            }
        });

        navView.findViewById(R.id.nav_video_info).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onVideoInfo();
            }

        });

        navView.findViewById(R.id.nav_closed_captions).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SubtitleTrack [] tracks = client.getPlayer().getSubtitleTracks();
                String [] items = new String[tracks.length + 1];
                int selectedIndex;

                items[0] = "Off";

                for(int i = 0; i < tracks.length; i++)
                {
                    items[i + 1] = tracks[i].toString();
                }

                if(client.getPlayer().getSelectedSubtitleTrack() == MiniPlayerPlugin.DISABLE_TRACK)
                {
                    selectedIndex = 0;
                }
                else
                {
                    selectedIndex = client.getPlayer().getSelectedSubtitleTrack() + 1;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(NavigationFragment.this.getActivity());
                builder.setTitle("Select Subtitle/Closed Caption");
                builder.setSingleChoiceItems(items, selectedIndex, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                        if(i > 0)
                        {
                            if(tracks[i - 1].isSupported())
                            {
                                client.getPlayer().setSubtitleTrack(tracks[i - 1].getIndex());
                            }
                        }
                        else if(i == 0)
                        {
                            client.getPlayer().setSubtitleTrack(MiniPlayerPlugin.DISABLE_TRACK);

                        }
                        dialogInterface.cancel();
                    }

                });
                builder.show();
            }
        });

        navView.findViewById(R.id.nav_remote_mode).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onToggleSmartRemote();
            }
        });

        navView.findViewById(R.id.nav_help).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onHelp();
            }
        });

        for (int id : new int[]{R.id.nav_keyboard, R.id.nav_close, R.id.nav_hide})
        {
            navView.findViewById(id).setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    buttonClickInternal(v);
                }
            });
        }

        if (client==null||client.getCurrentConnection()==null) return navView;

        if (client.getCurrentConnection().getMenuHint().isOSDMenuNoPopup() && (client.isVideoPlaying() || client.isVideoPaused()))
        {
            navPause.requestFocus();
        }
        else
        {
            navOptions.requestFocus();
        }

        updateSmartRemoteToggle();

        AppUtil.hideSystemUI(getActivity());

        return navView;
    }

    private void onVideoInfo()
    {
        log.debug("Showing Video Info View");
        client.eventbus().post(new VideoInfoShow());
    }

    public void buttonClick(View v)
    {
        try
        {
            log.debug("Clicked: {}", v.getTag());
            String key = v.getTag().toString().toLowerCase();
            boolean hide = key.startsWith("_");

            if (hide)
            {
                key = key.substring(1);
            }
            if (hide) dismiss();

            // unique case... if the video is paused, and this pause/play button is clicked,
            // the hide the navigation.
            if ("play_pause".equalsIgnoreCase(key) && client.isVideoPaused())
            {
                dismiss();
            }

            int sageCommand = SageCommand.parseByKey(key).getEventCode();

            if (sageCommand == -1)
            {
                log.warn("Invalid SageTV Command '{}'", key);
            }
            else
            {
                EventRouter.postCommand(client, sageCommand);
            }
        }
        catch (Throwable t)
        {
            log.error("Button Not Implemented for {} with ID {}", v.getTag(), v.getId(), t);
        }
    }

    // @OnClick(R.id.nav_switch_player)
    public void onSwitchPlayer()
    {
        dismiss();

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_switch_player)
                .setMessage(getResources().getString(R.string.msg_switch_player, getPlayerName(), getOtherPlayerName()))
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(isExoPlayer())
                        {
                            client.properties().setString(PrefStore.Keys.default_player, "ijkplayer");
                        }
                        else
                        {
                            client.properties().setString(PrefStore.Keys.default_player, "exoplayer");
                        }

                        AppUtil.message(MiniclientApplication.get().getString(R.string.msg_player_changed, getPlayerName()));
                        dismiss();
                    }
                })
                .setNeutralButton(R.string.yes_once, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        client.eventbus().post(new ChangePlayerOneTime());
                        AppUtil.message(MiniclientApplication.get().getString(R.string.msg_player_changed_one_time, getOtherPlayerName(), getPlayerName()));
                        dismiss();
                    }
                }).show();
    }

    // @OnClick(R.id.nav_toggle_ar)
    public void onToggleAspectRatio() {
        client.eventbus().post(ToggleAspectRatioEvent.INSTANCE);
    }

    // @OnClick(R.id.nav_remote_mode)
    public void onToggleSmartRemote()
    {
        prefs.setSmartRemoteEnabled(!prefs.isSmartRemoteEnabled());
        updateSmartRemoteToggle();
    }

    private void updateSmartRemoteToggle()
    {
        if (prefs.isSmartRemoteEnabled())
        {
            navSmartRemote.setImageResource(R.drawable.ic_open_with_white_24dp);
        }
        else
        {
            navSmartRemote.setImageResource(R.drawable.ic_open_with_red_24dp);
        }
    }


    // @OnClick(R.id.nav_help)
    public void onHelp()
    {
        dismiss();
        HelpDialogFragment.showDialog(getActivity());
    }

    private boolean isExoPlayer()
    {
        return client.properties().getString(PrefStore.Keys.default_player, "exoplayer").equalsIgnoreCase("exoplayer");
    }

    private String getPlayerName() {
        return (isExoPlayer() ? "ExoPlayer" : "IJKPlayer");
    }

    private String getOtherPlayerName() {
        return (isExoPlayer() ? "IJKPlayer" : "ExoPlayer");
    }

    @Override
    public void dismiss()
    {
        super.dismiss();
        client.eventbus().post(HideSystemUIEvent.INSTANCE);
    }

    // @OnClick({R.id.nav_keyboard, R.id.nav_close, R.id.nav_hide})
    public void buttonClickInternal(View v)
    {
        String tag = v.getTag().toString().toLowerCase();

        if ("_keyboard".equalsIgnoreCase(tag))
        {
            dismiss();
            client.eventbus().post(ShowKeyboardEvent.INSTANCE);
        }
        else if ("_close".equalsIgnoreCase(tag))
        {
            client.eventbus().post(CloseAppEvent.INSTANCE);
        }
        else if ("_hide".equalsIgnoreCase(tag))
        {
            client.eventbus().post(HideNavigationEvent.INSTANCE);
        }
        else
        {
            log.warn("Nothing Handled Internal Event: {}", tag);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = new WindowManager.LayoutParams();
        wmlp.copyFrom(dialog.getWindow().getAttributes());

        wmlp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        wmlp.x = 0;   //x position
        wmlp.y = 0;   //y position
        wmlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(wmlp);

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    client.eventbus().post(BackPressedEvent.INSTANCE);
                    return true;
                }
                return false;
            }
        });

        return dialog;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();

        if (dialog != null)
        {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public static void showDialog(Activity activity)
    {
        log.debug("Showing Navigation");
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.

        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        Fragment prev = activity.getFragmentManager().findFragmentByTag("nav");

        if (prev != null)
        {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = new NavigationFragment();
        newFragment.show(ft, "nav");

    }
}
