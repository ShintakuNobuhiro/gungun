package com.example.shintaku.test;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class SugorokuActivity extends AppCompatActivity { //ローマ字でダサいが適した英語が無いらしい

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sugoroku);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        frameAnimationTest(imageView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sugoroku, menu);
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

    // Frameアニメーションのテスト
    void frameAnimationTest(ImageView img){
        AnimationDrawable anim = new AnimationDrawable();

        // 画像の読み込み //
        Drawable frame1 = getResources().getDrawable( R.drawable.tshin_1);
        Drawable frame2 = getResources().getDrawable( R.drawable.tshin_2);
        Drawable frame3 = getResources().getDrawable( R.drawable.tshin_3);
        Drawable frame4 = getResources().getDrawable( R.drawable.tshin_4);
        Drawable frame5 = getResources().getDrawable( R.drawable.tshin_5);

        // 画像をアニメーションのコマとして追加していく
        anim.addFrame( frame1,  300 );
        anim.addFrame( frame2,  300 );
        anim.addFrame( frame3,  300 );
        anim.addFrame( frame4,  300 );
        anim.addFrame( frame5,  300 );


        // 繰り返し設定
        anim.setOneShot(true);

        // 画像にアニメーションを設定
        img.setBackground(anim);

        // アニメーション開始
        anim.start();
    }
}
