package sagex.miniclient.android;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnAddServerListener} interface
 * to handle interaction events.
 * Use the {@link AddServerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddServerFragment extends DialogFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SERVER_NAME = "servername";
    private static final String ARG_SERVER_ADDR = "serveraddr";

    EditText serverName;
    EditText serverAddr;

    private OnAddServerListener mListener;

    public AddServerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param serverName Parameter 1.
     * @param serverAddr Parameter 2.
     * @return A new instance of fragment AddServerFragment.
     */
    public static AddServerFragment newInstance(String serverName, String serverAddr) {
        AddServerFragment fragment = new AddServerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_NAME, serverName);
        args.putString(ARG_SERVER_ADDR, serverAddr);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_server, container, false);
        serverName = (EditText) v.findViewById(R.id.server_name);
        serverAddr = (EditText) v.findViewById(R.id.server_address);
        v.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed();
            }
        });
        if (getArguments() != null) {
            serverName.setText(getArguments().getString(ARG_SERVER_NAME));
            serverAddr.setText(getArguments().getString(ARG_SERVER_ADDR));
        }
        return v;
    }

    // @OnClick(R.id.button_ok)
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onAddServer(serverName.getText().toString(), serverAddr.getText().toString());
        }
        dismiss();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnAddServerListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnAddServerListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnAddServerListener {
        void onAddServer(String name, String addr);
    }
}
