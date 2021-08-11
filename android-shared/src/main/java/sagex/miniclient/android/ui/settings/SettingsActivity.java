package sagex.miniclient.android.ui.settings;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import sagex.miniclient.android.AppUtil;
import sagex.miniclient.android.R;

public class SettingsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtil.hideSystemUIOnTV(this);
        setContentView(R.layout.activity_preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtil.hideSystemUIOnTV(this);
    }
}
