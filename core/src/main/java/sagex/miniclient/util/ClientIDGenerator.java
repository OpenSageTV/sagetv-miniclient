package sagex.miniclient.util;

import java.util.Random;

/**
 * Created by seans on 05/03/16.
 */
public class ClientIDGenerator {
    public ClientIDGenerator() {

    }

    public String generateId() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append((char) (r.nextInt(26) + 'A'));
        }
        return generateId(sb.toString());
    }

    public String generateId(String in) {
        int size = in.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            char ch = (i >= in.length()) ? 0 : in.charAt(i);
            if (sb.length() > 0) {
                sb.append(":");
            }
            if (ch == 0) {
                sb.append("00");
            } else {
                sb.append(String.format("%02x", (int) ch));
            }
        }
        return sb.toString();
    }

    public String id2string(String id) {
        if (id == null) return null;

        StringBuilder sb = new StringBuilder();
        if (id != null) {
            for (int i = 0; i < id.length(); i += 3) {
                sb.append((char) (Integer.parseInt(id.substring(i, i + 2), 16) & 0xFF));
            }
        }
        return sb.toString();
    }
}
