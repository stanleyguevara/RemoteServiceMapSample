package com.example.util;

public interface PointListener {
    void onNewPoint(Point point, long scheduled);
    void onPointUpdate(Point point);
}
