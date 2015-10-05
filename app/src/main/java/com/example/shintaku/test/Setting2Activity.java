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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//課題小項目選択
public class Setting2Activity extends AppCompatActivity {
    int level = 1; //現在の閲覧レベル
    int lvlMin = 1; //最低
    int lvlMax = 4; //最高
    final int firstPage = 1; //初期ページ
    int page = firstPage;
    ArrayList<String> description = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting2);

        //大項目名表示
        Settings tr = (Settings) getIntent().getSerializableExtra("genre");//大項目名のインテント間引き継ぎ
        TextView a = (TextView)this.findViewById(R.id.textView);
        final int category = Integer.parseInt(tr.getSetting(Settings.subject.TEXT));
        final String genre[]= {getString(R.string.genre1), getString(R.string.genre2), getString(R.string.genre3), getString(R.string.genre4)};

        if(category >= 0 || category <= 3) {
            a.setText(genre[category]);
        } else {
            a.setText("error");
        }

        final int id[] = {R.id.mission1, R.id.mission2, R.id.mission3, R.id.mission4, R.id.mission5};
        jsonSetText(category,level,page);

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

        final TextView pageTxt = (TextView) findViewById(R.id.page);

        //レベル上げ
        Button nextLevel = (Button) findViewById(R.id.nextLevel);
        final TextView levelTxt = (TextView) findViewById(R.id.level);
        nextLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (level < lvlMax) {
                    level++;
                } else {
                    level = lvlMin;
                }
                Log.d("lv", String.valueOf(level));
                levelTxt.setText("レベル" + String.valueOf(level));
                page = firstPage;
                pageTxt.setText("ページ" + String.valueOf(page));
                jsonSetText(category, level, page);
            }
        });

        //レベル下げ
        Button prevLevel = (Button) findViewById(R.id.prevLevel);
        prevLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(level>lvlMin) {
                    level--;
                } else {
                    level = lvlMax;
                }
                Log.d("lv", String.valueOf(level));
                levelTxt.setText("レベル" + String.valueOf(level));
                page = firstPage;
                pageTxt.setText("ページ"+ String.valueOf(page));
                jsonSetText(category, level, page);
            }
        });

        //ページ送り
        Button nextPage = (Button) findViewById(R.id.nextPage);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page++;
                Log.d("page", String.valueOf(page));
                pageTxt.setText("ページ" + String.valueOf(page));
                jsonSetText(category,level,page);
            }
        });

        //ページ戻し
        Button prevPage = (Button) findViewById(R.id.prevPage);
        prevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(page > 1) {
                    page--;
                }
                Log.d("page", String.valueOf(page));
                pageTxt.setText("ページ" + String.valueOf(page));
                jsonSetText(category,level,page);
            }
        });

        final Button mission[] = new Button[id.length];
        for(int i=0; i<id.length; i++) {
            mission[i] = (Button) findViewById(id[i]);
            final int finalI = i;
            mission[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Setting2Activity.this, SettingActivity.class);
                    String text = mission[finalI].getText().toString();

                    intent.putExtra(String.valueOf(genre[category]), text);
                    Log.d("text", String.valueOf(genre[category])+text);
                    // 返却したい結果ステータスをセットする
                    setResult(Activity.RESULT_OK, intent);
                    // アクティビティを終了させる
                    finish();
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

    public void jsonSetText(int cat, final int lvl, final int p) {
        ASyncGet asyncGet = new ASyncGet(new AsyncCallback() {
            public void onPreExecute() {
                for(int i = description.size()-1; i>=0; i--) {
                    description.remove(i);
                }
            }
            public void onProgressUpdate(int progress) {
            }
            public void onPostExecute(final String result) {
                Log.d("start", result);
                try {
                    //パース準備
                    JSONObject json = new JSONObject(result);
                    JSONObject missionObject = json.getJSONObject("levels");
                    Log.d("test", String.valueOf(missionObject));
                    JSONArray missions = missionObject.getJSONArray("1");

                    //mission分解、説明の配列化
                    for (int i = 0; i < missions.length(); i++) {
                        JSONObject mission = missions.getJSONObject(i);
                        description.add(mission.getString("description"));
                        //Log.d("description",i+","+description.get(i));
                    }

                    //missionの表示
                    final int id[] = {R.id.mission1, R.id.mission2, R.id.mission3, R.id.mission4, R.id.mission5};
                    final Button button[] =new Button[id.length];
                    for(int i = 0; i < id.length; i++) {
                        button[i] = (Button) findViewById(id[i]);
                        int tmp = (page-1)*id.length + i;
                        if(tmp < description.size() && tmp >= 0) {
                            //Log.d("tmp", String.valueOf(tmp)+ "," + String.valueOf(i));
                            button[i].setText(description.get(tmp));
                        } else {
                            button[i].setText("empty");
                        }

                    }
                } catch (JSONException e) {
                    Log.e("error",e.toString());
                    e.printStackTrace();
                }
            }
            public void onCancelled() {
            }
        });
        String URL = null;
        if(cat == 0 || cat ==1) {
            URL = "https://railstutorial-ukyankyan-1.c9.io/category/"+cat+".json";
        } else {
            Log.e("error","error");
        }
        asyncGet.execute(URL);
    }
}

