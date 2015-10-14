package com.example.shintaku.test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

//チェック画面
public class MissionActivity extends AppCompatActivity {

    boolean clear[] = new boolean[4]; //達成状況の保存

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);
        final String nfcId = NfcActivity.nfcIdInfo;
        final SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        final String password = sp.getString(nfcId, "");
        Log.d("nfc", nfcId + "," + password);

        final String description[] = new String[4];
        final int missionId[] = {-1,-1,-1,-1};
        final int id[] = {R.id.checkButton, R.id.checkButton2, R.id.checkButton3, R.id.checkButton4};
        final int[] chkOn = new int[]{getResources().getColor(R.color.blue), getResources().getColor(R.color.green), getResources().getColor(R.color.orange), getResources().getColor(R.color.red)};
        final int[] chkOff = new int[]{getResources().getColor(R.color.lightblue), getResources().getColor(R.color.lightgreen), getResources().getColor(R.color.lightorange), getResources().getColor(R.color.lightred)};

        final Button chkBtn[] = new Button[id.length];
        for(int i=0; i<id.length;i++){
            chkBtn[i] = (Button) findViewById(id[i]);
            clear[i] = false;
            chkBtn[i].setBackgroundColor(chkOff[i]);
            chkBtn[i].setTextColor(getResources().getColor(R.color.black));
        }
        // POST通信を実行（AsyncTaskによる非同期処理を使うバージョン）
        ASyncPost task = new ASyncPost(MissionActivity.this,"https://",
                // タスク完了時に呼ばれるUIのハンドラ
                new HttpPostHandler() {
                    @Override
                    public void onPostCompleted(String response) {
                        Log.d("start", response);
                        try {
                            //パース準備
                            JSONObject json = new JSONObject(response);
                            JSONArray missions = json.getJSONArray("assigns");
                            JSONObject mission[] = new JSONObject[4];

                            //mission分解、説明の配列化
                            for (int i = 0; i < missions.length(); i++) {
                                mission[i] = missions.getJSONObject(i);
                                missionId[i] = mission[i].getInt("mission_id");
                                description[i] = mission[i].getString("description");
                                chkBtn[i].setText(description[i]);
                            }
                        } catch (JSONException e) {
                            Log.e("error",e.toString());
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPostFailed(String response) {
                        Toast.makeText(getApplicationContext(), "エラーが発生しました。", Toast.LENGTH_LONG).show();
                    }
                });
        task.addPostParam( "post_1", nfcId);
        task.addPostParam( "post_2", password);

        // タスクを開始
        task.execute();

        for(int i = 0; i < id.length; i++) {
            final int finalI = i;
            chkBtn[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!clear[finalI]) {
                        clear[finalI] = true;
                        chkBtn[finalI].setBackgroundColor(chkOn[finalI]);
                        chkBtn[finalI].setTextColor(getResources().getColor(R.color.white));
                    } else {
                        clear[finalI] = false;
                        chkBtn[finalI].setBackgroundColor(chkOff[finalI]);
                        chkBtn[finalI].setTextColor(getResources().getColor(R.color.black));
                    }
                    Log.d(String.valueOf(finalI),String.valueOf(clear[finalI]));
                }
            });
        }
        Button btnNext = (Button) this.findViewById(R.id.button2);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MissionActivity.this, LevelActivity.class);
                startActivity(intent);
                // POST通信を実行（AsyncTaskによる非同期処理を使うバージョン）
                ASyncPost task = new ASyncPost(MissionActivity.this,"https://",
                        // タスク完了時に呼ばれるUIのハンドラ
                        new HttpPostHandler() {
                            @Override
                            public void onPostCompleted(String response) {
                            }

                            @Override
                            public void onPostFailed(String response) {
                                Toast.makeText(getApplicationContext(), "エラーが発生しました。", Toast.LENGTH_LONG).show();
                            }
                        });
                task.addPostParam("post_1", nfcId);
                task.addPostParam("post_2", password);
                for(int i = 0; i < 4 ; i++) {
                    if(clear[i]=true && missionId[i]!=-1)
                        task.addPostParam("mission_ids[]", Arrays.toString(missionId));
                }

                // タスクを開始
                task.execute();
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
