package com.example.shintaku.test;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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

//課題大項目選択画面
public class SettingActivity extends AppCompatActivity {
    final String str[] = new String[4];
    final int[] mission_id = new int[2];
    String nfcId,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        final SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        nfcId = sp.getString("nfc_id","");
        password = sp.getString(nfcId, "");
        Log.d("nfc", nfcId + "," + password);


        Button btn = (Button) this.findViewById(R.id.checkButton); //健康
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = "0";
                Settings text = new Settings(txt);

                Intent intent = new Intent(SettingActivity.this, Setting2Activity.class);
                intent.putExtra("genre", text);
                // 遷移先から返却されてくる際の識別コード
                int requestCode = 1001;
                // 返却値を考慮したActivityの起動を行う
                startActivityForResult(intent, requestCode);
            }
        });

        btn = (Button) this.findViewById(R.id.checkButton2); //お友達・あいさつ
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = "1";
                Settings text = new Settings(txt);

                Intent intent = new Intent(getApplicationContext(), Setting2Activity.class);
                intent.putExtra("genre", text);
                // 遷移先から返却されてくる際の識別コード
                int requestCode = 1001;
                // 返却値を考慮したActivityの起動を行う
                startActivityForResult(intent, requestCode);
            }
        });

        //決定
        btn = (Button) this.findViewById(R.id.button3);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, LevelActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                new Loader().execute();
                startActivity(intent);
            }
        });
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
                JSONArray tmp = new JSONArray();
                tmp.put(mission_id[0]);
                tmp.put(mission_id[1]);
                jobj.put("mission_ids", tmp);
                Log.d("test", String.valueOf(jobj));
            } catch (JSONException e) {
                e.printStackTrace();
            }
             return postJsonObject("https://gungun.herokuapp.com/api/assigns.json", jobj);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            Log.d("start", String.valueOf(result));
        }
    }


    public JSONObject postJsonObject(String url, JSONObject loginJson) {
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
            json = loginJson.toString();
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
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        JSONObject json = null;
        try {
            Log.d("json", result);
            json = new JSONObject(result);
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
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
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

    public void onActivityResult( int requestCode, int resultCode, Intent intent ) {
        // startActivityForResult()の際に指定した識別コードとの比較
        if( requestCode == 1001 ){
            // 返却結果ステータスとの比較
            if( resultCode == Activity.RESULT_OK ) {
                // 返却されてきたintentから値を取り出す

                Button btn;
                if (intent.getStringExtra("健康") != null) {
                    str[0] = intent.getStringExtra("健康");
                    btn = (Button) findViewById(R.id.checkButton);
                    mission_id[0] = Integer.parseInt(intent.getStringExtra("id"));
                    Log.d("健康",str[0]+","+mission_id[0]);
                    btn.setText(str[0]);
                }
                else if (intent.getStringExtra("お友達/あいさつ") != null) {
                    str[1] = intent.getStringExtra("お友達/あいさつ");
                    btn = (Button) findViewById(R.id.checkButton2);
                    btn.setText(str[1]);
                    mission_id[1] = Integer.parseInt(intent.getStringExtra("id"));
                    Log.d("健康",str[1]+","+mission_id[1]);
                } else {
                    Log.e("Genre","error");
                }
            } else {
                Log.e("error","requestCode Unmatched");
            }
        }
    }
}
