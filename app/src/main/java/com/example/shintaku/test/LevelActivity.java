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
    private int count[] = {0, 0, 0, 0};
    private int maxCount[] = {0, 0, 0, 0};

    ProgressBar prog[] = new ProgressBar[4];

    private MyTimerTask timerTask = null;
    private Timer timer = null;
    private Handler handler = new Handler();
    String nfcId,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);
        final SharedPreferences sp = getSharedPreferences("data",MODE_PRIVATE);
        // ADD-S 2015/07/28 for read NFC
        // NFC-ID情報を表示する
        nfcId = NfcActivity.nfcIdInfo;
        SharedPreferences.Editor e = sp.edit();
        e.putString("nfc_id", nfcId);
        e.apply();
        Log.d("",sp.getString(nfcId, ""));
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
            return postJsonObject("https://gungun.herokuapp.com/api/users/"+nfcId+".json", jobj);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            Log.d("result", String.valueOf(result));
            if (result != null) {
                try {
                    //パース

                    String[] name = {result.getString("name")};
                    JSONArray statuses = result.getJSONArray("statuses");
                    Log.d("length", String.valueOf(statuses.length()));
                    int exp[] = new int[statuses.length()];//現在経験値
                    int next[] = new int[statuses.length()];//レベルアップに必要な経験値
                    int level[] = new int[statuses.length()];//現在レベル
                    //ステータスの各項目分解、経験値の配列化
                    for (int i = 0; i < statuses.length(); i++) {
                        JSONObject status = statuses.getJSONObject(i);
                        exp[i] = status.getInt("experience");
                        next[i] = status.getInt("next_level_required_experience");
                        level[i] = status.getInt("level");
                    }
                    //名前の表示
                    final TextView nameTxt = (TextView) findViewById(R.id.name);
                    nameTxt.setText(name[0]);

                    int lvlId[] = {R.id.LevelView1,R.id.LevelView2,R.id.LevelView3,R.id.LevelView4};

                    //プログレスバーの伸び率設定等
                    int progId[] = {R.id.progressBar, R.id.progressBar2, R.id.progressBar3, R.id.progressBar4};
                    TextView levelView[] = new TextView[lvlId.length];
                    ProgressBar progressBar[] = new ProgressBar[progId.length];
                    for (int i = 0; i < statuses.length(); i++) {
                        //Log.d("statues", String.valueOf(i));
                        progressBar[i] = (ProgressBar) findViewById(progId[i]);
                        progressBar[i].setMax(next[i]); // 水平プログレスバーの最大値を設定
                        levelView[i] = (TextView) findViewById(lvlId[i]);
                        levelView[i].setText("レベル"+ level[i]);
                        //Log.d("max", String.valueOf(progressBar[i].getMax()));

                        // タイマーインスタンスを作成
                        timer = new Timer();
                        // タイマータスクインスタンスを作成
                        timerTask = new MyTimerTask();
                        // タイマースケジュールを設定
                        timer.schedule(timerTask, 0, 3);
                        // カウンタを初期化して設定
                        prog[i] = progressBar[i];
                        maxCount[i] = exp[i];
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
