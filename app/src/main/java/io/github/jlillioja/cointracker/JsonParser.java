package io.github.jlillioja.cointracker;

/**
 * Created by jlillioja on 8/14/2015.
 */

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class parses XML feeds from stackoverflow.com.
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class JsonParser {
    private static final String TAG_FINAL_BALANCE = "final_balance";

    // We don't use namespaces

    public String parse(InputStream in) throws IOException, JSONException {
        String balance = null;
        try {
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            JSONObject json = new JSONObject(sb.toString());
            balance = json.getString(TAG_FINAL_BALANCE);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        return balance;
    }
}
