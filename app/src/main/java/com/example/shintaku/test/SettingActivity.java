package com.example.shintaku.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Log.d("a", "a");
        final String[] description = new String[2];

        ASyncGet asyncGet = new ASyncGet(new AsyncCallback() {
            public void onPreExecute() {
                // do something
            }
            public void onProgressUpdate(int progress) {
                // do something
            }
            public void onPostExecute(final String result) {
                Log.d("start","start");
                if (result != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        JSONArray missions = json.getJSONArray("missions");

                        for (int i = 0; i < missions.length(); i++) {
                            JSONObject mission = missions.getJSONObject(i);
                            description[i] = mission.getString("description");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("result","null");
                }
            }

            public void onCancelled() {
                // do something
            }
        });
        asyncGet.execute("https://railstutorial-ukyankyan-1.c9.io/users/1.json");

        Button btnNext = (Button) this.findViewById(R.id.toggleButton); //健康
        final Button finalBtnNext = btnNext;
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = "健康";
                Settings text = new Settings(txt);
                finalBtnNext.setText(description[0]);

                Intent intent = new Intent(SettingActivity.this, Setting2Activity.class);
                intent.putExtra("test_result", text);
                startActivity(intent);

            }
        });
        btnNext = (Button) this.findViewById(R.id.toggleButton3); //運動とお仕事
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt="運動とお仕事";
                Settings text = new Settings(txt);

                Intent intent = new Intent(getApplicationContext(), Setting2Activity.class);
                intent.putExtra("test_result", text);
                startActivity(intent);
            }
        });

        btnNext = (Button) this.findViewById(R.id.button3);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //トップ画面に戻る実装
                Intent intent = new Intent(SettingActivity.this, LevelActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
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
