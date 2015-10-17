package com.example.shintaku.test;

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
import java.util.Arrays;

//チェック画面
public class MissionActivity extends AppCompatActivity {

    boolean clear[] = new boolean[4]; //達成状況の保存
    final int missionId[] = {-1,-1,-1,-1};
    String nfcId,password;
    final int id[] = {R.id.checkButton, R.id.checkButton2, R.id.checkButton3, R.id.checkButton4};
    final String description[] = new String[4];
    final Button chkBtn[] = new Button[id.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);

        nfcId = NfcActivity.nfcIdInfo;
        final SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        password = sp.getString(nfcId, "");
        Log.d("nfc", nfcId + "," + password);

        final int[] chkOn = new int[]{getResources().getColor(R.color.blue), getResources().getColor(R.color.green), getResources().getColor(R.color.orange), getResources().getColor(R.color.red)};
        final int[] chkOff = new int[]{getResources().getColor(R.color.lightblue), getResources().getColor(R.color.lightgreen), getResources().getColor(R.color.lightorange), getResources().getColor(R.color.lightred)};

        for (int i = 0; i < id.length; i++) {
            chkBtn[i] = (Button) findViewById(id[i]);
            clear[i] = false;
            chkBtn[i].setBackgroundColor(chkOff[i]);
            chkBtn[i].setTextColor(getResources().getColor(R.color.black));
        }
        new Loader().execute();


        for (int i = 0; i < id.length; i++) {
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
                    Log.d(String.valueOf(finalI), String.valueOf(clear[finalI]));
                }
            });
        }
        Button btnNext = (Button) this.findViewById(R.id.button2);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MissionActivity.this, LevelActivity.class);
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
                    String tmp[] = new String[4];
                    for (int i = 0; i < tmp.length; i++) {
                        tmp[i] = null;
                        if (clear[i] = true && missionId[i] != -1)
                            tmp[i] = String.valueOf(missionId[i]);
                    }
                    jobj.put("mission_ids", Arrays.deepToString(tmp));
                    Log.d("test", String.valueOf(jobj));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return postJsonObject("https://railstutorial-ukyankyan-1.c9.io/images.json", jobj);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                super.onPostExecute(result);
                Log.d("start", String.valueOf(result));
                try {
                    //パース準備
                    JSONArray missions = null;
                    JSONObject mission[] = new JSONObject[4];
                    if(result!=null) {
                        missions = result.getJSONArray("assigns");

                    }

                    //mission分解、説明の配列化
                    if(missions!=null) {
                        for (int i = 0; i < missions.length(); i++) {
                            mission[i] = missions.getJSONObject(i);
                            missionId[i] = mission[i].getInt("mission_id");
                            description[i] = mission[i].getString("description");
                            chkBtn[i].setText(description[i]);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("error", e.toString());
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
