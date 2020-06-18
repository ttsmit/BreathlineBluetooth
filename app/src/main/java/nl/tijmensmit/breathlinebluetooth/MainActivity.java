package nl.tijmensmit.breathlinebluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.companion.AssociationRequest;
import android.companion.BluetoothLeDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.os.Bundle;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    MyDeviceSelectionActivity testAcaCadara = Context.getSystemService(Context.COMPANION_DEVICE_SERVICE);
    testAcaCadara.associate(android.companion.AssociationRequest, android.companion.CompanionDeviceManager.Callback, android.os.Handler);


}
