package nl.tijmensmit.breathlinebluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
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

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private CompanionDeviceManager deviceManager;
    private AssociationRequest pairingRequest;
    private BluetoothLeDeviceFilter deviceFilter;
    private BluetoothDevice mDevice;
    private static final int SELECT_DEVICE_REQUEST_CODE = 42;

    private BluetoothConnectionService mBluetoothLeService;
    private String mDeviceAddress;
    private boolean mConnected = false;

    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothConnectionService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize(bluetoothAdapter)) {
                Log.e(TAG, "Unable to Initialize");
//                finish();
            }
//            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothConnectionService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                // Other actions?
            } else if (BluetoothConnectionService.ACTION_GATT_DISCONNECTED.equals(action)){
                mConnected = false;
                // ...
            } else if (BluetoothConnectionService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                Log.d(TAG, "Supported Services:" + mBluetoothLeService.getSupportedGattServices());
                //Recieve gattService as List<BluetoothGattService>
            } else if (BluetoothConnectionService.ACTION_DATA_AVAILABLE.equals(action)){
                Log.d(TAG, "Extra Data Available:" + BluetoothConnectionService.EXTRA_DATA);
                // Recieve data as String
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        Intent gattServiceIntent = new Intent(this, BluetoothConnectionService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.d(TAG, "Enabled BLE");
        }

        Set<BluetoothDevice> allDevices = bluetoothAdapter.getBondedDevices();
        if (allDevices.size() == 0) {
            scanForDevices();
        } else {
            boolean found = false;
            for (BluetoothDevice currentDevice : allDevices) {
                String deviceName = currentDevice.getName();
                Log.d(TAG, deviceName);
                Pattern p = Pattern.compile("Breath.ine.*");
                Matcher m = p.matcher(deviceName);
                boolean b = m.matches();
                if(m.matches()) {
                    found = true;
                    connect(currentDevice);
                    break;
                }
            }
            if(!found) {
                scanForDevices();
            }
        }

    }

    void scanForDevices() {
        deviceManager = getSystemService(CompanionDeviceManager.class);
        deviceFilter = new BluetoothLeDeviceFilter.Builder()
                .setNamePattern(Pattern.compile("BreathLine.*"))
                .setRenameFromName(null, null, 0, 10)
                .build();
        pairingRequest = new AssociationRequest.Builder()
//                .addDeviceFilter(deviceFilter)
                .setSingleDevice(false)
                .build();

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
        this.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if(mDevice != null){
            connect(mDevice);
        } else {
            scanForDevices();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    public void showAccelerometerData(){
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_DEVICE_REQUEST_CODE &&
                resultCode == Activity.RESULT_OK) {
            // User has chosen to pair with the Bluetooth device.
//            android.bluetooth.le.ScanResult deviceToPair =
//                    data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
//            Log.d(TAG, "DeviceToPair:"+deviceToPair);
//            assert deviceToPair != null;
//            BluetoothDevice mDevice = deviceToPair.getDevice();
            mDevice = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
//            mDevice.createBond();
            connect(mDevice);
        }
    }

    public void connect(BluetoothDevice device) {
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(device);
            Log.d(TAG, "Connect request result:" + result);
        } else {
            Log.d(TAG, "Not Connected to BluetoothConnectionService");
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothConnectionService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothConnectionService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothConnectionService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothConnectionService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}
