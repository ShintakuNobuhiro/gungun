package com.example.shintaku.test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

//トップ画面
public class LevelActivity extends AppCompatActivity {
    private int count[] = {0, 0};
    private int maxCount[] = {0, 0};

    ProgressBar prog[] = new ProgressBar[2];

    private MyTimerTask timerTask = null;
    private Timer timer = null;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);
        final SharedPreferences sp = getSharedPreferences("data",MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putString("thegeegegh", "qwerty123");
        e.commit();
        // ADD-S 2015/07/28 for read NFC
        // NFC-ID情報を表示する
        String nfcId = NfcActivity.nfcIdInfo;
        nfcId = "thegeegegh";
        String password = sp.getString(nfcId, "");
        Log.d("nfc", nfcId+","+password);

        post(nfcId,password);


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

    // タイマータスク用のクラス
    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            handler.post( new Runnable() {
                public void run() {
                    for(int i = 0; i < count.length; i++) {
                        if (count[i] < maxCount[i]) {
                            count[i]++;
                            prog[i].setProgress(count[i]);
                        }
                    }
                }
            });
        }
    }

    public void post(String nfcID, String password) {
        // POST通信を実行（AsyncTaskによる非同期処理を使うバージョン）
        ASyncPost task = new ASyncPost(LevelActivity.this, "https://railstutorial-ukyankyan-1.c9.io/users/1.json",
                // タスク完了時に呼ばれるUIのハンドラ
                new HttpPostHandler() {
                    @Override
                    public void onPostCompleted(String response) {
                        Log.d("start", response);
                        try {
                            //パース準備
                            JSONObject json = new JSONObject(response);
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

                            //プログレスバーの伸び率設定等
                            int id[] = {R.id.progressBar, R.id.progressBar2, R.id.progressBar3, R.id.progressBar4};
                            ProgressBar progressBar[] = new ProgressBar[id.length];
                            for (int i = 0; i < statuses.length(); i++) {
                                Log.d("statues", String.valueOf(i));
                                progressBar[i] = (ProgressBar) findViewById(id[i]);
                                progressBar[i].setMax(Integer.parseInt(next[i])); // 水平プログレスバーの最大値を設定
                                Log.d("max", String.valueOf(progressBar[i].getMax()));

                                // タイマーインスタンスを作成
                                timer = new Timer();
                                // タイマータスクインスタンスを作成
                                timerTask = new MyTimerTask();
                                // タイマースケジュールを設定
                                timer.schedule(timerTask, 0, 3);
                                // カウンタを初期化して設定
                                prog[i] = progressBar[i];
                                maxCount[i] = Integer.parseInt(exp[i]);
                            }
                        } catch (JSONException e) { //JSONObject等例外発生時
                            Log.e("error", e.toString());
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPostFailed(String response) {
                        Toast.makeText(getApplicationContext(), "エラーが発生しました。", Toast.LENGTH_LONG).show();
                    }
                });
        task.addPostParam("post_1", nfcID);
        task.addPostParam("post_2", password);

        // タスクを開始
        task.execute();
    }
}
