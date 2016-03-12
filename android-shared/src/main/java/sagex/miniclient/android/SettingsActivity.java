package sagex.miniclient.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

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
