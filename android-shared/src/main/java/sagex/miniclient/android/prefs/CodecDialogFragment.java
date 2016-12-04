package sagex.miniclient.android.prefs;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.media.MediaCodecInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.badlogic.gdx.backends.android.AndroidApplication;

import sagex.miniclient.android.AppUtil;
import sagex.miniclient.android.R;

/**
 * Created by seans on 03/12/16.
 */

public class CodecDialogFragment extends DialogFragment {
    ListView list = null;

    public CodecDialogFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.codec_selection, container, false);
        list = (ListView) v.findViewById(R.id.list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CodecAdapter.Holder h = (CodecAdapter.Holder)view.getTag();
                h.checkBox.setChecked(!h.checkBox.isChecked());
                Log.d("XXXXXXXX","Clicked on item in list+ " + h.text1.getText());
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list.setAdapter(new CodecAdapter(getActivity(), R.layout.codec_item, R.id.text1, AppUtil.getDecoders()));
    }

    public static CodecDialogFragment showDialog(FragmentManager fragmentManager) {
        // Create the fragment and show it as a dialog.
        CodecDialogFragment newFragment = new CodecDialogFragment();
        newFragment.show(fragmentManager, "codec_dialog");
        return newFragment;
    }
}
