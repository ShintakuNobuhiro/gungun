package com.example.shintaku.test.com.example.shintaku.test.api;

import retrofit.ResponseCallback;
import retrofit.http.Field;
import retrofit.http.POST;

public interface JsonAPI {
    @POST("/comments.json")
    void post(@Field("comment[body]") String i,
                     ResponseCallback callback);
}
