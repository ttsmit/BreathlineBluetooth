package nl.tijmensmit.breathlinebluetooth;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public final class Bluetooth {
    private final static String TAG = Bluetooth.class.getSimpleName();

    private BLEService mBluetoothLeService;
    private String mDeviceAddress;
    private boolean mConnected = false;
    private Context context;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BLEService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to Initialize");
//                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
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
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                // Other actions?
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)){
                mConnected = false;
                // ...
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                Log.d(TAG, "Supported Services:" + mBluetoothLeService.getSupportedGattServices());
                //Recieve gattService as List<BluetoothGattService>
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)){
                Log.d(TAG, "Extra Data Available:" + BLEService.EXTRA_DATA);
                // Recieve data as String
            }
        }
    };

    public Bluetooth(Context context){
        this.context = context;
        Intent gattServiceIntent = new Intent(context, BLEService.class);
        context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }


    public void connect() {
        mDeviceAddress = "80:1F:12:BE:2B:7C";
        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result:" + result);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void pause() {
        context.unregisterReceiver(mGattUpdateReceiver);
    }


    public void stop() {
        context.unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
}
