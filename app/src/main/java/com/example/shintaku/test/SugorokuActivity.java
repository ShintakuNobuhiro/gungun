package com.example.shintaku.test;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class SugorokuActivity extends AppCompatActivity {
    String nfcId,password,URL;
    int cell,recent_cell;
    // view(「view」オブジェクトを格納する変数)の宣言
    private View view;

    // ハンドラを作成
    private Handler handler = new Handler();
    // ビューの再描画間隔(ミリ秒)
    private final static long MSEC = 15;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sugoroku);
        System.gc();
        final SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        nfcId = sp.getString("nfc_id","");
        password = sp.getString(nfcId,"");
        URL = sp.getString("URL","");
        new Loader().execute();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        System.gc();
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
            Log.d("result", String.valueOf(result));
            if (result != null) {
                try {
                    //パース
                    recent_cell = (int)result.getDouble("recent_cell");
                    cell = (int)result.getDouble("cell");
                    // 「GameView」オブジェクト(ビュー)の作成
                    view = new GameView(SugorokuActivity.this,recent_cell,cell);

                    // アクティビティにビューを組み込む
                    setContentView(view);

                    // ビュー再描画タイマー
                    // タイマーを作成
                    Timer timer = new Timer(false);
                    // 「MSEC」ミリ秒おきにタスク(TimerTask)を実行
                    timer.schedule(new TimerTask() {
                        public void run() {
                            handler.post(new Runnable() {
                                public void run() {
                                    // ビューを再描画
                                    view.invalidate();
                                }
                            });
                        }
                    }, 0, MSEC);
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
        Log.d("json", String.valueOf(json));
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
