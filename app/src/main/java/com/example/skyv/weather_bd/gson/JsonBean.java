package com.example.skyv.weather_bd.gson;

import java.util.List;
/**
 * Created by skyv on 3/15/2017.
 */

public class JsonBean {
    List<Results> results;
    public List<Results> getResults() {
        return results;
    }
    public void setResults(List<Results> results) {
        this.results = results;
    }
    public static class Results {
        public Location location;
        public Now now;
        public String last_update;
        public class Location {
            String id;
            public String name;
            String country;
            String path;
            String timezone;
            String timezone_offset;
        }
        public class Now {
            public String text;
            String code;
            public String temperature;
        }
    }
}
