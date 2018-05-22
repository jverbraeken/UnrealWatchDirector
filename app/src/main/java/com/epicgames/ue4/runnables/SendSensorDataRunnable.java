package com.epicgames.ue4.runnables;

import android.util.Log;

import com.epicgames.ue4.Acceleration;
import com.epicgames.ue4.Rotation;
import com.epicgames.ue4.Touch;
import com.epicgames.ue4.WearListenerService;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

import static com.epicgames.ue4.WearListenerService.INET_ADDRESS;
import static com.epicgames.ue4.WearListenerService.PORT;
import static com.epicgames.ue4.WearListenerService.TAG;

public final class SendSensorDataRunnable implements Runnable {
    private final DatagramSocket datagramSocket;
    private final Rotation rotation;
    private final Acceleration acceleration;
    private final Touch touch;

    SendSensorDataRunnable(final DatagramSocket datagramSocket, final Rotation rotation, final Acceleration acceleration, final Touch touch) {
        this.datagramSocket = datagramSocket;
        this.rotation = rotation;
        this.acceleration = acceleration;
        this.touch = touch;
    }

    @Override
    public void run() {
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
            dataOutputStream.writeByte(WearListenerService.COMTP_SENSOR_DATA);
            dataOutputStream.writeFloat(rotation.vectorX);
            dataOutputStream.writeFloat(rotation.vectorY);
            dataOutputStream.writeFloat(rotation.vectorZ);
            dataOutputStream.writeFloat(rotation.rotX);
            dataOutputStream.writeFloat(rotation.rotY);
            dataOutputStream.writeFloat(rotation.rotZ);
            dataOutputStream.writeLong(this.rotation.timestamp);

            dataOutputStream.writeFloat(this.acceleration.x);
            dataOutputStream.writeFloat(this.acceleration.y);
            dataOutputStream.writeFloat(this.acceleration.z);
            dataOutputStream.writeLong(this.acceleration.timestamp);

            if (this.touch.equals(WearListenerService.NO_TOUCH)) {
                dataOutputStream.writeFloat(-1);
                dataOutputStream.writeFloat(-1);
                dataOutputStream.writeByte(2);
            } else {
                dataOutputStream.writeFloat(this.touch.x);
                dataOutputStream.writeFloat(this.touch.y);
                dataOutputStream.writeByte(this.touch.state);
            }
            dataOutputStream.writeLong(this.touch.timestamp);

            final byte[] byteArray = byteArrayOutputStream.toByteArray();
            final byte[] bytes = ByteBuffer.allocate(byteArray.length).put(byteArray).array();
            datagramSocket.send(new DatagramPacket(bytes, byteArray.length, INET_ADDRESS, PORT));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
