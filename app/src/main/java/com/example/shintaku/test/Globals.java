package com.example.shintaku.test;

import android.app.Application;
import android.widget.Toast;

public class Globals extends Application {

    boolean mIsBound;
    boolean a;
    boolean b;
    boolean c;
    boolean d;
    boolean connected;
    int length;
    int[] buffer;
    byte[] data;

    public void init(){
        mIsBound = false;
        a = false;
        b = false;
        c = false;
        d = false;
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}