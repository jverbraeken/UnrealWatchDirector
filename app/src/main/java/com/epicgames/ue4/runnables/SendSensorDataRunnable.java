package com.epicgames.ue4.runnables;

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

public final class SendSensorDataRunnable implements Runnable {
    private final DatagramSocket datagramSocket;
    private final float[] rotation;
    private final float[] acceleration;
    private final Touch touch;

    SendSensorDataRunnable(final DatagramSocket datagramSocket, final float[] rotation, final float[] acceleration, final Touch touch) {
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
            dataOutputStream.writeFloat(rotation[0]);
            dataOutputStream.writeFloat(rotation[1]);
            dataOutputStream.writeFloat(rotation[2]);
            dataOutputStream.writeFloat(rotation[3]);

            dataOutputStream.writeFloat(acceleration[0]);
            dataOutputStream.writeFloat(acceleration[1]);
            dataOutputStream.writeFloat(acceleration[2]);

            if (this.touch.equals(WearListenerService.NO_TOUCH)) {
                dataOutputStream.writeFloat(-1);
                dataOutputStream.writeFloat(-1);
                dataOutputStream.writeByte(2);
            } else {
                dataOutputStream.writeFloat(this.touch.x);
                dataOutputStream.writeFloat(this.touch.y);
                dataOutputStream.writeByte(this.touch.state);
            }

            final byte[] byteArray = byteArrayOutputStream.toByteArray();
            final byte[] bytes = ByteBuffer.allocate(byteArray.length).put(byteArray).array();
            datagramSocket.send(new DatagramPacket(bytes, byteArray.length, INET_ADDRESS, PORT));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
