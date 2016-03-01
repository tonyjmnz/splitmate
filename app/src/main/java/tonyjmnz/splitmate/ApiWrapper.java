package tonyjmnz.splitmate;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tony on 12/14/15.
 */
public class ApiWrapper {
    private String baseUrl = "http://test.tonyjmnz.com:8001";
    private Context context;

    public ApiWrapper(Context context) {
        this.context = context;
    }
    private String getUrl(String path) {
        return baseUrl + path;
    }

    public void login(String email, String password, Response.Listener success,
                      Response.ErrorListener error, boolean pwIsEncrypted) {

        if (!pwIsEncrypted) {
            password = Utils.md5(password);
        }

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        JSONObject json = new JSONObject(params);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (getUrl("/login"), json, success, error);

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }

    public void signUp(String email, String password, Response.Listener success,
                      Response.ErrorListener error) {

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", Utils.md5(password));

        JSONObject json = new JSONObject(params);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (getUrl("/signup"), json, success, error);

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }

    public void createSplitgroup(int creator, String groupName, ArrayList<String> members,
                                 Response.Listener success, Response.ErrorListener error) {

        // Post params to be sent to the server
        HashMap<String, Object> params = new HashMap<>();
        params.put("creatorId", Integer.toString(creator));
        params.put("groupName", groupName);
        params.put("members", members);

        JSONObject json = new JSONObject(params);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (getUrl("/splitgroup"), json, success, error);

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }

    public void getSplitgroups(int requester, Response.Listener success, Response.ErrorListener error) {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (getUrl("/user/splitgroups/" + Integer.toString(requester)), success, error);

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }

    public void getSplitgroupMembers(int groupId, Response.Listener success, Response.ErrorListener error) {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (getUrl("/splitgroup/" + Integer.toString(groupId)), success, error);

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }

    public void inviteNewMember(String email, int inviter, int groupId,
                                Response.Listener success, Response.ErrorListener error) {
        // Post params to be sent to the server
        HashMap<String, Object> params = new HashMap<>();
        params.put("inviterId", Integer.toString(inviter));
        params.put("groupId", Integer.toString(groupId));
        params.put("email", email);

        JSONObject json = new JSONObject(params);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (getUrl("/splitgroup/invite"), json, success, error);

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }

    public void newPayment(int payerId, int payeeId, int groupId, Double amount, String desc,
                                Response.Listener success, Response.ErrorListener error) {
        // Post params to be sent to the server
        HashMap<String, Object> params = new HashMap<>();
        params.put("payerId", payerId);
        params.put("payeeId", payeeId);
        params.put("groupId", groupId);
        params.put("amount", amount);
        params.put("desc", desc);

        JSONObject json = new JSONObject(params);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (getUrl("/payment"), json, success, error);

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }

    public void getMemberBalance(int userId, int groupId, Response.Listener success, Response.ErrorListener error) {
        // Post params to be sent to the server
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("groupId", groupId);

        JSONObject json = new JSONObject(params);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (getUrl("/balance"), json, success, error);

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }

    public void getExpenses(int userId, int groupId, Response.Listener success, Response.ErrorListener error) {
        // Post params to be sent to the server
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("groupId", groupId);

        JSONObject json = new JSONObject(params);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (getUrl("/expenses"), json, success, error);

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }

    public void getExpense(int expenseId, Response.Listener success, Response.ErrorListener error) {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (getUrl("/expense/" + Integer.toString(expenseId)), success, error);

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }

    public void createExpense(int userId, int groupId, String description, Double amount,
                              ArrayList<Integer> groupMemberIds, ArrayList<Double> amounts,
                              Response.Listener success, Response.ErrorListener error) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("groupId", groupId);
        params.put("description", description);
        params.put("amount", amount);
        params.put("userIds", groupMemberIds);
        params.put("amounts", amounts);

        JSONObject json = new JSONObject(params);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (getUrl("/expense/"), json, success, error);

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }
}
