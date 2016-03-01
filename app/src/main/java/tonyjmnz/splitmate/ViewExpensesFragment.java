package tonyjmnz.splitmate;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewExpensesFragment extends Fragment {
    private ViewExpensesFragmentInteraction mainActivity;
    private View layout;
    private ApiWrapper api;
    private TextView myStatus;
    private String actionBarTitle;
    private Button createExpense;
    private int groupId;
    private ArrayList<Integer> expenseIds;

    private ArrayList<HashMap<String,String>> listItems = new ArrayList<>();
    private SimpleAdapter sa = null;
    private ListView expenseList;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new ApiWrapper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_view_expenses, container, false);
        myStatus = (TextView) layout.findViewById(R.id.myStatus);
        actionBarTitle = getArguments().getString("actionBarTitle");
        groupId = getArguments().getInt("groupId");
        createExpense = (Button) layout.findViewById(R.id.newExpense);
        createExpense.setOnClickListener(onCreateExpenseClicked);
        expenseIds = new ArrayList<>();
       /* descriptionValues = new ArrayList<>();
        balanceValues = new ArrayList<>();

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_expandable_list_item_1,
                android.R.id.text1, adapterValues);*/

        expenseList = (ListView) layout.findViewById(R.id.myExpenses);
        //memberList.setAdapter(adapter);

        getExpenses();

        // Inflate the layout for this fragment
        return layout;
    }

    private View.OnClickListener onCreateExpenseClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mainActivity.onCreateNewExpenseClicked(groupId);
        }
    };

    private void getExpenses() {
        api.getExpenses(((MainActivity) getActivity()).getUserId(), groupId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    double balance = response.getDouble("balance");
                    listItems = new ArrayList<>();
                    String sign = balance > 0 ? " +" : " ";
                    myStatus.setText(getString(R.string.my_status) + sign + Double.toString(balance));
                    JSONArray details = response.getJSONArray("detailedBalance");
                    HashMap<String, String> item;

                    for(int i = 0; i < details.length(); i++) {
                        JSONObject detail = ((JSONObject) details.get(i));
                        item = new HashMap<>();
                        item.put("line1", detail.getString("description"));
                        item.put("line2", Double.toString(detail.getDouble("balance")));

                        listItems.add(item);
                        expenseIds.add(detail.getInt("id"));

                        sa = new SimpleAdapter(getContext(), listItems, android.R.layout.two_line_list_item,
                                new String[] {"line1", "line2"},
                                new int[] {android.R.id.text1, android.R.id.text2});
                        expenseList.setAdapter(sa);
                        expenseList.setOnItemClickListener(onExpenseClicked);
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
    }

    private AdapterView.OnItemClickListener onExpenseClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mainActivity.onExpenseClicked(expenseIds.get(position), listItems.get(position).get("line1"), groupId);
        }
    };

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mainActivity = (ViewExpensesFragmentInteraction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity.onExpensesViewResumed(actionBarTitle);
    }

    public interface ViewExpensesFragmentInteraction {
        void onExpenseClicked(int expenseId, String expenseName, int groupId);
        void onExpensesViewResumed(String title);
        void onCreateNewExpenseClicked(int groupId);
    }

}
