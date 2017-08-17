package com.epicgames.ue4;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WearListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String IP_ADDRESS = "192.168.178.29";
    private static final int PORT = 55056;
    private static final Touch NO_TOUCH = new Touch(-1, -1, (byte) 0);
    private static final InetAddress INET_ADDRESS;
    private static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    public static WearListenerService instance;

    static {
        InetAddress tmp = null;
        try {
            tmp = InetAddress.getByName(IP_ADDRESS);
        } catch (final UnknownHostException e) {
            e.printStackTrace();
        }
        INET_ADDRESS = tmp;
    }

    private DatagramSocket datagramSocket;
    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        Log.d("Foo", "WearListenerService created");
        instance = this;

        if (this.googleApiClient == null) {
            this.googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API)
                    .build();
        }

        if (!this.googleApiClient.isConnected()) {
            Log.d("Foo", "connecting to googleApiClient");
            this.googleApiClient.connect();
        }

        try {
            this.datagramSocket = new DatagramSocket(PORT);
            this.datagramSocket.setBroadcast(true);
        } catch (final SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (this.googleApiClient != null) {
            if (this.googleApiClient.isConnected()) {
                this.googleApiClient.disconnect();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onChannelOpened(final Channel channel) {
        Log.d("Foo", "onChannelOpened!");
        channel.getInputStream(this.googleApiClient).setResultCallback(new ResultCallback<Channel.GetInputStreamResult>() {
            @Override
            public void onResult(@NonNull final Channel.GetInputStreamResult r) {
                Log.d("Foo", "Starting WearListenerRunnable");
                cachedThreadPool.execute(new WearListenerRunnable(r));
            }
        });
    }

    @Override
    public void onChannelClosed(final Channel channel, final int closeReason, final int appSpecificErrorCode) {
        Log.d("Foo", "onChannelClosed! " + Integer.toString(closeReason) + ", " + Integer.toString(appSpecificErrorCode));
    }

    @Override
    public void onConnected(@Nullable final Bundle bundle) {
        Log.d("Foo", "onConnected!");
        Wearable.ChannelApi.addListener(this.googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(final int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {

    }

    private class WearListenerRunnable implements Runnable {
        final DataInputStream dataInputStream;

        WearListenerRunnable(final Channel.GetInputStreamResult r) {
            Log.d("Foo", "Obtaining InputStream");
            this.dataInputStream = new DataInputStream(r.getInputStream());
            Log.d("Foo", "DataInputStream obtained");
        }

        @Override
        public void run() {
            Log.d("Foo", "Channel opened");
            try {
                while (true) {
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

                    Log.d("Foo", "==============> rotation(" + xRot + ", " + yRot + ", " + zRot + ", " + dateRot + ")");
                    Log.d("Foo", "==============> acceleration(" + xAcc + ", " + yAcc + ", " + zAcc + ", " + dateAcc + ")");
                    if (xTouch < 0 && yTouch < 0) {
                        Log.d("Foo", "==============> No touch......");
                    } else {
                        Log.d("Foo", "==============> touch(" + xTouch + ", " + yTouch + ", " + stateTouch + ", " + dateTouch + ")");
                    }

                    (new Thread(new UDPRunnable(new Rotation(xRot, yRot, zRot, dateRot), new Acceleration(xAcc, yAcc, zAcc, dateAcc), new Touch(xTouch, yTouch, stateTouch, dateTouch)))).start();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }


    private final class UDPRunnable implements Runnable {
        private final Rotation rotation;
        private final Acceleration acceleration;
        private final Touch touch;

        UDPRunnable(final Rotation rotation, final Acceleration acceleration, final Touch touch) {
            this.rotation = rotation;
            this.acceleration = acceleration;
            this.touch = touch;
        }

        @Override
        public void run() {
            try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                 DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
                dataOutputStream.writeFloat(this.rotation.x);
                dataOutputStream.writeFloat(this.rotation.y);
                dataOutputStream.writeFloat(this.rotation.z);
                dataOutputStream.writeLong(this.rotation.timestamp);

                dataOutputStream.writeFloat(this.acceleration.x);
                dataOutputStream.writeFloat(this.acceleration.y);
                dataOutputStream.writeFloat(this.acceleration.z);
                dataOutputStream.writeLong(this.acceleration.timestamp);

                if (this.touch.equals(NO_TOUCH)) {
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
                WearListenerService.this.datagramSocket.send(new DatagramPacket(bytes, byteArray.length, INET_ADDRESS, PORT));
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}