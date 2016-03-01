package tonyjmnz.splitmate;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OverviewFragment extends Fragment {

    private static final int MENU_POSITION = 1;
    private OverviewFragmentInteractions mainActivity;

    public OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_overview, container, false);

        // Inflate the layout for this fragment
        return layout;
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mainActivity = (ExpensesFragmentInteractions) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        setNavName();
    }

    private void setNavName() {
        ((MainActivity) getActivity()).onSectionAttached(MENU_POSITION);
        ((MainActivity) getActivity()).restoreActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setNavigationDrawerChecked(MENU_POSITION);
        setNavName();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    public interface OverviewFragmentInteractions {

    }

}
