package tonyjmnz.splitmate;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class PaymentFragment extends Fragment {

    private static final int MENU_POSITION = 3;
    private MainActivity mainActivity;
    private View layout;
    private ApiWrapper api;
    private Spinner groupSpinner;
    private Spinner memberSpinner;
    private ArrayList<Integer> groupIds;
    private ArrayList<String> groupNames;
    private ArrayList<Integer> memberIds;
    private ArrayList<String> memberNames;
    private ArrayAdapter<String> groupsAdapter;
    private ArrayAdapter<String> membersAdapter;
    private Button makePaymentBtn;
    private EditText makePaymentEditText;
    private TextView selfTextView;
    private TextView memberTextView;
    private int groupId = 0;
    private int memberId = 0;


    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_payment, container, false);
        api = new ApiWrapper(getContext());
        selfTextView = (TextView) layout.findViewById(R.id.selfTextView);
        memberTextView = (TextView) layout.findViewById(R.id.memberTextView);

        makePaymentBtn = (Button) layout.findViewById(R.id.makePaymentBtn);
        makePaymentEditText = (EditText) layout.findViewById(R.id.makePaymentEditText);
        groupSpinner = (Spinner) layout.findViewById(R.id.groupSpinner);
        memberSpinner = (Spinner) layout.findViewById(R.id.memberSpinner);
        groupSpinner.setOnItemSelectedListener(onGroupSelected);
        memberSpinner.setOnItemSelectedListener(onMemberSelected);
        makePaymentBtn.setOnClickListener(onMakePaymentClicked);
        getGroupSpinnerData();
        // Inflate the layout for this fragment
        return layout;
    }

    private View.OnClickListener onMakePaymentClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            api.newPayment(memberId, mainActivity.getUserId(), groupId,
                    Double.parseDouble(makePaymentEditText.getText().toString()), "",
                    new Response.Listener() {
                        @Override
                        public void onResponse(Object response) {
                            Utils.toast(getString(R.string.payment_processed), getContext());
                            clearUi();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
        }
    };

    private void clearUi() {
        memberTextView.setText("");
        selfTextView.setText("");
        makePaymentEditText.setText("");
        groupSpinner.setSelection(0);
        memberSpinner.setSelection(0);
    }

    private AdapterView.OnItemSelectedListener onGroupSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                if (membersAdapter != null) membersAdapter.clear();
                makePaymentBtn.setEnabled(false);
                makePaymentEditText.setEnabled(false);
                groupId = 0;
                return;
            }
            groupId = groupIds.get(position);
            getMemberSpinnerData(groupId);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener onMemberSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                makePaymentBtn.setEnabled(false);
                makePaymentEditText.setEnabled(false);
                selfTextView.setText("");
                memberTextView.setText("");
                memberId = 0;
                return;
            }
            memberId = memberIds.get(position);
            makePaymentBtn.setEnabled(true);
            makePaymentEditText.setEnabled(true);
            getSelectedMemberBalance();
            getUserBalance();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void getSelectedMemberBalance() {
        if (groupId == 0 || memberId == 0) return;
        api.getMemberBalance(memberId, groupId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Double balance = response.getDouble("balance");
                    String owe = balance > 0 ? getString(R.string.is_owed) : getString(R.string.owes);
                    String prefix = getString(R.string.that_member) + " " + owe;
                    memberTextView.setText(prefix + " " + Double.toString(Math.abs(balance)));
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

    private void getUserBalance() {
        if (groupId == 0) return;
        api.getMemberBalance(mainActivity.getUserId(), groupId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Double balance = response.getDouble("balance");
                    String owe = balance > 0 ? getString(R.string.are_owed) : getString(R.string.owe);
                    String prefix = getString(R.string.you) + " " + owe;
                    selfTextView.setText(prefix + " " + Double.toString(Math.abs(balance)));
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

    private void getGroupSpinnerData() {
        api.getSplitgroups(((MainActivity) mainActivity).getUserId(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                attachGroupSpinnerData(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private void getMemberSpinnerData(int groupId) {
        api.getSplitgroupMembers(groupId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                attachMemberSpinnerData(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private void attachGroupSpinnerData(JSONObject response) {
        groupIds = new ArrayList<>();
        groupIds.add(0);
        groupNames = new ArrayList<>();
        groupNames.add(getString(R.string.select_a_group));

        try {
            JSONArray arr = response.getJSONArray("userGroups");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = (JSONObject) arr.get(i);
                groupIds.add(item.getInt("id"));
                groupNames.add(item.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        groupsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, groupNames);
        groupsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(groupsAdapter);

    }

    private void attachMemberSpinnerData(JSONObject response) {
        memberIds = new ArrayList<>();
        memberIds.add(0);
        memberNames = new ArrayList<>();
        memberNames.add(getString(R.string.select_a_member));

        try {
            JSONArray arr = response.getJSONArray("members");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = (JSONObject) arr.get(i);
                memberIds.add(item.getInt("id"));
                memberNames.add(item.getString("email"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        membersAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, memberNames);
        membersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        memberSpinner.setAdapter(membersAdapter);


    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setNavName();

        try {
            mainActivity = (MainActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setNavigationDrawerChecked(MENU_POSITION);
        setNavName();
    }

    private void setNavName() {
        ((MainActivity) getActivity()).onSectionAttached(MENU_POSITION);
        ((MainActivity) getActivity()).restoreActionBar();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }
}
