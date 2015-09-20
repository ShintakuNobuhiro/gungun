package com.example.shintaku.test;
//thanks! http://qiita.com/otoyo/items/70cfa89bfa93ffdf6c9a

public interface AsyncCallback {

    void onPreExecute(); //非同期前処理
    void onPostExecute(String result); //非同期完了時処理
    void onProgressUpdate(int progress); //非同期中処理
    void onCancelled(); //非同期キャンセル時処理

}