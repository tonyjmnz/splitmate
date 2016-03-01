package tonyjmnz.splitmate;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SignUpFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {

    EditText email;
    EditText repeatEmail;
    EditText password;
    Button signUp;

    private SignUpFragmentInteractions loginActivity;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_sign_up, container, false);

        email = (EditText) layout.findViewById(R.id.email);
        repeatEmail = (EditText) layout.findViewById(R.id.repeatEmail);
        password = (EditText) layout.findViewById(R.id.password);
        signUp = (Button) layout.findViewById(R.id.signup);
        signUp.setOnClickListener(onSignUpClicked);

        // Inflate the layout for this fragment
        return layout;
    }

    // TODO: Rename method, update argument and hook method into UI event
    private View.OnClickListener onSignUpClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!email.getText().toString().equals(repeatEmail.getText().toString())) {
                Context context = getContext();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, R.string.emails_must_match, duration);
                toast.show();
                return;
            }

            if (loginActivity != null) {
                loginActivity.onSignUp(email.getText().toString(), password.getText().toString());
            }
        }
    };

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            loginActivity = (SignUpFragmentInteractions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loginActivity = null;
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
    public interface SignUpFragmentInteractions {
        // TODO: Update argument type and name
        public void onSignUp(String email, String password);
    }

}
