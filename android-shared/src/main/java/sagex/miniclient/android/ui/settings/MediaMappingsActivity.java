package sagex.miniclient.android.ui.settings;

import android.app.Activity;
import android.os.Bundle;

import sagex.miniclient.android.AppUtil;
import sagex.miniclient.android.R;

public class MediaMappingsActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AppUtil.hideSystemUIOnTV(this);
        setContentView(R.layout.activity_media_mappings);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        AppUtil.hideSystemUIOnTV(this);

    }
}
