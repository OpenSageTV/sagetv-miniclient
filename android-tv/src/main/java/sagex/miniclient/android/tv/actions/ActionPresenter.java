package sagex.miniclient.android.tv.actions;

import android.content.Context;
import android.graphics.Color;
import android.support.v17.leanback.widget.Presenter;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import sagex.miniclient.android.tv.R;

/**
 * Created by seans on 28/02/16.
 */
public class ActionPresenter extends Presenter {
    private final Context context;

    public ActionPresenter(Context ctx) {
        this.context = ctx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        TextView view = new TextView(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams((int) context.getResources().getDimension(R.dimen.action_card_width), (int) context.getResources().getDimension(R.dimen.action_card_height)));
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setBackgroundColor(context.getResources().getColor(R.color.default_background));
        view.setTextColor(Color.WHITE);
        view.setGravity(Gravity.CENTER);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((TextView) viewHolder.view).setText(((Action) item).getLabel());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }
}
