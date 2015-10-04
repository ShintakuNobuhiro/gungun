package com.example.shintaku.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//課題大項目選択画面
public class SettingActivity extends AppCompatActivity {
    final String str[] = new String[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        final String[] description = new String[2];


        ASyncGet asyncGet = new ASyncGet(new AsyncCallback() {
            public void onPreExecute() {
            }
            public void onProgressUpdate(int progress) {
            }
            public void onPostExecute(final String result) {
                Log.d("start",result);
                try {
                    //パース準備
                    JSONObject json = new JSONObject(result);
                    JSONArray missions = json.getJSONArray("missions");

                    //mission分解、説明の配列化
                    for (int i = 0; i < missions.length(); i++) {
                        JSONObject mission = missions.getJSONObject(i);
                        description[i] = mission.getString("description");
                    }
                } catch (JSONException e) {
                    Log.e("error",e.toString());
                    e.printStackTrace();
                }
            }
            public void onCancelled() {
            }
        });
        asyncGet.execute("https://railstutorial-ukyankyan-1.c9.io/users/1.json");

        Button btn = (Button) this.findViewById(R.id.checkButton); //健康
        final Button finalBtn = btn;
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
        final Button finalBtn1 = btn;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt="1";
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
                ASyncPost post = new ASyncPost();
                post.execute("1", str[0], str[1]);
                startActivity(intent);
            }
        });
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
                    Log.d("健康",str[0]);
                    btn = (Button) findViewById(R.id.checkButton);
                    btn.setText(str[0]);
                }
                if (intent.getStringExtra("お友達/あいさつ") != null) {
                    str[1] = intent.getStringExtra("お友達/あいさつ");
                    Log.d("お友達/あいさつ",str[1]);
                    btn = (Button) findViewById(R.id.checkButton2);
                    btn.setText(str[1]);
                } else {
                    Log.e("お友達あいさつ","error");
                }
            }
        }
    }
}
