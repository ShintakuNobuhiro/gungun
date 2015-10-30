package com.example.shintaku.test;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        final SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        // IDとパスワード動的内部設定
        for(int i=1;; i++) {
            int resourceCardId = getResources().getIdentifier("card_number" + String.valueOf(i), "string", getPackageName());
            int resourcePassword = getResources().getIdentifier("password" + String.valueOf(i), "string", getPackageName());
            if(resourceCardId == 0) break;
            // リソースを取得
            String card_id = getResources().getString(resourceCardId);
            String password = getResources().getString(resourcePassword);
            e.putString(card_id, password);
        }
        e.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
