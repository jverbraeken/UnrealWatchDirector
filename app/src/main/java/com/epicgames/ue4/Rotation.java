package com.epicgames.ue4;

final class Rotation {
    final float x;
    final float y;
    final float z;
    final long timestamp;

    Rotation(final float x, final float y, final float z, final long timestamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }
}
