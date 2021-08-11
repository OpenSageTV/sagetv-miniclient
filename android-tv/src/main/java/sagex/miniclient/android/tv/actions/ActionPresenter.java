package sagex.miniclient.android.tv.actions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;

import sagex.miniclient.android.tv.R;

/**
 * Created by seans on 28/02/16.
 */
public class ActionPresenter extends Presenter
{
    public class ActionViewHolder extends ViewHolder {
        public TextView text;

        public ActionViewHolder(View view) {
            super(view);
            text = (TextView) view.findViewById(R.id.text);
        }

        public void bind(Action item) {
            text.setText(item.getLabel());
        }
    }

    private final Context context;

    public ActionPresenter(Context ctx) {
        this.context = ctx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        LayoutInflater li = LayoutInflater.from(context);
        View view = li.inflate(R.layout.action_card, parent, false);
        return new ActionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((ActionViewHolder) viewHolder).bind((Action) item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }
}
