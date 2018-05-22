package com.epicgames.ue4;

public class Acceleration {
    public float x;
    public float y;
    public float z;
    public final long timestamp;

    public Acceleration(final float x, final float y, final float z, final long timestamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }
}
