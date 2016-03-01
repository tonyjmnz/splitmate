package tonyjmnz.splitmate;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class LoginFragment extends Fragment {
    EditText email;
    EditText password;
    Button login;
    Button signUp;

    private LoginFragmentInteractions loginActivity;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_login, container, false);

        email = (EditText) layout.findViewById(R.id.email);
        password = (EditText) layout.findViewById(R.id.password);
        login = (Button) layout.findViewById(R.id.login);
        signUp = (Button) layout.findViewById(R.id.signup);

        login.setOnClickListener(onLoginClicked);
        signUp.setOnClickListener(onSignUpClicked);
        // Inflate the layout for this fragment
        return layout;
    }
    private View.OnClickListener onLoginClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (loginActivity != null) {
                loginActivity.onLogin(email.getText().toString(), password.getText().toString());
            }
        }
    };

    private View.OnClickListener onSignUpClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (loginActivity != null) {
                loginActivity.onSignUp();
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            loginActivity = (LoginFragmentInteractions) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loginActivity = null;
    }

    public interface LoginFragmentInteractions {
        // TODO: Update argument type and name
        void onLogin(String email, String password);
        void onSignUp();
    }

}
