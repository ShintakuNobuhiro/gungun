package com.example.shintaku.test;

import android.app.Activity;
import android.app.ProgressDialog;
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
import java.util.ArrayList;

//課題小項目選択
public class Setting2Activity extends AppCompatActivity {

    int level = 1; //現在の閲覧レベル
    int lvlMin = 1; //最低
    int lvlMax = 4; //最高
    int maxPage = 1;
    final int firstPage = 1; //初期ページ
    int page = firstPage;
    int category = -1;
    String genre[];

    String nfcId,password;
    ArrayList<Integer> mission_id = new ArrayList<>();
    ArrayList<String> description = new ArrayList<>();

    public Setting2Activity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting2);
        final SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        nfcId = sp.getString("nfc_id", "");
        password = sp.getString(nfcId, "");
        genre = new String[]{getString(R.string.genre1), getString(R.string.genre2), getString(R.string.genre3), getString(R.string.genre4)};

        //大項目名表示
        Settings tr = (Settings) getIntent().getSerializableExtra("genre");//大項目名のインテント間引き継ぎ
        TextView a = (TextView) this.findViewById(R.id.textView);
        category = Integer.parseInt(tr.getSetting(Settings.subject.TEXT));


        if (category >= 0 || category <= 3) {
            a.setText(genre[category]);
        } else {
            a.setText("error");
        }

        final int id[] = {R.id.mission1, R.id.mission2, R.id.mission3, R.id.mission4, R.id.mission5};
        new Loader().execute();

        //戻るボタン
        Button btn = (Button) findViewById(R.id.button5);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Setting2Activity.this, SettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });


        //レベル上げ
        Button nextLevel = (Button) findViewById(R.id.nextLevel);
        nextLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (level < lvlMax) {
                    level++;
                } else {
                    level = lvlMin;
                }
                Log.d("lv", String.valueOf(level));
                page = firstPage;
                new Loader().execute();
            }
        });

        //レベル下げ
        Button prevLevel = (Button) findViewById(R.id.prevLevel);
        prevLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (level > lvlMin) {
                    level--;
                } else {
                    level = lvlMax;
                }
                Log.d("lv", String.valueOf(level));
                page = firstPage;
                new Loader().execute();
            }
        });

        //ページ送り
        Button nextPage = (Button) findViewById(R.id.nextPage);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (page < maxPage) {
                    page++;
                } else {
                    page = firstPage;
                }
                Log.d("page", String.valueOf(page));
                mission_id.clear();
                new Loader().execute();
            }
        });

        //ページ戻し
        Button prevPage = (Button) findViewById(R.id.prevPage);
        prevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page > 1) {
                    page--;
                } else {
                    page = maxPage;
                }
                Log.d("page", String.valueOf(page));
                new Loader().execute();
            }
        });
        Log.d("test", String.valueOf(mission_id.size()));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting2, menu);
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

    class Loader extends AsyncTask<Void, Void, JSONObject> {
        ProgressDialog progressDialog = new ProgressDialog(Setting2Activity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.show();
            mission_id.clear();
            description.clear();
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
            int tmp = category + 1;
            String URL = "https://gungun.herokuapp.com/api/categories/" + tmp + ".json";
            return postJsonObject(URL, jobj);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            Log.d("result", String.valueOf(result));

            //パース準備
            try {
                String name = result.getString("name");
                JSONArray levels = result.getJSONArray("levels");
                //mission分解、説明の配列化
                JSONObject lvl = levels.getJSONObject(level - 1);
                Log.d("jsonlvl", String.valueOf(lvl));
                JSONArray missions = lvl.getJSONArray("missions");
                for (int i = 0; i < missions.length(); i++) {
                    JSONObject mission = missions.getJSONObject(i);
                    mission_id.add(mission.getInt("id"));
                    description.add(mission.getString("description"));
                    //Log.d("description",i+","+description.get(i));
                }
                if (mission_id.size() % 5 == 0)
                    maxPage = mission_id.size() / 5;
                else
                    maxPage = mission_id.size() / 5 + 1;
                final TextView pageTxt = (TextView) findViewById(R.id.page);
                final TextView levelTxt = (TextView) findViewById(R.id.level);
                levelTxt.setText("レベル\n" + String.valueOf(level) + "/" + String.valueOf(lvlMax));
                pageTxt.setText("ページ\n" + String.valueOf(page) + "/" + String.valueOf(maxPage));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //missionの表示
            final int id[] = {R.id.mission1, R.id.mission2, R.id.mission3, R.id.mission4, R.id.mission5};
            final Button button[] = new Button[id.length];
            for (int i = 0; i < id.length; i++) {
                button[i] = (Button) findViewById(id[i]);
                int tmp = (page - 1) * id.length + i;
                if (description.size() > tmp && tmp >= 0) {
                    Log.d("tmp", String.valueOf(tmp) + "," + String.valueOf(description.get(tmp)));
                    Log.d("page", String.valueOf(level));
                    button[i].setText(String.valueOf(description.get(tmp)));
                } else {
                    button[i].setText(getText(R.string.empty));
                }
            }

            progressDialog.dismiss();

            final Button mission[] = new Button[id.length];
            for (int i = 0; i < id.length; i++) {

                mission[i] = (Button) findViewById(id[i]);
                final int index = id.length * (page - 1) + i;
                final int finalI = i;
                mission[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Setting2Activity.this, SettingActivity.class);
                        String text = mission[finalI].getText().toString();
                        if (index < mission_id.size()) {
                            String id = String.valueOf(mission_id.get(index));
                            Log.d("id", id);
                            intent.putExtra(String.valueOf(genre[category]), text);
                            intent.putExtra("id", id);
                            Log.d("text", String.valueOf(genre[category]) + text);
                            // 返却したい結果ステータスをセットする
                            setResult(Activity.RESULT_OK, intent);
                            // アクティビティを終了させる
                            finish();
                        }
                    }
                });
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

