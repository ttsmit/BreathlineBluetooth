package nl.tijmensmit.breathlinebluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.companion.AssociationRequest;
import android.companion.BluetoothLeDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private Bluetooth mBluetooth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetooth = new Bluetooth(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBluetooth.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetooth.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetooth.stop();
    }

}
