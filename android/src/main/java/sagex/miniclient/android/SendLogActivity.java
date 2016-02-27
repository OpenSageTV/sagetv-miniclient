package sagex.miniclient.android;

import org.l6n.sendlog.library.SendLogActivityBase;

/**
 * Created by seans on 08/11/15.
 */
public class SendLogActivity extends SendLogActivityBase {

    public SendLogActivity() {
    }

    @Override
    protected String getDestinationAddress() {
        return "metadatatools@gmail.com";
    }
}
