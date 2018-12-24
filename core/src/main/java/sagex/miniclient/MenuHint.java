package sagex.miniclient;

/**
 * Created by seans on 13/01/16.
 */
public class MenuHint {
    public String menuName;
    public String popupName;
    public boolean hasTextInput;

    public MenuHint() {
    }

    public void update(String data) {
        menuName = null;
        popupName = null;
        hasTextInput = false;

        if (data == null) return;

        String parts[] = data.split(",");
        if (parts != null) {
            String n, v;
            for (String part : parts) {
                try {
                    n = part.substring(0, part.indexOf(':')).trim();
                    v = part.substring(part.indexOf(':') + 1).trim();
                    if ("menuName".equalsIgnoreCase(n)) {
                        menuName = parseString(v);
                    } else if ("popupName".equalsIgnoreCase(n)) {
                        popupName = parseString(v);
                    } else if ("hasTextInput".equalsIgnoreCase(n)) {
                        hasTextInput = Boolean.parseBoolean(v);
                    }
                } catch (Throwable t) {
                    System.out.println("Failed to parse Menu Hint: " + data + "; at part: " + part);
                }
            }
        }
    }

    private String parseString(String val) {
        if (val == null || val.trim().equalsIgnoreCase("NULL") || val.trim().length() == 0) {
            return null;
        }
        return val.trim();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MenuHint{");
        sb.append("hasTextInput=").append(hasTextInput);
        sb.append(", menuName='").append(menuName).append('\'');
        sb.append(", popupName='").append(popupName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * Returns true if the menu name contains the value
     *
     * @param val
     * @return
     */
    public boolean hasMenuLike(String val) {
        return menuName != null && menuName.contains(val);
    }

    /**
     * Returns true if menu is video OSD
     *
     * @return
     */
    public boolean isOSDMenu() {
        return hasMenuLike("OSD");
    }

    /**
     * True, if we are on the OSD and there is no popup visible
     *
     * @return
     */
    public boolean isOSDMenuNoPopup() {
        return popupName == null && isOSDMenu();
    }

    public boolean isPluginMenu() {
        return popupName == null && hasMenuLike("Plugin List");
    }

    public boolean isGuideMenu() {
        return popupName == null && hasMenuLike("TVGuide");
    }
}
