package sagex.miniclient.android.tv.actions;

/**
 * Created by seans on 28/02/16.
 */
public class Action {
    public interface OnClick {
        void onClick(Action action);
    }

    private final int actionId;
    private final String label;
    private final OnClick onClick;

    public Action(int actionId, String label) {
        this.actionId = actionId;
        this.label = label;
        this.onClick = null;
    }

    public Action(int actionId, String label, OnClick onClick) {
        this.actionId = actionId;
        this.label = label;
        this.onClick = onClick;
    }

    public String getLabel() {
        return label;
    }

    public int getActionId() {
        return actionId;
    }

    public void perform() {
        if (onClick != null) onClick.onClick(this);
    }
}
