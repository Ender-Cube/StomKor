package me.zax71.stomKor.listeners;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;

import static me.zax71.stomKor.Main.playerMapQueue;

public class RedisSub extends JedisPubSub {
    @Override
    public void onMessage(String channel, String message) {

        // Turn JSON into HashMap
        HashMap<String, String> incomingData = new Gson().fromJson(message, new TypeToken<HashMap<String, String>>() {
        }.getType());

        // Append the incoming data to our global queue List
        playerMapQueue.add(incomingData);
    }
}
