package nl.tijmensmit.breathlinebluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.le.ScanResult;
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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private Bluetooth mBluetooth;
    MyDeviceSelectionActivity myDeviceSelectionActivity;

    //...
    private CompanionDeviceManager deviceManager;
    private AssociationRequest pairingRequest;
    private BluetoothLeDeviceFilter deviceFilter;

    private static final int SELECT_DEVICE_REQUEST_CODE = 42;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mBluetooth = new Bluetooth(this);

        // ...
        deviceManager = getSystemService(CompanionDeviceManager.class);

        // To skip filtering based on name and supported feature flags (UUIDs),
        // don't include calls to setNamePattern() and addServiceUuid(),
        // respectively. This example uses Bluetooth.
        deviceFilter = new BluetoothLeDeviceFilter.Builder()
                .setNamePattern(Pattern.compile("Breath.ine.*"))
                .setRenameFromName(null, null, 0, 10)
                .build();

        // The argument provided in setSingleDevice() determines whether a single
        // device name or a list of device names is presented to the user as
        // pairing options.
        pairingRequest = new AssociationRequest.Builder()
                .addDeviceFilter(deviceFilter)
                .setSingleDevice(false)
                .build();

        // When the app tries to pair with the Bluetooth device, show the
        // appropriate pairing request dialog to the user.
        deviceManager.associate(pairingRequest,
                new CompanionDeviceManager.Callback() {
                    @Override
                    public void onDeviceFound(IntentSender chooserLauncher) {
                        try {
                            startIntentSenderForResult(chooserLauncher,
                                    SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(CharSequence charSequence) {

                    }
                },
                null);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mBluetooth.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mBluetooth.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mBluetooth.stop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_DEVICE_REQUEST_CODE &&
                resultCode == Activity.RESULT_OK) {
            // User has chosen to pair with the Bluetooth device.
            android.bluetooth.le.ScanResult deviceToPair =
                    data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
            Log.d(TAG, "DeviceToPair:"+deviceToPair);
            assert deviceToPair != null;
            deviceToPair.getDevice().createBond();

            // ... Continue interacting with the paired device.
        }
    }

}
