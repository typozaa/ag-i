package com.example.user_pc.ag_scanner;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by user-pc on 8/11/2016.
 */

public class BaseScannerActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

