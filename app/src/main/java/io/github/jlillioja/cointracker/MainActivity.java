package io.github.jlillioja.cointracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends Activity {

    public static boolean refreshDisplay;


    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();

    private ListView listView;
    private ArrayAdapter<String> listAdapter;

    private final String ADDRESS = "13xwb8zh1WaidugZwjVZExJwsnWT5xfDzf";
    private final String ADDRESS_FILENAME = "addresses";
    private SharedPreferences addresses;

    private final String tag = "CoinTracker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Register BroadcastReceiver to track connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);

        listAdapter = new ArrayAdapter<String>(
                this,
                R.layout.balance_entry,
                R.id.balance_entry_textview);

        listView = (ListView) findViewById(R.id.listview1);
        listView.setAdapter(listAdapter);

        addresses = getSharedPreferences(ADDRESS_FILENAME, 0);
        SharedPreferences.Editor editor = addresses.edit();
        editor.putString("address1",ADDRESS);
        editor.commit();

        Log.d(tag, "Completed onCreate");
    }

    public void onStart() {
        super.onStart();
        if (refreshDisplay) {
            Log.d(tag, "refreshDisplay = true, running loadPage");
            loadPage();
        } else {
            Log.d(tag, "refreshDisplay = false");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_refresh) {
            Toast.makeText(getApplicationContext(), "Refreshing...", Toast.LENGTH_SHORT).show();
            loadPage();
        }

        return super.onOptionsItemSelected(item);
    }

    // Uses AsyncTask subclass to download the JSON object from blockchain.info
    private void loadPage() {
        // AsyncTask subclass
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("blockchain.info")
                .appendPath("address")
                .appendPath(addresses.getString("address1", "0"))
                .appendQueryParameter("format", "json");
        String URL = builder.build().toString();
        Log.d(tag, "URL constructed: "+URL);
        new DownloadJsonTask().execute(URL);
    }

    // Displays an error if the app is unable to load content.
    private void showErrorPage() {
        setContentView(R.layout.main);
        if (!listAdapter.isEmpty()) {
            listAdapter.clear();
        }

        listAdapter.add(getResources().getString(R.string.connection_error));
    }

    private class DownloadJsonTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            Log.d(tag, "Doing in background: loadJsonFromNetwork "+urls[0]);
            try {
                return loadJsonFromNetwork(urls[0]);
            } catch (IOException e) {
                Log.d(tag, "DownloadJsonTask.doInBackground threw IOException");
                return getResources().getString(R.string.connection_error);
            } catch (JSONException e) {
                Log.d(tag, "DownloadJsonTask.doInBackground threw JSONException");
                return getResources().getString(R.string.json_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Displays the HTML string in the UI via a WebView
            listAdapter.clear();
            listAdapter.add(result);
        }
    }

    private String loadJsonFromNetwork(String urlString) throws JSONException, IOException {
        InputStream stream = null;
        String balance = null;
        String title = null;
        String url = null;
        String summary = null;
        Calendar rightNow = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");

        try {
            stream = downloadUrl(urlString);
            JsonParser jsonParser = new JsonParser();
            balance = jsonParser.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return balance;
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    public class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null) {
                refreshDisplay = true;
                Log.d(tag, "Network connected, refreshDisplay = true");
                Toast.makeText(context, R.string.network_connected, Toast.LENGTH_SHORT).show();
                loadPage();
            } else {
                refreshDisplay = false;
                Log.d(tag, "Network connection failed, refreshDisplay = false");
                Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
