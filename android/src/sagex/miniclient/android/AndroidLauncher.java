package sagex.miniclient.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import sagex.miniclient.MiniClientMain;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		System.setProperty("user.home", getCacheDir().getAbsolutePath());

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new MiniClientMain(), config);
	}
}
