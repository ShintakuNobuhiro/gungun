package com.example.shintaku.test;

import java.io.Serializable;

public class Settings implements Serializable {
    String text = "ジャンル";

    enum subject{
        TEXT
    }

    public Settings(String txt) {
        text = txt;
    }

    public String getSetting(subject sub) {
        switch (sub) {
            case TEXT: return text;
            default: return "error";
        }
    }
}
