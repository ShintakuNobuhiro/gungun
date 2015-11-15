package com.example.shintaku.test;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
    final int[] mission_id = {-1,-1};
    String nfcId,password,URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        final SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        nfcId = sp.getString("nfc_id","");
        password = sp.getString(nfcId, "");
        URL = sp.getString("URL","");
        Log.d("nfc", nfcId + "," + password);
        new Mission().execute();

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
        //戻る
        btn = (Button) this.findViewById(R.id.button6);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, LevelActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
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
                if(mission_id[0] != -1)
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
                for(int i=0;i<2;i++) {
                    if(mission_id[i] != -1)
                        tmp.put(mission_id[i]);
                }
                jobj.put("mission_ids", tmp);
                Log.d("test", String.valueOf(jobj));
            } catch (JSONException e) {
                e.printStackTrace();
            }
             return postJsonObject(URL + "/api/assigns.json", jobj);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            Log.d("start", String.valueOf(result));
        }
    }
    class Mission extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jobj = new JSONObject();
            try {
                jobj.put("password", password);
                Log.d("test", String.valueOf(jobj));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return postJsonObject(URL + "/api/users/" + nfcId + ".json", jobj);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            Log.d("start", String.valueOf(result));
            try {
                Button btn1 = (Button) SettingActivity.this.findViewById(R.id.checkButton); //健康
                Button btn2 = (Button) SettingActivity.this.findViewById(R.id.checkButton2); //お友達あいさつ
                String description[] = new String[4];

                //パース準備
                JSONArray missions = null;
                JSONObject mission[] = new JSONObject[4];
                if (result != null) {
                    missions = result.getJSONArray("assigns");
                }

                //mission分解、説明の配列化
                JSONObject category[] = new JSONObject[mission.length];
                String category_name[] = new String[category.length];
                int missionIdTmp[] = new int[mission.length];
                String descriptionTmp[] = new String[mission.length];
                if (missions != null) {
                    for (int i = 0; i < missions.length(); i++) {
                        mission[i] = missions.getJSONObject(i);
                        category[i] = mission[i].getJSONObject("category");
                        category_name[i] = category[i].getString("name");
                        Log.d("name", category_name[i]);
                        missionIdTmp[i] = mission[i].getInt("mission_id");
                        descriptionTmp[i] = mission[i].getString("description");
                    }
                    for (int i = 0; i < 2; i++) {
                        if (category_name[i] != null) {
                            Log.d("name", category_name[i]);
                            if (category_name[i].equals("けんこう")) {
                                mission_id[0] = missionIdTmp[i];
                                description[0] = descriptionTmp[i];
                            }
                            else if (category_name[i].equals("お友だちとあいさつ")) {
                                mission_id[1] = missionIdTmp[i];
                                description[1] = descriptionTmp[i];
                            }
                        }
                    }
                    if(description[0] != null) {
                        btn1.setTextColor(Color.argb(255,255,255,255));
                        btn1.setText(description[0]);
                    }
                    if(description[1] != null) {
                        btn1.setTextColor(Color.argb(255,255,255,255));
                        btn2.setText(description[1]);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                if (intent.getStringExtra(getString(R.string.genre1)) != null) {
                    str[0] = intent.getStringExtra(getString(R.string.genre1));
                    btn = (Button) findViewById(R.id.checkButton);
                    mission_id[0] = Integer.parseInt(intent.getStringExtra("id"));
                    Log.d(getString(R.string.genre1),str[0]+","+mission_id[0]);
                    btn.setTextColor(Color.argb(255, 255, 255, 255));
                    btn.setText(str[0]);
                }
                else if (intent.getStringExtra(getString(R.string.genre2)) != null) {
                    str[1] = intent.getStringExtra(getString(R.string.genre2));
                    btn = (Button) findViewById(R.id.checkButton2);
                    btn.setTextColor(Color.argb(255,255,255,255));
                    btn.setText(str[1]);
                    mission_id[1] = Integer.parseInt(intent.getStringExtra("id"));
                    Log.d(getString(R.string.genre2),str[1]+","+mission_id[1]);
                } else {
                    Log.e("Genre","error");
                }
            } else {
                Log.e("error","requestCode Unmatched");
            }
        }
    }
}
