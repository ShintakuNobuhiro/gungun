package com.example.shintaku.test;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

//トップ画面
public class LevelActivity extends AppCompatActivity {
    private int count[] = {0, 0, 0, 0};
    private int maxCount[] = {0, 0, 0, 0};

    ProgressBar prog[] = new ProgressBar[4];

    private Handler handler = new Handler();
    String nfcId,password,URL;
    int recentlevel[] = new int[4];
    int level[] = new int[recentlevel.length];//現在レベル
    ArrayList<Integer> requireExp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requireExp = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);
        final SharedPreferences sp = getSharedPreferences("data",MODE_PRIVATE);
        // ADD-S 2015/07/28 for read NFC
        // NFC-ID情報を表示する
        nfcId = NfcActivity.nfcIdInfo;
        SharedPreferences.Editor e = sp.edit();
        e.putString("nfc_id", nfcId);
        e.apply();
        Log.d("", sp.getString(nfcId, ""));
        password = sp.getString(nfcId, "");
        URL = sp.getString("URL","");
        Log.d("nfc", nfcId + "," + password);

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
                for(int i=0;i<recentlevel.length;i++) {
                    recentlevel[i] = level[i];
                    intent.putExtra("recentlevel"+i,recentlevel[i]);
                }
                int requestCode = 810;
                // 返却値を考慮したActivityの起動を行う
                startActivityForResult(intent, requestCode);
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
    protected void onResume() {
        super.onResume();
        android.net.ConnectivityManager connectivityManager =
                (android.net.ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);

        android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new Levels().execute();
            new Loader().execute();
        } else {
            Toast.makeText(this, "エラー:インターネットにつながっていないよ！\n\n支援者の方へ：インターネット未接続です。Wi-Fi等の状態を確かめた上で再試行してください。", Toast.LENGTH_LONG).show();
            finish();
        }
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
                            prog[i].setSecondaryProgress(count[i]);
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
            return postJsonObject(URL+"/api/users/"+nfcId+".json", jobj);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    Log.d("json",String.valueOf(result));
                    //パース
                    String[] name = {result.getString("name")};
                    JSONArray statuses = result.getJSONArray("statuses");
                    int recentExp[] = new int[statuses.length()];
                    int exp[] = new int[statuses.length()];//現在経験値
                    int next[] = new int[statuses.length()];//レベルアップに必要な経験値
                    int recentExpTmp[] = new int[statuses.length()];
                    int expTmp[] = new int[statuses.length()];//現在経験値
                    int nextTmp[] = new int[statuses.length()];//レベルアップに必要な経験値
                    int levelTmp[] = new int[statuses.length()];
                    String category[] = new String[statuses.length()];
                    //ステータスの各項目分解、経験値の配列化
                    for (int i = 0; i < statuses.length(); i++) {
                        JSONObject status = statuses.getJSONObject(i);
                        recentExpTmp[i] = status.getInt("recent_experience");
                        expTmp[i] = status.getInt("experience");
                        nextTmp[i] = status.getInt("next_level_required_experience");
                        levelTmp[i] = status.getInt("level");
                        category[i] = status.getString("category");
                    }
                    //名前の表示
                    final TextView nameTxt = (TextView) findViewById(R.id.name);
                    nameTxt.setText("こんにちは\n"+name[0]+"さん");

                    int lvlId[] = {R.id.LevelView1,R.id.LevelView2,R.id.LevelView3,R.id.LevelView4};

                    //プログレスバーの伸び率設定等
                    int progId[] = {R.id.progressBar, R.id.progressBar2, R.id.progressBar3, R.id.progressBar4};
                    int catId[] = {R.id.textView3, R.id.textView5, R.id.textView6, R.id.textView7};
                    TextView levelView[] = new TextView[lvlId.length];
                    ProgressBar progressBar[] = new ProgressBar[progId.length];
                    TextView categoryView[] = new TextView[catId.length];

                    for(int i=0;i<statuses.length();i++){
                        if(category[i].equals("けんこう")) {
                            exp[0] = expTmp[i];
                            next[0] = nextTmp[i];
                            recentExp[0] = recentExpTmp[i];
                            level[0] = levelTmp[i];
                        }
                        else if(category[i].equals("お友だちとあいさつ")){
                            exp[1] = expTmp[i];
                            next[1] = nextTmp[i];
                            recentExp[1] = recentExpTmp[i];
                            level[1] = levelTmp[i];
                        } else if(category[i].equals("うんどうとおしごと")){
                            exp[2] = expTmp[i];
                            next[2] = nextTmp[i];
                            recentExp[2] = recentExpTmp[i];
                            level[2] = levelTmp[i];
                        } else if(category[i].equals("くらし")){
                            exp[3] = expTmp[i];
                            next[3] = nextTmp[i];
                            recentExp[3] = recentExpTmp[i];
                            level[3] = levelTmp[i];
                        }
                    }

                    for (int i = 0; i < statuses.length(); i++) {
                        progressBar[i] = (ProgressBar) findViewById(progId[i]);
                        categoryView[i] = (TextView) findViewById(catId[i]);
                        int e;
                        if(requireExp != null) {
                            e = requireExp.get(level[i] - 1);
                            progressBar[i].setMax(next[i] - e); // 水平プログレスバーの最大値を設定
                        }
                        else {
                            Toast.makeText(LevelActivity.this, "もういちどカードをよみこんでね", Toast.LENGTH_LONG).show();
                            progressBar[i].setMax(next[i]);
                        }
                        if(exp[i] >= recentExp[i])
                            progressBar[i].setProgress(recentExp[i]-requireExp.get(level[i]-1));
                        levelView[i] = (TextView) findViewById(lvlId[i]);
                        if(level[i] == recentlevel[i] || recentlevel[i] == 0)
                            levelView[i].setText("レベル"+ level[i]);
                        else {
                            levelView[i].setTextColor(Color.RED);
                            levelView[i].setText("レベルアップ！！ レベル" + level[i]);
                        }

                        MyTimerTask timerTask;
                        Timer timer;
                        // タイマーインスタンスを作成
                        timer = new Timer();
                        // タイマータスクインスタンスを作成
                        timerTask = new MyTimerTask();
                        // タイマースケジュールを設定
                        timer.schedule(timerTask, 0, 300);
                        // カウンタを初期化して設定
                        prog[i] = progressBar[i];
                        maxCount[i] = exp[i]-requireExp.get(level[i]-1);
                        count[i] = 0;
                        Log.d("progress" + i, next[i] - requireExp.get(level[i] - 1) + "," + maxCount[i]);
                    }

                    JSONArray missions;
                    missions = result.getJSONArray("assigns");
                    //assign無しなら中ボタン無効に
                    Button btn = (Button) findViewById(R.id.button8);
                    if(missions.length()==0) {
                        btn.setEnabled(false);
                    } else {
                        btn.setEnabled(true);
                    }
                } catch (JSONException e) { //JSONObject等例外発生時
                    Log.e("error", e.toString());
                    e.printStackTrace();
                }
            }
        }

    }

    class Levels extends AsyncTask<Void, Void, JSONArray> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (requireExp != null) {
                requireExp.clear();
                requireExp = new ArrayList<>();
            }
        }

        @Override
        protected JSONArray doInBackground(Void... params) {

            JSONObject jobj = new JSONObject();

            try {
                jobj.put("card_number", nfcId);
                jobj.put("password", password);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return postJsonArray(URL + "/api/levels.json", jobj);
        }

        protected void onPostExecute(JSONArray result) {
            try {
                for (int i = 0; i < 20; i++) {
                    requireExp.add(result.getJSONObject(i).getInt("required_experience"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
        return json;
    }

    public JSONArray postJsonArray(String url, JSONObject loginJobj){
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

        JSONArray json = null;
        try {
            json = new JSONArray(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 11. return result
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

    public void onActivityResult( int requestCode, int resultCode, Intent intent ) {
        // startActivityForResult()の際に指定した識別コードとの比較
        if (requestCode == 810) {
            // 返却結果ステータスとの比較
            if (resultCode == Activity.RESULT_OK) {
                // 返却されてきたintentから値を取り出す
                for (int i = 0; i < recentlevel.length; i++) {
                    recentlevel[i] = intent.getIntExtra("recentlevel" + i, 0);
                }
            }
        }
        new Levels().execute();
        new Loader().execute();
    }
}
