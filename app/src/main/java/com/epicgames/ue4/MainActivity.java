package com.epicgames.ue4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.epicgames.ue4.unrealwatchredirector.R;

public final class MainActivity extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, WearListenerService.class));
    }
}
