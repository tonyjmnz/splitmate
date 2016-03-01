package tonyjmnz.splitmate;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ViewExpenseFragment extends Fragment {

    private int expenseId;
    private View layout;
    private OnFragmentInteractionListener mainActivity;


    private ApiWrapper api;
    private TextView payer;

    private ArrayList<HashMap<String,String>> listItems = new ArrayList<>();
    private SimpleAdapter sa = null;
    private ListView owerList;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new ApiWrapper(getContext());
    }

    public ViewExpenseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        expenseId = getArguments().getInt("expenseId");
        layout = inflater.inflate(R.layout.fragment_view_expense, container, false);
        payer = (TextView) layout.findViewById(R.id.paidBy);

        owerList = (ListView) layout.findViewById(R.id.owed);

        getExpense();

        return layout;
    }

    private void getExpense() {
        api.getExpense(expenseId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject payerObj = response.getJSONObject("payer");
                    listItems = new ArrayList<>();
                    String payerText = getString(R.string.paid_by) + " " + payerObj.get("email") +
                            ": " + Double.toString(payerObj.getDouble("amount"));
                    payer.setText(payerText);
                    JSONArray owers = response.getJSONArray("owers");
                    HashMap<String, String> item;

                    for (int i = 0; i < owers.length(); i++) {
                        JSONObject ower = ((JSONObject) owers.get(i));
                        item = new HashMap<>();
                        item.put("line1", getString(R.string.owed_by) + " " + ower.getString("email"));
                        item.put("line2", Double.toString(ower.getDouble("amount")));

                        listItems.add(item);

                        sa = new SimpleAdapter(getContext(), listItems, android.R.layout.two_line_list_item,
                                new String[]{"line1", "line2"},
                                new int[]{android.R.id.text1, android.R.id.text2});
                        owerList.setAdapter(sa);
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mainActivity = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
