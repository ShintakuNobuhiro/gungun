package com.example.shintaku.test;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//課題大項目選択画面
public class SettingActivity extends AppCompatActivity {
    final String str[] = new String[4];
    final int[] mission_id = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        String nfcId = NfcActivity.nfcIdInfo;
        final SharedPreferences sp = getSharedPreferences("data",MODE_PRIVATE);
        final String password = sp.getString(nfcId, "");
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
        final String finalNfcId = nfcId;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, LevelActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                // POST通信を実行（AsyncTaskによる非同期処理を使うバージョン）
                ASyncPost task = new ASyncPost(SettingActivity.this,"https://",
                    // タスク完了時に呼ばれるUIのハンドラ
                    new HttpPostHandler() {
                        @Override
                        public void onPostCompleted(String response) {
                        }

                        @Override
                        public void onPostFailed(String response) {
                            Toast.makeText(getApplicationContext(), "エラーが発生しました。", Toast.LENGTH_LONG).show();
                        }
                    });
                    task.addPostParam("post_1", finalNfcId);
                    task.addPostParam("post_2", password);
                    for(int i=0;i<2;i++) {
                        if(mission_id[i]!=0)
                            task.addPostParam("mission_ids[]", String.valueOf(mission_id[i]));
                    }
                    // タスクを開始
                    task.execute();

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
