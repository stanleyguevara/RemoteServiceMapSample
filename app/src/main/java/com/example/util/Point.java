package com.example.util;

import java.io.Serializable;

public class Point implements Serializable {

    public static final String NO_INFO = "No info yet";

    public int index;
    public double lat;
    public double lng;
    public String info;

    public Point(int index, double lat, double lng, String info) {
        this.index = index;
        this.lat = lat;
        this.lng = lng;
        this.info = info;
    }
}
