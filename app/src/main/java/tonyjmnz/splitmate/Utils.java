package tonyjmnz.splitmate;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by tony on 12/14/15.
 */
public class Utils {
    public static void responseToast(JSONObject response, Context context) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = null;

        try {
            String msg = response.get("message").toString();
            if (msg.isEmpty()) {
                return;
            }

            toast = Toast.makeText(context, msg, duration);
            toast.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public static void toast(String text, Context context) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();


    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
