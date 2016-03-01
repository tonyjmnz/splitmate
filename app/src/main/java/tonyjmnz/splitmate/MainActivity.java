package tonyjmnz.splitmate;


import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        SplitgroupFragment.SplitgroupFragmentInteractions,
        NewSplitgroupFragment.NewSplitgroupInteractionListener,
        ExpensesFragment.ExpensesFragmentInteractions,
        ViewExpensesFragment.ViewExpensesFragmentInteraction,
        NewExpenseFragment.OnNewExpenseFragmentInteraction {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    boolean firstFragment = true;
    ApiWrapper api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();
        api = new ApiWrapper(this);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        getSupportFragmentManager().addOnBackStackChangedListener(backStackChangedListener);

    }

    //trigger onresume when fragments are restored from back stack
    android.support.v4.app.FragmentManager.OnBackStackChangedListener backStackChangedListener =
            new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    FragmentManager manager = getSupportFragmentManager();

                    if (manager != null)
                    {
                        Fragment currFrag = manager.
                                findFragmentById(R.id.container);

                        currFrag.onResume();
                    }
                }
            };
    public void setNavigationDrawerChecked(int position) {
        mNavigationDrawerFragment.setItemChecked(position);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;

        switch (position + 1) {
            case 1:
                //fragment = new OverviewFragment();
                fragment = new ExpensesFragment();
                break;
            case 2:
                fragment = new SplitgroupFragment();
                break;
            case 3:
                fragment = new PaymentFragment();
                break;
            case 4:
                logout();
                break;
            case 5:
                break;
        }

        if(fragment == (null)) {
            return;
        }

        FragmentTransaction t = fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, fragment.getClass().toString());

        if(!firstFragment) {
            t.addToBackStack(null);
        }

        t.commit();
        firstFragment = false;
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                //mTitle = getString(R.string.overview);
                mTitle = getString(R.string.expenses);
                break;
            case 2:
                mTitle = getString(R.string.splitgroups);
                break;
            case 3:
                mTitle = getString(R.string.make_payment);
                break;
            case 4:
                break;
        }

    }

    public void logout() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(getString(R.string.user_id));
        editor.commit();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
    public int getUserId() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPref.getInt(getString(R.string.user_id), 0);
    }
    @Override
    public void createSplitgroup(String groupName, ArrayList<String> members) {
        ArrayList filteredMembers = new ArrayList(members);
        filteredMembers.remove(getString(R.string.me));

        api.createSplitgroup(getUserId(), groupName, filteredMembers, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Utils.responseToast(response, getApplicationContext());
                FragmentManager fm = getSupportFragmentManager();
                fm.popBackStack();
                /*SplitgroupFragment f = (SplitgroupFragment) fm.findFragmentByTag(SplitgroupFragment.class.toString());
                try {
                    f.setMemberList(response.getJSONArray("userGroups"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    public void onNewSplitgroupClicked() {
        mTitle = getString(R.string.new_splitgroup);
        getSupportActionBar().setTitle(mTitle);
        Fragment newSplitgroup = new NewSplitgroupFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, newSplitgroup)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSplitgroupClicked(int groupId, String groupName) {
        mTitle = groupName + " " + getString(R.string.members);
        getSupportActionBar().setTitle(mTitle);


        Fragment viewSplitgroup = new ViewSplitgroupFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("groupId", groupId);
        viewSplitgroup.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, viewSplitgroup)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onExpensesGroupClicked(int groupId, String groupName) {
        mTitle = getString(R.string.expenses_for) + " " + groupName;
        getSupportActionBar().setTitle(mTitle);

        Fragment viewGroupExpenses = new ViewExpensesFragment();

        Bundle bundle = new Bundle();
        bundle.putString("actionBarTitle", mTitle.toString());
        bundle.putInt("groupId", groupId);
        viewGroupExpenses.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, viewGroupExpenses)
                .addToBackStack(viewGroupExpenses.getClass().toString())
                .commit();
    }

    @Override
    public void onExpenseClicked(int expenseId, String expenseName, int groupId) {
        mTitle = expenseName;
        getSupportActionBar().setTitle(mTitle);

        Fragment viewExpenseFragment = new ViewExpenseFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("expenseId", expenseId);
        bundle.putInt("groupId", groupId);
        viewExpenseFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, viewExpenseFragment)
                .addToBackStack(viewExpenseFragment.getClass().toString())
                .commit();
    }

    @Override
    public void onExpensesViewResumed(String title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onCreateNewExpenseClicked(int groupId) {
        mTitle = getString(R.string.new_expense);
        getSupportActionBar().setTitle(mTitle);

        Fragment newExpenseFragment = new NewExpenseFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("groupId", groupId);
        newExpenseFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, newExpenseFragment)
                .addToBackStack(newExpenseFragment.getClass().toString())
                .commit();
    }

    @Override
    public void onExpenseAdded() {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
    }
}
