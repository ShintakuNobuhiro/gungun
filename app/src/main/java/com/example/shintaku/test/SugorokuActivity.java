package com.example.shintaku.test;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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
import java.util.ArrayList;

public class SugorokuActivity extends AppCompatActivity {
    String nfcId,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sugoroku);
        final SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        nfcId = sp.getString("nfc_id","");
        password = sp.getString(nfcId,"");
        new Loader().execute();
        //戻る
        Button btn;
        btn = (Button) this.findViewById(R.id.button7);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SugorokuActivity.this, LevelActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }

    // Frameアニメーション
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void frameAnimationTest(ImageView img){
        AnimationDrawable anim = new AnimationDrawable();
        ArrayList<Drawable> frame = new ArrayList<>();

        // 画像の読み込み //
        for(int i=0;i<5;i++) {
            frame.add(getResources().getDrawable(getResources().getIdentifier("image" + i, "drawable", getPackageName())));
            anim.addFrame(frame.get(i),  300);
        }
        // 繰り返し設定
        anim.setOneShot(false);
        // 画像にアニメーションを設定
        img.setBackground(anim);
        // アニメーション開始
        anim.start();
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
/*            if (result != null) {
                try {
                    //パース
                    String[] name = {result.getString("name")};

                    AnimationDrawable anim = new AnimationDrawable();
                    ArrayList<Drawable> frame = new ArrayList<>();
                    ArrayList<Integer> id = new ArrayList<>();
                    int j = 0;
                    while(getResources().getIdentifier("image" + j, "drawable", getPackageName()) != 0) {
                        id.add(getResources().getIdentifier("image" + j, "drawable", getPackageName()));
                        j++;
                    }

                    int sumCell = id.size();
                    int recentCell = result.getInt("recent_cell")%sumCell;
                    int cell = result.getInt("cell")%sumCell;


                    // 画像の読み込み //
                    int dCell = cell - recentCell;
                    Log.d("", String.valueOf(recentCell)+","+String.valueOf(cell)+","+String.valueOf(dCell));
                    int interval;
                    if(recentCell<cell) {
                        for (int i = 0; i < dCell; i++) {
                            frame.add(getResources().getDrawable(id.get(recentCell + i)));
                            if (i == 0)
                                interval = 1000;
                            else
                                interval = 500;
                            anim.addFrame(frame.get(i), interval);
                        }
                    } else {
                        frame.add(getResources().getDrawable(id.get(cell)));
                        anim.addFrame(frame.get(0),500);
                    }
                    // 繰り返し設定
                    anim.setOneShot(true);
                    // 画像にアニメーションを設定
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setBackground(anim);
                    // アニメーション開始
                    anim.start();
                } catch (JSONException e) { //JSONObject等例外発生時
                    Log.e("error", e.toString());
                    e.printStackTrace();
                }

            }*/
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
