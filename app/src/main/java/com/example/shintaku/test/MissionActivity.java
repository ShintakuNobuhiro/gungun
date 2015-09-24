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

//チェック画面
public class MissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);
        final String description[] = new String[4];
        final int id[] = {R.id.checkButton, R.id.checkButton2, R.id.checkButton3, R.id.checkButton4,};
        final Button chkBtn[] = new Button[id.length];
        for(int i=0; i<id.length;i++){
            chkBtn[i] = (Button) findViewById(id[i]);
        }
        ASyncGet asyncGet = new ASyncGet(new AsyncCallback() {
            public void onPreExecute() {
            }
            public void onProgressUpdate(int progress) {
            }
            public void onPostExecute(final String result) {
                Log.d("start", result);
                try {
                    //パース準備
                    JSONObject json = new JSONObject(result);
                    JSONArray missions = json.getJSONArray("missions");

                    //mission分解、説明の配列化
                    for (int i = 0; i < missions.length(); i++) {
                        JSONObject mission = missions.getJSONObject(i);
                        description[i] = mission.getString("description");
                        chkBtn[i].setText(description[i]);
                    }
                } catch (JSONException e) {
                    Log.e("error",e.toString());
                    e.printStackTrace();
                }
            }
            public void onCancelled() {
            }
        });
        asyncGet.execute("https://railstutorial-ukyankyan-1.c9.io/users/1.json");

        chkBtn[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = getResources().getColor(R.color.blue);
                chkBtn[0].setBackgroundColor(color);
            }
        });
        Button btnNext = (Button) this.findViewById(R.id.button2);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MissionActivity.this, LevelActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mission, menu);
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
