package io.github.jlillioja.cointracker;

/**
 * Created by jlillioja on 8/14/2015.
 */

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonParser {


    // We don't use namespaces

    public JSONObject parse(InputStream in) throws IOException, JSONException {
        JSONObject json = null;
        try {
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
             json = new JSONObject(sb.toString());
            //balance = json.getString(TAG_FINAL_BALANCE);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        if (json == null) {
            Log.e("JsonParser", "json failed to parse");
        }
        return json;
    }
}
