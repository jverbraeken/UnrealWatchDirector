package com.epicgames.ue4;

class Acceleration {
    float x;
    float y;
    float z;
    final long timestamp;

    Acceleration(final float x, final float y, final float z, final long timestamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }
}
