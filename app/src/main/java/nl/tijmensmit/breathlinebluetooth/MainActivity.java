package nl.tijmensmit.breathlinebluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.widget.TextView;

import java.util.List;
import java.util.Set;
import java.util.UUID;
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

    private String UUID_ACCELEROMETER_X = "68d5151b-d0ec-4683-80e7-3ca4dd42034d";
    private String UUID_ACCELEROMETER_Y = "68d5151c-d0ec-4683-80e7-3ca4dd42034d";
    private String UUID_ACCELEROMETER_Z = "68d5151d-d0ec-4683-80e7-3ca4dd42034d";

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
            if(mDevice != null){
                connect(mDevice);
            }
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
                List<BluetoothGattService> supportedGattServices = mBluetoothLeService.getSupportedGattServices();
                enableNotification(supportedGattServices);
                Log.d(TAG, "Supported Services:" + supportedGattServices);
            } else if (BluetoothConnectionService.ACTION_DATA_AVAILABLE.equals(action)){
                String data = intent.getStringExtra(BluetoothConnectionService.EXTRA_DATA);
                int value = 0;
                if(data != null && data.length() != 0){
                    value = Integer.parseInt(data, 16);

                }
                String UUID = intent.getStringExtra(BluetoothConnectionService.EXTRA_CHARACTERISTIC);
                updateValue(UUID, value);
            }
        }
    };

    private void updateValue(String uuid, int value) {
        float reading = value/100;
        reading -= 100;
        String readingAsText = Float.toString(reading);
        if(uuid.equals(UUID_ACCELEROMETER_X)){
            TextView AccelerometerXText =new TextView(this);
            AccelerometerXText=(TextView)findViewById(R.id.textView_X);
            AccelerometerXText.setText(readingAsText);
        }
    }

    private void enableNotification(List<BluetoothGattService> supportedGattServices) {
        for (BluetoothGattService gattService : supportedGattServices){
            List<BluetoothGattCharacteristic> supportedCharacteristics = gattService.getCharacteristics();
            for(BluetoothGattCharacteristic gattCharacteristic : supportedCharacteristics){
                byte[] value = gattCharacteristic.getValue();
                String UUID = gattCharacteristic.getUuid().toString();
                if( UUID.equals(UUID_ACCELEROMETER_X) || UUID.equals(UUID_ACCELEROMETER_Y) || UUID.equals(UUID_ACCELEROMETER_Z)){
//                    Log.d(TAG, "Characteristic Value:" + value);
                    mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                }

            }
        }

    }


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
//                    found = true;
//                    connect(currentDevice);
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
//                .setNamePattern(Pattern.compile("BreathLine.*"))
//                .setRenameFromName(null, null, 0, 10)
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
//            final BluetoothGattCharacteristic characteristic =
//                    mGattCharacteristics.get(groupPosition).get(childPosition);
//            mBluetoothLeService.setCharacteristicNotification(xAccel, true);
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
