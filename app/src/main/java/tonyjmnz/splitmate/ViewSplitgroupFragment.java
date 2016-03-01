package tonyjmnz.splitmate;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ViewSplitgroupFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private int groupId;
    private ApiWrapper api;
    private ArrayAdapter<String> membersAdapter;
    private ArrayList<String> adapterValues = null;
    private ArrayList<Integer> memberIds;
    private ListView membersListView;
    private EditText inviteMemberEditText;
    private View layout;
    private Button inviteMember;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new ApiWrapper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_view_splitgroup, container, false);
        groupId = getArguments().getInt("groupId");
        inviteMember = (Button) layout.findViewById(R.id.inviteMember);
        inviteMemberEditText = (EditText) layout.findViewById(R.id.inviteMemberEditText);

        inviteMember.setOnClickListener(onInviteMemberClicked);
        getMembers();

        // Inflate the layout for this fragment
        return layout;
    }
    private void getMembers() {
        api.getSplitgroupMembers(groupId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray arr = response.getJSONArray("members");
                    setMemberList(arr);
                    showMembers();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private View.OnClickListener onInviteMemberClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (inviteMemberEditText.getText().toString().equals("")) {
                Utils.toast(getString(R.string.email_empty), getContext());
                return;
            }
            int userId = ((MainActivity) getActivity()).getUserId();
            api.inviteNewMember(inviteMemberEditText.getText().toString(), userId, groupId,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Utils.responseToast(response, getContext());
                            getMembers();
                            inviteMemberEditText.setText("");
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
        }
    };

    public void setMemberList(ArrayList<String> members, ArrayList<Integer> memberIds) {
        adapterValues = members;
        this.memberIds = memberIds;
    }

    public void setMemberList(JSONArray memberList) {
        ArrayList<String> members = new ArrayList();
        ArrayList<Integer> memberIds = new ArrayList<Integer>();
        for (int i = 0; i < memberList.length(); i++) {
            try {
                members.add(((JSONObject) memberList.get(i)).getString("email"));
                memberIds.add(((JSONObject) memberList.get(i)).getInt("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setMemberList(members, memberIds);
    }

    private void showMembers() {
        membersListView = createListView(getContext());
        membersAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_expandable_list_item_1,
                android.R.id.text1, adapterValues);
        membersListView.setAdapter(membersAdapter);
        ((RelativeLayout)layout.findViewById(R.id.view_splitgroup_layout)).addView(membersListView);
    }

    private ListView createListView(Context context) {

        ListView listView = new ListView(context);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ListView.LayoutParams.WRAP_CONTENT,
                ListView.LayoutParams.MATCH_PARENT);
        p.addRule(RelativeLayout.ABOVE, R.id.inviteMemberEditText);
        listView.setLayoutParams(p);
        listView.setId(R.id.splitgroup_list_view);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //mainActivity.onSplitgroupClicked(groupIds.get(position), adapterValues.get(position));
            }
        });
        return listView;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
