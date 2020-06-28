//package nl.tijmensmit.breathlinebluetooth;
//
//import android.app.Activity;
//import android.companion.AssociationRequest;
//import android.companion.BluetoothLeDeviceFilter;
//import android.companion.CompanionDeviceManager;
//import android.content.BroadcastReceiver;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.IntentSender;
//import android.content.ServiceConnection;
//import android.os.IBinder;
//import android.util.Log;
//
//import java.util.regex.Pattern;
//
//public final class Bluetooth {
//    private final static String TAG = Bluetooth.class.getSimpleName();
//
//    private BluetoothConnectionService mBluetoothLeService;
//    private String mDeviceAddress;
//    private boolean mConnected = false;
//    private Context context;
//    private CompanionDeviceManager deviceManager;
//    private AssociationRequest pairingRequest;
//    private BluetoothLeDeviceFilter deviceFilter;
//    private static final int SELECT_DEVICE_REQUEST_CODE = 42;
//
//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            mBluetoothLeService = ((BluetoothConnectionService.LocalBinder) service).getService();
//            if (!mBluetoothLeService.initialize()) {
//                Log.e(TAG, "Unable to Initialize");
////                finish();
//            }
//            mBluetoothLeService.connect(mDeviceAddress);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mBluetoothLeService = null;
//        }
//    };
//
//    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (BluetoothConnectionService.ACTION_GATT_CONNECTED.equals(action)) {
//                mConnected = true;
//                // Other actions?
//            } else if (BluetoothConnectionService.ACTION_GATT_DISCONNECTED.equals(action)){
//                mConnected = false;
//                // ...
//            } else if (BluetoothConnectionService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
//                Log.d(TAG, "Supported Services:" + mBluetoothLeService.getSupportedGattServices());
//                //Recieve gattService as List<BluetoothGattService>
//            } else if (BluetoothConnectionService.ACTION_DATA_AVAILABLE.equals(action)){
//                Log.d(TAG, "Extra Data Available:" + BluetoothConnectionService.EXTRA_DATA);
//                // Recieve data as String
//            }
//        }
//    };
//
//    public Bluetooth(Context context){
//
//        // ...
//        deviceManager = context.getSystemService(CompanionDeviceManager.class);
//
//        // To skip filtering based on name and supported feature flags (UUIDs),
//        // don't include calls to setNamePattern() and addServiceUuid(),
//        // respectively. This example uses Bluetooth.
//        deviceFilter = new BluetoothLeDeviceFilter.Builder()
//                .setNamePattern(Pattern.compile("Breath.ine.*"))
//                .setRenameFromName(null, null, 0, 10)
//                .build();
//
//        // The argument provided in setSingleDevice() determines whether a single
//        // device name or a list of device names is presented to the user as
//        // pairing options.
//        pairingRequest = new AssociationRequest.Builder()
//                .addDeviceFilter(deviceFilter)
//                .setSingleDevice(false)
//                .build();
//
//        // When the app tries to pair with the Bluetooth device, show the
//        // appropriate pairing request dialog to the user.
//        deviceManager.associate(pairingRequest,
//                new CompanionDeviceManager.Callback() {
//                    @Override
//                    public void onDeviceFound(IntentSender chooserLauncher) {
//                        try {
//                            context.startIntentSenderForResult(chooserLauncher,
//                                    SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0);
//                        } catch (IntentSender.SendIntentException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(CharSequence charSequence) {
//                    }
//                },
//                null);
//
//
//        this.context = context;
//        Intent gattServiceIntent = new Intent(context, BluetoothConnectionService.class);
//        context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
//    }
//
//
//    public void connect(String mDeviceAddress) {
//        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
//        if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.d(TAG, "Connect request result:" + result);
//        }
//    }
//
//    private static IntentFilter makeGattUpdateIntentFilter() {
//        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(BluetoothConnectionService.ACTION_GATT_CONNECTED);
//        intentFilter.addAction(BluetoothConnectionService.ACTION_GATT_DISCONNECTED);
//        intentFilter.addAction(BluetoothConnectionService.ACTION_GATT_SERVICES_DISCOVERED);
//        intentFilter.addAction(BluetoothConnectionService.ACTION_DATA_AVAILABLE);
//        return intentFilter;
//    }
//
//    public void pause() {
//        context.unregisterReceiver(mGattUpdateReceiver);
//    }
//
//
//    public void stop() {
//        context.unbindService(mServiceConnection);
//        mBluetoothLeService = null;
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        context.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == SELECT_DEVICE_REQUEST_CODE &&
//                resultCode == Activity.RESULT_OK) {
//            // User has chosen to pair with the Bluetooth device.
//            android.bluetooth.le.ScanResult deviceToPair =
//                    data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
//            Log.d(TAG, "DeviceToPair:"+deviceToPair);
//            assert deviceToPair != null;
//            deviceToPair.getDevice().createBond();
//
//        }
//    }
//}
