package com.example.shintaku.test;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;

public class ASyncPost extends AsyncTask<String, Integer, Integer> {

    @Override
    protected Integer doInBackground(String... contents) {
        String url="http://localhost:8888/test";
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add( new BasicNameValuePair("user_id", contents[0]));
        params.add( new BasicNameValuePair("mission_ids[]", contents[1]));
        params.add( new BasicNameValuePair("mission_ids[]", contents[2]));

        HttpResponse res = null;

        try {
            post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
            Log.d("post", String.valueOf(params));
            res = httpClient.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(res != null) {
            Log.d("test", String.valueOf(res.getStatusLine().getStatusCode()));
            return res.getStatusLine().getStatusCode();
        } else {
            return -1;
        }
    }
}