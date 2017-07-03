package com.eightyeightysix.shourya.almondclient.data;

/*
 * Created by shourya on 20/6/17.
 */

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class BroadCast {

    public String bId;
    public String author;
    public String title;
    public String body;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public BroadCast() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public BroadCast(String bId, String author, String title, String body) {
        this.bId = bId;
        this.author = author;
        this.title = title;
        this.body = body;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("bId", bId);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("starCount", starCount);
        result.put("stars", stars);

        return result;
    }

}
