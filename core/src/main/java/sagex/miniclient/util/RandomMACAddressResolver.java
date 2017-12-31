package sagex.miniclient.util;

import sagex.miniclient.MACAddressResolver;
import sagex.miniclient.prefs.PrefStore;
import sagex.miniclient.util.ClientIDGenerator;

public class RandomMACAddressResolver implements MACAddressResolver {
    private final PrefStore prefStore;

    public RandomMACAddressResolver(PrefStore prefStore) {
        this.prefStore=prefStore;
    }

    @Override
    public String getMACAddress() {
        String id = prefStore.getString(PrefStore.Keys.client_id);
        if (id == null) {
            ClientIDGenerator gen = new ClientIDGenerator();
            id = gen.generateId();
            prefStore.setString(PrefStore.Keys.client_id, id);

        }
        return id;
    }
}
