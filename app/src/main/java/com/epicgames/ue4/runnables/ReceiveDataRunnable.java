package com.epicgames.ue4.runnables;

import android.util.Log;

import com.epicgames.ue4.Acceleration;
import com.epicgames.ue4.Rotation;
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
                    final float vectorX = this.dataInputStream.readFloat();
                    final float vectorY = this.dataInputStream.readFloat();
                    final float vectorZ = this.dataInputStream.readFloat();
                    final float xRot = this.dataInputStream.readFloat();
                    final float yRot = this.dataInputStream.readFloat();
                    final float zRot = this.dataInputStream.readFloat();
                    final long dateRot = this.dataInputStream.readLong();

                    final float xAcc = this.dataInputStream.readFloat();
                    final float yAcc = this.dataInputStream.readFloat();
                    final float zAcc = this.dataInputStream.readFloat();
                    final long dateAcc = this.dataInputStream.readLong();

                    final float xTouch = this.dataInputStream.readFloat();
                    final float yTouch = this.dataInputStream.readFloat();
                    final byte stateTouch = this.dataInputStream.readByte();
                    final long dateTouch = this.dataInputStream.readLong();

                    Log.d(TAG, "==============> vector(" + vectorX + ", " + vectorY + ", " + vectorZ + ')');
                    Log.d(TAG, "==============> rotation(" + xRot + ", " + yRot + ", " + zRot + ", " + dateRot + ')');
                    Log.d(TAG, "==============> acceleration(" + xAcc + ", " + yAcc + ", " + zAcc + ", " + dateAcc + ')');
                    if (xTouch < 0 && yTouch < 0) {
                        Log.d(TAG, "==============> No touch......");
                    } else {
                        Log.d(TAG, "==============> touch(" + xTouch + ", " + yTouch + ", " + stateTouch + ", " + dateTouch + ')');
                    }

                    ThreadManager.execute(new SendSensorDataRunnable(datagramSocket, new Rotation(vectorX, vectorY, vectorZ, xRot, yRot, zRot, dateRot), new Acceleration(xAcc, yAcc, zAcc, dateAcc), new Touch(xTouch, yTouch, stateTouch, dateTouch)));
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
