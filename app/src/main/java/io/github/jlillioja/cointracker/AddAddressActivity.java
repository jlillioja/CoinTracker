package io.github.jlillioja.cointracker;

import android.os.Bundle;
import android.app.Activity;

public class AddAddressActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
