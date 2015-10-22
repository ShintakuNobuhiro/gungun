package com.example.shintaku.test;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class NfcActivity extends AppCompatActivity {

    public static String nfcIdInfo = "init";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // インテントの取得
        Intent intent = getIntent();

        // ICカードの検出かチェック
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // NFC対応情報の取得
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            // タグ情報を表示用に加工する
            String tagName = "";
            for (String tech : tag.getTechList()) {
                tagName += tech + "\n";
            }

            // NFCからID情報取得
            byte[] ids = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);

            // ID情報を表示用に加工する
            StringBuilder tagId = new StringBuilder();
            for (int i=0; i<ids.length; i++) {
                tagId.append(String.format("%02x", ids[i] & 0xff));
            }

            // ID情報を画面に表示する
            nfcIdInfo =tagId.toString();
            System.out.println(nfcIdInfo);

            // メインアクテビィティを呼ぶ
            intent = new Intent();
            intent.setClassName("com.example.shintaku.test",
                    "com.example.shintaku.test.LevelActivity");
            startActivity(intent);
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}