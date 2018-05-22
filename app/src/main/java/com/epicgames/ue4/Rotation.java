package com.epicgames.ue4;

public final class Rotation {
    public final float vectorX;
    public final float vectorY;
    public final float vectorZ;
    public final float rotX;
    public final float rotY;
    public final float rotZ;
    public final long timestamp;

    public Rotation(final float vectorX, final float vectorY, final float vectorZ, final float rotX, final float rotY, final float rotZ, long timestamp) {
        this.vectorX = vectorX;
        this.vectorY = vectorY;
        this.vectorZ = vectorZ;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.timestamp = timestamp;
    }
}
