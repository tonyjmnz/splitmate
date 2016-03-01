package tonyjmnz.splitmate;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class NewSplitgroupFragment extends Fragment {

    private NewSplitgroupInteractionListener mainActivity;
    ListView memberList;
    ArrayAdapter<String> adapter;
    ArrayList<String> adapterValues;
    Button addMember;
    Button createGroup;
    EditText splitgroupName;
    EditText newMemberEmail;

    public NewSplitgroupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_new_splitgroup, container, false);

        splitgroupName = (EditText) layout.findViewById(R.id.groupName);
        newMemberEmail = (EditText) layout.findViewById(R.id.newMember);

        addMember = (Button) layout.findViewById(R.id.addMember);
        addMember.setOnClickListener(onAddMemberClicked);
        createGroup = (Button) layout.findViewById(R.id.createGroup);
        createGroup.setOnClickListener(onCreateGroupClicked);


        adapterValues = new ArrayList<>();
        adapterValues.add(getString(R.string.me));

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_expandable_list_item_1,
                android.R.id.text1, adapterValues);

        memberList = (ListView) layout.findViewById(R.id.memberList);
        memberList.setAdapter(adapter);

        return layout;
    }
    private View.OnClickListener onCreateGroupClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String groupName = splitgroupName.getText().toString();
            if (groupName.trim().equals("")) {
                toast(getString(R.string.groupname_empty));
            } else {
                mainActivity.createSplitgroup(groupName, adapterValues);
            }
        }
    };

    private View.OnClickListener onAddMemberClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = newMemberEmail.getText().toString();
            newMemberEmail.setText("");
            if (adapter.getPosition(email) < 0) {
                adapter.add(email);
            } else {
                toast(getString(R.string.already_in_group));
            }
        }
    };

    private void toast(String message) {
        Context context = getContext();
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        mainActivity = (NewSplitgroupInteractionListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }


    public interface NewSplitgroupInteractionListener {
        void createSplitgroup(String groupName, ArrayList<String> members);
    }

}
