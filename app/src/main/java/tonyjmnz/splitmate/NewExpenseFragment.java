package tonyjmnz.splitmate;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class NewExpenseFragment extends Fragment {
    private OnNewExpenseFragmentInteraction mainActivity;
    private View layout;
    private int groupId;
    private ApiWrapper api;
    private LayoutInflater inflater;
    private RadioButton splitEvenly;
    private RadioButton splitCustom;
    private View paymentList;
    private EditText paidByMeEditText;
    private ArrayList<EditText> listEditTexts;
    private TextWatcher splitEvenlyListener;
    private Button createExpense;
    private EditText descriptionEditText;
    private boolean isSplittingEvenly;
    private ArrayList<Integer> groupMemberIds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        layout = inflater.inflate(R.layout.fragment_new_expense, container, false);
        api = new ApiWrapper(getContext());
        listEditTexts = new ArrayList<>();
        groupMemberIds = new ArrayList<>();
        descriptionEditText = (EditText) layout.findViewById(R.id.descEditText);
        groupId = getArguments().getInt("groupId");
        createExpense = (Button) layout.findViewById(R.id.createExpense);
        createExpense.setOnClickListener(onCreateExpenseClicked);
        paymentList = layout.findViewById(R.id.paymentList);
        paidByMeEditText = (EditText) layout.findViewById(R.id.paidByMeEditText);
        splitEvenly = (RadioButton) layout.findViewById(R.id.splitEvenly);
        splitCustom = (RadioButton) layout.findViewById(R.id.splitCustom);
        splitEvenly.setOnClickListener(onSplitEvenlySelected);
        splitCustom.setOnClickListener(onSplitCustomSelected);


        api.getSplitgroupMembers(groupId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray arr = response.getJSONArray("members");
                    createList(arr);
                    addSplitEvenlyListener();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Inflate the layout for this fragment

        return layout;
    }

    private View.OnClickListener onCreateExpenseClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(descriptionEditText.getText().toString().equals("")){
                Utils.toast(getString(R.string.empty_desc), getContext());
            } else if(paidByMeEditText.getText().toString().equals("")) {
                Utils.toast(getString(R.string.empty_cost), getContext());
            } else if (Double.parseDouble(paidByMeEditText.getText().toString()) < 1) {
                Utils.toast(getString(R.string.cost_too_low), getContext());
            } else if (!isSplittingEvenly && !costsMatch()) {
                Utils.toast(getString(R.string.costs_dont_match), getContext());
            } else {
                formatAndCreateExpense();
            }
        }
    };

    private void formatAndCreateExpense() {
        int userId = ((MainActivity)getContext()).getUserId();
        //groupId
        String description = descriptionEditText.getText().toString();
        Double amount = Double.parseDouble(paidByMeEditText.getText().toString());
        ArrayList<Double> amounts = new ArrayList<>();
        for (EditText et : listEditTexts) {
            amounts.add(Double.parseDouble(et.getText().toString()));
        }

        api.createExpense(userId, groupId, description, amount, groupMemberIds, amounts,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mainActivity.onExpenseAdded();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
    }
    private boolean costsMatch() {
        double sum = 0;
        for (EditText et : listEditTexts) {
            sum += Double.parseDouble(et.getText().toString());
        }

        if (Math.abs(Double.parseDouble(paidByMeEditText.getText().toString()) - sum) <= 0.01) {
            return true;
        }

        return false;
    }

    private View.OnClickListener onSplitEvenlySelected = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addSplitEvenlyListener();
        }
    };

    private void addSplitEvenlyListener() {
        disableEditTexts();
        setToZero();
        splitEvenlyListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    setToZero();
                    return;
                }
                splitCostEvenly(Double.parseDouble(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        paidByMeEditText.addTextChangedListener(splitEvenlyListener);
        isSplittingEvenly = true;
        String text = paidByMeEditText.getText().toString();
        if (!text.equals("")) {
            splitCostEvenly(Double.parseDouble(text));
        }
    }

    private void setToZero() {
        for (EditText et : listEditTexts) {
            et.setText("0.00");
        }
    }

    private void splitCostEvenly(double cost) {
        double split = cost / listEditTexts.size();
        for (EditText et : listEditTexts) {
            et.setText(String.format("%.2f", split));
        }
    }

    private View.OnClickListener onSplitCustomSelected = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            paidByMeEditText.removeTextChangedListener(splitEvenlyListener);
            isSplittingEvenly = false;
            enableEditTexts();
        }
    };

    private void createList(JSONArray members) {
        ArrayList<String> groupMembers = new ArrayList<>();

        for (int i = 0; i < members.length(); i++) {
            try {
                groupMembers.add(((JSONObject) members.get(i)).getString("email"));
                this.groupMemberIds.add(((JSONObject) members.get(i)).getInt("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ViewGroup container = (ViewGroup)layout.findViewById(R.id.paymentList);


        for (String member: groupMembers) {
            View listItem = inflater.inflate(R.layout.payment_list_item, container, false);
            ((TextView)listItem.findViewById(R.id.nameTextView)).setText(member);
            listEditTexts.add((EditText) listItem.findViewById(R.id.amountEditText));
            //RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
            //        RelativeLayout.LayoutParams.WRAP_CONTENT);
            //p.addRule(RelativeLayout.BELOW);
            //listItem.setLayoutParams(p);
            ((ViewGroup) paymentList).addView(listItem);
        }
    }

    private void disableEditTexts() {
        for (EditText et : listEditTexts) {
            et.setEnabled(false);
        }
    }



    private void enableEditTexts() {
        for (EditText et : listEditTexts) {
            et.setEnabled(true);
        }
    }


    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mainActivity = (OnNewExpenseFragmentInteraction) activity;
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

    public interface OnNewExpenseFragmentInteraction {
        // TODO: Update argument type and name
        void onExpenseAdded();
    }

}
