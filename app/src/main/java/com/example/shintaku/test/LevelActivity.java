package com.example.shintaku.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//トップ画面
public class LevelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);
        //非同期処理
        ASyncGet asyncGet = new ASyncGet(new AsyncCallback() {
            public void onPreExecute() {
            }
            public void onProgressUpdate(int progress) {
            }
            //非同期処理後
            public void onPostExecute(final String result) {
                Log.d("start", result);
                try {
                    //パース準備
                    JSONObject json = new JSONObject(result);
                    JSONArray statuses = json.getJSONArray("status");
                    String[] name = new String[1];
                    String exp[] = new String[statuses.length()];//現在経験値
                    String next[] = new String[statuses.length()];//レベルアップに必要な経験値

                    //ステータスの各項目分解、経験値の配列化
                    for (int i = 0; i < statuses.length(); i++) {
                        JSONObject status = statuses.getJSONObject(i);
                        exp[i] = status.getString("exp");
                        next[i] = status.getString("next");
                    }
                    //名前の表示
                    final TextView nameTxt = (TextView) findViewById(R.id.name);
                    name[0] = json.getString("name");
                    nameTxt.setText(name[0]);
                    Log.d("name", name[0]);

                    //プログレスバーの伸び率設定等
                    ProgressBar progressBar[] = new ProgressBar[4];
                    progressBar[0] = (ProgressBar) findViewById(R.id.progressBar);
                    progressBar[1] = (ProgressBar) findViewById(R.id.progressBar2);
                    progressBar[2] = (ProgressBar) findViewById(R.id.progressBar3);
                    progressBar[3] = (ProgressBar) findViewById(R.id.progressBar4);
                    for (int i = 0; i < statuses.length(); i++) {
                        Log.d("exp", exp[i]);
                        progressBar[i].setMax(Integer.parseInt(next[i])); // 水平プログレスバーの最大値を設定
                        progressBar[i].setProgress(Integer.parseInt(exp[i])); // 水平プログレスバーの値を設定
                        progressBar[i].setSecondaryProgress(60); // 水平プログレスバーのセカンダリ値を設定
                    }
                } catch (JSONException e) { //JSONObject等例外発生時
                    Log.e("error",e.toString());
                    e.printStackTrace();
                }
            }
            public void onCancelled() {
            }
        });
        asyncGet.execute("https://railstutorial-ukyankyan-1.c9.io/users/1.json"); //URL指定

        //課題選択ボタン
        Button btn = (Button) this.findViewById(R.id.button4);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LevelActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        //チェックボタン
        btn = (Button) this.findViewById(R.id.button8);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LevelActivity.this, MissionActivity.class);
                startActivity(intent);
            }
        });
        //すごろくボタン
        btn = (Button) this.findViewById(R.id.button9);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LevelActivity.this, SugorokuActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_level, menu);
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
