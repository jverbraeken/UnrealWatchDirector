package com.epicgames.ue4.runnables;

import com.epicgames.ue4.WearListenerService;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

import static com.epicgames.ue4.WearListenerService.INET_ADDRESS;
import static com.epicgames.ue4.WearListenerService.PORT;

public final class SendShakingStartedRunnable implements Runnable {
    private final DatagramSocket datagramSocket;

    SendShakingStartedRunnable(final DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             final DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
            dataOutputStream.writeByte(WearListenerService.COMTP_SHAKING_STARTED);

            final byte[] byteArray = byteArrayOutputStream.toByteArray();
            final byte[] bytes = ByteBuffer.allocate(byteArray.length).put(byteArray).array();
            datagramSocket.send(new DatagramPacket(bytes, byteArray.length, INET_ADDRESS, PORT));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
