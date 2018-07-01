package com.epicgames.ue4.runnables;

import android.util.Log;

import com.epicgames.ue4.ThreadManager;
import com.epicgames.ue4.Touch;
import com.google.android.gms.wearable.Channel;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramSocket;

import static com.epicgames.ue4.WearListenerService.TAG;

public final class ReceiveDataRunnable implements Runnable {
    private static final byte COMTP_SENSOR_DATA = 1;
    private static final byte COMTP_SHAKING_STARTED = 2;
    private static final byte COMTP_SHAKING_STOPPED = 3;

    private final DataInputStream dataInputStream;
    private final DatagramSocket datagramSocket;

    public ReceiveDataRunnable(final Channel.GetInputStreamResult r, final DatagramSocket datagramSocket) {
        Log.d(TAG, "Obtaining InputStream");
        this.dataInputStream = new DataInputStream(r.getInputStream());
        Log.d(TAG, "DataInputStream obtained");
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        Log.d(TAG, "Channel opened");
        try {
            while (true) {
                final byte request = dataInputStream.readByte();
                Log.d(TAG, "Request received! -> " + request);
                if (request == COMTP_SENSOR_DATA) {
                    final float zRotation = this.dataInputStream.readFloat();
                    final float vectorX = this.dataInputStream.readFloat();
                    final float vectorY = this.dataInputStream.readFloat();
                    final float vectorZ = this.dataInputStream.readFloat();

                    final float xAcc = this.dataInputStream.readFloat();
                    final float yAcc = this.dataInputStream.readFloat();
                    final float zAcc = this.dataInputStream.readFloat();

                    final float xTouch = this.dataInputStream.readFloat();
                    final float yTouch = this.dataInputStream.readFloat();
                    final byte stateTouch = this.dataInputStream.readByte();

                    Log.d(TAG, "==============> rotation(" + zRotation + ", " + vectorX + ", " + vectorY + ", " + vectorZ + ')');
                    Log.d(TAG, "==============> acceleration(" + xAcc + ", " + yAcc + ", " + zAcc + ')');
                    if (xTouch < 0 && yTouch < 0) {
                        Log.d(TAG, "==============> No touch......");
                    } else {
                        Log.d(TAG, "==============> touch(" + xTouch + ", " + yTouch + ", " + stateTouch + ", " + ')');
                    }

                    ThreadManager.execute(new SendSensorDataRunnable(datagramSocket, new float[] {zRotation, vectorX, vectorY, vectorZ}, new float[] {xAcc, yAcc, zAcc}, new Touch(xTouch, yTouch, stateTouch)));
                } else if (request == COMTP_SHAKING_STARTED) {
                    ThreadManager.execute(new SendShakingStartedRunnable(datagramSocket));
                } else if (request == COMTP_SHAKING_STOPPED) {
                    ThreadManager.execute(new SendShakingStoppedRunnable(datagramSocket));
                } else {
                    Log.e(TAG, "Unknown request received");
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
