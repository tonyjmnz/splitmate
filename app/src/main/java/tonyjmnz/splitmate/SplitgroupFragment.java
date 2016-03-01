package tonyjmnz.splitmate;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SplitgroupFragment extends Fragment {

    private static final int MENU_POSITION = 2;
    private SplitgroupFragmentInteractions mainActivity;
    private Button newSplitgroup;
    private ListView groupsListView;
    private ArrayAdapter<String> groupsAdapter;
    private ArrayList<String> adapterValues = null;
    private ArrayList<Integer> groupIds;
    private ApiWrapper api;
    private View layout;

    public SplitgroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new ApiWrapper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        layout = inflater.inflate(R.layout.fragment_splitgroup, container, false);
        newSplitgroup = (Button) layout.findViewById(R.id.newSplitgroup);
        newSplitgroup.setOnClickListener(onNewSplitgroupClick);

        api.getSplitgroups(((MainActivity) getActivity()).getUserId(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray arr = response.getJSONArray("userGroups");
                    if (arr.length() > 0) {
                        setGroupList(arr);
                        showGroups();
                    } else {
                        setNoResults();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        return layout;
    }

    View.OnClickListener onNewSplitgroupClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mainActivity.onNewSplitgroupClicked();
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            setNavName();
            mainActivity = (SplitgroupFragmentInteractions) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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

    private void setNoResults() {
        TextView noResults = new TextView(getContext());
        noResults.setText(R.string.no_splitgroup);
        //noResults.setTextAppearance(android.R.style.);
        noResults.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        ((RelativeLayout)layout.findViewById(R.id.splitgroup_relative_layout)).addView(noResults);
    }

    public void setGroupList(ArrayList<String> groups, ArrayList<Integer> groupIds) {
        adapterValues = groups;
        this.groupIds = groupIds;
    }

    public void setGroupList(JSONArray groupList) {
        ArrayList<String> groups = new ArrayList();
        ArrayList<Integer> groupIds = new ArrayList<Integer>();
        for (int i = 0; i < groupList.length(); i++) {
            try {
                groups.add(((JSONObject)groupList.get(i)).getString("name"));
                groupIds.add(((JSONObject) groupList.get(i)).getInt("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setGroupList(groups, groupIds);
    }

    private void showGroups() {
        groupsListView = createListView(getContext());
        groupsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_expandable_list_item_1,
                android.R.id.text1, adapterValues);
        groupsListView.setAdapter(groupsAdapter);
        ((RelativeLayout)layout.findViewById(R.id.splitgroup_relative_layout)).addView(groupsListView);
    }

    private ListView createListView(Context context) {

        ListView listView = new ListView(context);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ListView.LayoutParams.WRAP_CONTENT,
                ListView.LayoutParams.MATCH_PARENT);
        p.addRule(RelativeLayout.ABOVE, R.id.newSplitgroup);
        listView.setLayoutParams(p);
        listView.setId(R.id.splitgroup_list_view);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mainActivity.onSplitgroupClicked(groupIds.get(position), adapterValues.get(position));
            }
        });
        return listView;
    }


    public interface SplitgroupFragmentInteractions {
        void onNewSplitgroupClicked();
        void onSplitgroupClicked(int groupId, String groupName);
    }

}
