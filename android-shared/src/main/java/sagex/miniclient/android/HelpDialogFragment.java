package sagex.miniclient.android;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import sagex.miniclient.MiniClientConnection;
import sagex.miniclient.util.IOUtil;

/**
 * Created by seans on 31/01/16.
 */
public class HelpDialogFragment extends DialogFragment {
    TextView helpText;

    public HelpDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_help, container, false);
        helpText = (TextView) v.findViewById(R.id.help_text);
        try {
            updateText();
        } catch (IOException e) {
            e.printStackTrace();
            helpText.setText("Can't find Help File");
        }
        return v;
    }

    private void updateText() throws IOException {
        String input = "TOUCH_NAVIGATION.md";
        if (getResources().getBoolean(R.bool.istv)) {
            input = "REMOTE_NAVIGATION.md";
        }
        InputStream is = MiniClientConnection.class.getClassLoader().getResourceAsStream(input);
        CharSequence text = prettyText(IOUtil.toString(is));
        is.close();

        helpText.setText(text);
    }

    private CharSequence prettyText(String text) {
        text = text.replaceAll("#\\s*(.*)\n", "<h2><font color=\"#009688\">$1</font></h2>");
        text = text.replaceAll("\\*", "\u2022 ");
        text = text.replaceAll("```([^`]+)```", "<font color=\"#B2DFDB\">$1</font>");
        text = text.replaceAll("\n", "<br>");
        return Html.fromHtml(text);
    }

    public static void showDialog(Activity context) {
        FragmentTransaction ft = context.getFragmentManager().beginTransaction();
        Fragment prev = context.getFragmentManager().findFragmentByTag("help");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = new HelpDialogFragment();
        newFragment.show(ft, "help");
    }
}
