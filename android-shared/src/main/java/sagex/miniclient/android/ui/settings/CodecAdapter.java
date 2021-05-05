package sagex.miniclient.android.ui.settings;

import android.content.Context;
import android.media.MediaCodecInfo;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import sagex.miniclient.android.MiniclientApplication;
import sagex.miniclient.android.R;

/**
 * Created by seans on 03/12/16.
 */

public class CodecAdapter extends ArrayAdapter<MediaCodecInfo> {
    public static final String getCodecKey(String key) {
        return "disabled_" + key;
    }

    class Holder {
        int pos=0;
        TextView text1;
        TextView text2;
        CheckBox checkBox;
    }

    public CodecAdapter(Context context, int resource, int textViewResourceId, List<MediaCodecInfo> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        Holder h = (Holder) v.getTag();
        if (h==null) {
            h=new Holder();
            v.setTag(h);
//            v.setFocusable(true);
//            v.setClickable(true);
            h.text1= (TextView) v.findViewById(R.id.text1);
            h.text2= (TextView) v.findViewById(R.id.text2);
            h.checkBox = (CheckBox) v.findViewById(R.id.checkBox);
            h.checkBox.setFocusable(false);
            h.checkBox.setClickable(false);

            final Holder finalH = h;
//            v.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onItemChanged(finalH);
//                }
//            });
//
            h.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onItemChanged(finalH);
                }
            });
//            h.checkBox.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Log.d("XXXX", "check box click");
//                }
//            });

        }

        MediaCodecInfo mi = getItem(position);
        h.text1.setText(mi.getName());
        h.text2.setText(Arrays.toString(mi.getSupportedTypes()));
        h.pos=position;
        h.checkBox.setChecked(!MiniclientApplication.get().getClient().properties().getBoolean(getCodecKey(mi.getName()), false));

        return v;
    }

    private void onItemChanged(Holder finalH) {
        Log.d("XXX","On Change Item");
        MediaCodecInfo mi = getItem(finalH.pos);
        //finalH.checkBox.setChecked(!finalH.checkBox.isChecked());
        MiniclientApplication.get().getClient().properties().setBoolean(getCodecKey(mi.getName()), !finalH.checkBox.isChecked());
    }
}
