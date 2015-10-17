package com.example.shintaku.test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    String nfcId,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);
        final SharedPreferences sp = getSharedPreferences("data",MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putString("thegeegegh", getString(R.string.password));
        e.apply();
        // ADD-S 2015/07/28 for read NFC
        // NFC-ID情報を表示する
        nfcId = NfcActivity.nfcIdInfo;
        password = sp.getString(nfcId, "");
        Log.d("nfc", nfcId+","+password);

        new Loader().execute();


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

    class Loader extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            JSONObject jobj = new JSONObject();

            try {
                jobj.put("card_number", nfcId);
                jobj.put("password", password);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return postJsonObject("https://railstutorial-ukyankyan-1.c9.io/missions.json", jobj);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            Log.d("result", String.valueOf(result));
            if (result != null) {
                try {
                    //パース準備
                    JSONArray statuses = result.getJSONArray("status");
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
                    name[0] = result.getString("name");
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
        }

    }

    public JSONObject postJsonObject(String url, JSONObject loginJobj){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL

            HttpPost httpPost = new HttpPost(url);

            System.out.println(url);
            String json = "";

            // 4. convert JSONObject to JSON to String

            json = loginJobj.toString();

            System.out.println(json);
            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        JSONObject json = null;
        try {
            json = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 11. return result
        Log.d("json",String.valueOf(json));
        return json;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}
