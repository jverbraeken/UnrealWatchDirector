package com.epicgames.ue4;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.epicgames.ue4.runnables.ReceiveDataRunnable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WearListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks {
    public static final String TAG = "Foo";
    public static final Touch NO_TOUCH = new Touch(-1, -1, (byte) 0);
    public static final InetAddress INET_ADDRESS;
    public static final int PORT = 55056;
    public static final byte COMTP_SENSOR_DATA = 1;
    public static final byte COMTP_SHAKING_STARTED = 2;
    public static final byte COMTP_SHAKING_STOPPED = 3;

    private static final String IP_ADDRESS = "192.168.1.13";
    private static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private static DatagramSocket datagramSocket;

    static {
        InetAddress tmp = null;
        try {
            tmp = InetAddress.getByName(IP_ADDRESS);
        } catch (final UnknownHostException e) {
            e.printStackTrace();
        }
        INET_ADDRESS = tmp;
    }

    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        Log.d(TAG, "WearListenerService created");

        if (this.googleApiClient == null) {
            this.googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(Wearable.API)
                    .build();
        }

        if (!this.googleApiClient.isConnected()) {
            Log.d(TAG, "connecting to googleApiClient");
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
        Log.d(TAG, "onChannelOpened!");
        channel.getInputStream(this.googleApiClient).setResultCallback(new ResultCallback<Channel.GetInputStreamResult>() {
            @Override
            public void onResult(@NonNull final Channel.GetInputStreamResult r) {
                Log.d(TAG, "Starting WearListenerRunnable");
                cachedThreadPool.execute(new ReceiveDataRunnable(r, datagramSocket));
            }
        });
    }

    @Override
    public void onChannelClosed(final Channel channel, final int closeReason, final int appSpecificErrorCode) {
        Log.d(TAG, "onChannelClosed! " + Integer.toString(closeReason) + ", " + Integer.toString(appSpecificErrorCode));
    }

    @Override
    public void onConnected(@Nullable final Bundle bundle) {
        Log.d(TAG, "onConnected!");
        Wearable.ChannelApi.addListener(this.googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}