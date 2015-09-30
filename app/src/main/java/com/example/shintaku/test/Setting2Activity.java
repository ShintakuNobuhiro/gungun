package com.example.shintaku.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//課題小項目選択
public class Setting2Activity extends AppCompatActivity {
    int level = 1; //現在の閲覧レベル
    int lvlMin = 1; //最低
    int lvlMax = 4; //最高
    final String description[] = new String[5];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting2);

        //大項目名表示
        Settings tr = (Settings) getIntent().getSerializableExtra("test_result");//大項目名のインテント間引き継ぎ
        TextView a = (TextView)this.findViewById(R.id.textView);
        final int category = Integer.parseInt(tr.getSetting(Settings.subject.TEXT));

        if(category >= 1 || category <= 4) {
            final int id []= {R.string.genre1, R.string.genre2, R.string.genre3, R.string.genre4};
            a.setText(id[category]);
        } else {
            a.setText("error");
        }

        final int id[] = {R.id.mission1, R.id.mission2, R.id.mission3, R.id.mission4, R.id.mission5};
        jsonSetText(category,level,0);

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
        Button next = (Button) findViewById(R.id.next);
        final TextView levelTxt = (TextView) findViewById(R.id.level);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (level < lvlMax) {
                    level++;
                } else {
                    level = lvlMin;
                }
                Log.d("lv", String.valueOf(level));
                levelTxt.setText("レベル" + String.valueOf(level));
                jsonSetText(category,level,0);
            }
        });

        //レベル下げ
        Button prev = (Button) findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(level>lvlMin) {
                    level--;
                } else {
                    level = lvlMax;
                }
                Log.d("lv", String.valueOf(level));
                levelTxt.setText("レベル" + String.valueOf(level));
                jsonSetText(category,level,0);
            }
        });

        Button mission[] = new Button[id.length];
        for(int i=0; i<id.length; i++) {
            mission[i] = (Button) findViewById(id[i]);
            mission[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Setting2Activity.this, SettingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });
        }
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
    public String URI(int cat,int lvl){
        String tmp = null;
        if(cat == 1) {
            tmp = "https://railstutorial-ukyankyan-1.c9.io/missions/health/"+lvl+".json";
        } else if (cat == 2) {
            tmp = "https://railstutorial-ukyankyan-1.c9.io/missions/friend/"+lvl+".json";
        } else {
            Log.d("error","error");
        }
        return tmp;
    }
    public void jsonSetText(int cat,int lvl, int i) {
        ASyncGet asyncGet = new ASyncGet(new AsyncCallback() {
            public void onPreExecute() {
            }
            public void onProgressUpdate(int progress) {
            }
            public void onPostExecute(final String result) {
                Log.d("start", result);
                try {
                    //パース準備
                    JSONObject json = new JSONObject(result);
                    JSONArray missions = json.getJSONArray("missions");

                    //mission分解、説明の配列化
                    for (int i = 0; i < missions.length(); i++) {
                        JSONObject mission = missions.getJSONObject(i);
                        description[i] = mission.getString("description");
                        Log.d("description",i+","+description[i]);
                    }

                    //missionの表示
                    final int id[] = {R.id.mission1, R.id.mission2, R.id.mission3, R.id.mission4, R.id.mission5};
                    final Button button[] =new Button[id.length];
                    for(int i = 0; i < id.length; i++) {
                        button[i] = (Button) findViewById(id[i]);
                        button[i].setText(description[i]);
                    }
                } catch (JSONException e) {
                    Log.e("error",e.toString());
                    e.printStackTrace();
                }
            }
            public void onCancelled() {
            }
        });
        asyncGet.execute(URI(cat,lvl));
    }
}

