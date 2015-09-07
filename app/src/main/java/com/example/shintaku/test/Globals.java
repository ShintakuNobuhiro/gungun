package com.example.shintaku.test;

import java.io.Serializable;

public class Globals implements Serializable {
    String health = "健康";
    String harmony = "協調";
    String desire = "やる気";
    String self = "自立";

    enum subject{
        HEALTH,HARMONY,DESIRE,SELF
    }

    public Globals(String hea, String har, String des, String sel) {
        health = hea;
        harmony = har;
        desire = des;
        self = sel;
    }

    public String getGlobal(subject sub) {
        switch (sub) {
            case HEALTH: return health;
            case HARMONY: return harmony;
            case DESIRE: return desire;
            case SELF: return  self;
            default: return "error";
        }
    }
}
