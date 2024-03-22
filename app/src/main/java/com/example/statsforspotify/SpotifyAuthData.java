package com.example.statsforspotify;

import java.util.ArrayList;
import java.util.HashMap;

public class SpotifyAuthData {

    private static HashMap<String, String> data;

    private SpotifyAuthData() {
        /* Structure of the hashmap
           {
                "token": // spotify auth token from login w spotify
           }
         */
    }

    public static HashMap<String, String> getInstance() {
        if (data == null) {
            data = new HashMap<>();
        }
        return data;
    }
}
