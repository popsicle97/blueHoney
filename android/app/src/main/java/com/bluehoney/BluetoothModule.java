package com.bluehoney;

import static com.facebook.react.common.ReactConstants.TAG;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;


public class BluetoothModule extends ReactContextBaseJavaModule implements
        BluetoothServiceStateObserver, LifecycleEventListener {

    private static final String TAG = "BluetoothModule";

    public static final String EVENT_DEVICE_ALREADY_PAIRED = "EVENT_DEVICE_ALREADY_PAIRED";
    public static final String EVENT_DEVICE_FOUND = "EVENT_DEVICE_FOUND";
    public static final String EVENT_DEVICE_DISCOVER_DONE = "EVENT_DEVICE_DISCOVER_DONE";
    public static final String EVENT_DEVICE_DISCOVER_START = "EVENT_DEVICE_DISCOVER_START";
    public static final String EVENT_CONNECTION_LOST = "EVENT_CONNECTION_LOST";
    public static final String EVENT_UNABLE_CONNECT = "EVENT_UNABLE_CONNECT";
    public static final String EVENT_CONNECTED = "EVENT_CONNECTED";
    public static final String EVENT_BLUETOOTH_STATE = "EVENT_BLUETOOTH_STATE";
    public static final String EVENT_BLUETOOTH_NOT_SUPPORT = "EVENT_BLUETOOTH_NOT_SUPPORT";

    public static final String SELECT_DEVICE_REQUEST_CODE = "SELECT_DEVICE_REQUEST_CODE";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    public static final int MESSAGE_STATE_CHANGE = BluetoothService.MESSAGE_STATE_CHANGE;
    public static final int MESSAGE_READ = BluetoothService.MESSAGE_READ;
    public static final int MESSAGE_WRITE = BluetoothService.MESSAGE_WRITE;
    public static final int MESSAGE_DEVICE_NAME = BluetoothService.MESSAGE_DEVICE_NAME;

    public static final int MESSAGE_CONNECTION_LOST = BluetoothService.MESSAGE_CONNECTION_LOST;
    public static final int MESSAGE_UNABLE_CONNECT = BluetoothService.MESSAGE_UNABLE_CONNECT;
    public static final String DEVICE_NAME = BluetoothService.DEVICE_NAME;

    //Listeners
    public static final String EVENT_DEVICE_DISCOVER_STARTED = "EVENT_DEVICE_DISCOVER_STARTED";

    //Return intent extra;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private final ReactApplicationContext reactContext;

    private BluetoothAdapter mmBluetoothAdapter;

    private ReactInstanceManager mReactInstanceManager;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothService mService = null;
    private BluetoothGatt mBluetoothGatt = null;

    private boolean scanning;
    private static final long SCAN_PERIOD = 10000;

    BluetoothModule(ReactApplicationContext reactContext, BluetoothService bluetoothService) {
        super(reactContext);
        this.reactContext = reactContext;
        this.mService = bluetoothService;
        reactContext.addActivityEventListener(mActivityEventListener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.reactContext.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public String getName() {
        return "BluetoothModule";
    }

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            System.out.println(requestCode);
            System.out.println(resultCode);
            System.out.println("ActivityResultFired");
            Log.d(TAG, "onActivityResult " + resultCode);
            //mReactInstanceManager.onActivityResult();
            // ReactApplicationContext.onActivityResult( this, requestCode, resultCode, data );
        }
    };

    public BluetoothAdapter getBluetoothAdapter() {
      BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      return bluetoothAdapter;
    };

    public BluetoothLeScanner getBluetoothLEScanner() {
        BluetoothLeScanner bluetoothLeScanner = getBluetoothAdapter().getBluetoothLeScanner();
        return bluetoothLeScanner;
    };

    @ReactMethod
    public void checkBluetooth(final Promise promise) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        promise.resolve(mBluetoothAdapter.isEnabled());
    }

    @ReactMethod
    public void enableBluetooth(final Promise promise) {
        if (!mmBluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.reactContext.startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT, Bundle.EMPTY);
            promise.resolve("Bluetooth activated");
        } else {
            promise.reject("Bluetooth already active");
        }
    }

    @ReactMethod
    public void disableBluetooth(final Promise promise) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            promise.resolve("Bluetooth disabled");
        } else {
            promise.reject("Bluetooth already disabled");
        }
    }

    @ReactMethod
    public void getPairedDevices(final Promise promise) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            JSONArray pairedDevicesArr = new JSONArray();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    System.out.println(device.getName().toString());
                    try {
                        JSONObject pairedDeviceObj = new JSONObject();
                        pairedDeviceObj.put("name", device.getName().toString());
                        pairedDeviceObj.put("address", device.getAddress().toString());
                        pairedDevicesArr.put(pairedDeviceObj);
                    } catch (Exception e) {
                        Log.e(TAG, "Problem initializing object in getPairedDevices()", e);
                    }
                }
            }
            WritableMap writableMap = Arguments.createMap();
            writableMap.putString("devices", pairedDevicesArr.toString());
            promise.resolve(writableMap);
        } else {
            promise.reject("Bluetooth is disabled");
        }
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "Connection State Change: "+status+" -> " + newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    System.out.println("Connect to device");
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    System.out.println("Disconnected from device");
                    break;
            }
      /*      System.out.println(BluetoothProfile.STATE_DISCONNECTED);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                System.out.println("Successfully connect");
            }

            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                System.out.println("Successfully disconnected");
            }*/
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceName = device.getName();
            String deviceAddress = device.getAddress();
            if (device != null && deviceName != null) {
                JSONObject deviceFound = new JSONObject();
                try {
                    deviceFound.put("name", deviceName);
                    deviceFound.put("address", deviceAddress);
                } catch (Exception e) {
                    Log.e(TAG, "Problem initializing object in Broadcast OnReceive()", e);
                }

                WritableMap writableMap = Arguments.createMap();
                writableMap.putString("device", deviceFound.toString());
                emitReactNativeEvent(EVENT_DEVICE_FOUND, writableMap);
            }
        }
    };


    @ReactMethod
    public void stopBluetoothLEDiscovery(final Promise promise) {
        Log.d(TAG, "Stop le discovery");
        if (getBluetoothAdapter() == null) {
            promise.reject("Bluetooth might not be supported or turned off");
            return;
        }

        if (!getBluetoothAdapter().isEnabled()) {
            promise.reject("Bluetooth is not enabled");
            return;
        }

        getBluetoothLEScanner().stopScan(mScanCallback);
        promise.resolve("Bluetooth discovery stopped");
    }

    @ReactMethod
    public void startBluetoothLEDiscovery(int scanDuration, final Promise promise) {
        long duration = (long) (scanDuration * 1000);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        Handler handler = new Handler();

        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth is not enabled");
            promise.reject("Bluetooth is not enabled");
            return;
        }

        if (bluetoothLeScanner == null){
            promise.reject("Bluetooth might be turned off or not supported");
            return;
        }

        if (!scanning) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Scan stopped");
                    scanning = false;
                    bluetoothLeScanner.stopScan(mScanCallback);
                    promise.resolve("Scan completed");
                    emitReactNativeEvent(EVENT_DEVICE_DISCOVER_DONE, null);
                }
            }, duration);
            scanning = true;
            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            bluetoothLeScanner.startScan(null, scanSettings, mScanCallback);
            emitReactNativeEvent(EVENT_DEVICE_DISCOVER_START, null);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    @ReactMethod
    public void connectLEDevice(String address, final Promise promise) {
        BluetoothDevice mDevice = null;
        if (getBluetoothAdapter() == null) {
            promise.reject("Bluetooth might not be on or supported");
            return;
        }

        if (scanning) {
            getBluetoothLEScanner().stopScan(mScanCallback);
        }

        if (mBluetoothGatt != null) {
            promise.reject("Another device is already connected. Please disconnect from that device");
            return;
        }

        try {
            mDevice = getBluetoothAdapter().getRemoteDevice(address);
            mBluetoothGatt = mDevice.connectGatt(reactContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
            promise.resolve("Connected");
        } catch (IllegalArgumentException e) {
            promise.reject("Device not found with provided address");
            return;
        }
    }

    @ReactMethod
    public void disconnectLEDevice(final Promise promise) {
        Log.d(TAG,"Disconnecting device");
        if (mBluetoothGatt == null) {
            promise.resolve("Device already disconnected");
        } else {
            mBluetoothGatt.disconnect();
            mBluetoothGatt = null;
            Log.d(TAG, "Device disconnected successfully");
            promise.resolve("Device disconnected");
        }
    }

    @ReactMethod
    public void startBluetoothDiscovery() {
        if (!mmBluetoothAdapter.isDiscovering()) {
            mmBluetoothAdapter.cancelDiscovery();
            System.out.println("Stopping bluetooth discovering to avoid flooding");
        }

        if (mmBluetoothAdapter.isEnabled()) {
            int permissionChecked = ContextCompat.checkSelfPermission(reactContext,
                    Manifest.permission.ACCESS_FINE_LOCATION);

            if (permissionChecked == PackageManager.PERMISSION_DENIED) {
                // // TODO: 2018/9/21
                ActivityCompat.requestPermissions(reactContext.getCurrentActivity(),
                        new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                        1);
            }

            mmBluetoothAdapter.startDiscovery();
            emitReactNativeEvent(EVENT_DEVICE_DISCOVER_STARTED, null);
        } else {
            Toast.makeText(reactContext, "Bluetooth is disabled", Toast.LENGTH_SHORT).show();
        }
    }

    @ReactMethod
    public void connectDevice(String address, final Promise promise) {
        if (!mmBluetoothAdapter.isEnabled()) {
            promise.reject("Bluetooth not enabled");
        } else {
            BluetoothDevice device = mmBluetoothAdapter.getRemoteDevice(address);
            mService.connect(device, promise);
        }
    }

    @ReactMethod
    public void disconnectDevice(final Promise promise) {
        mService.stop(promise);
    }

    public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println("Action " + action);
            System.out.println(BluetoothAdapter.ACTION_STATE_CHANGED);
            System.out.println(BluetoothAdapter.EXTRA_STATE);
            System.out.println(BluetoothDevice.ACTION_FOUND);
            System.out.println(action);
            System.out.println(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1));
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_TURNING_OFF) {

                }

                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_TURNING_ON) {
                }

                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    System.out.println("Bluetooth turned off");
                    WritableMap writableMap = Arguments.createMap();
                    writableMap.putInt("status", 0);
                    emitReactNativeEvent(EVENT_BLUETOOTH_STATE, writableMap);
                }

                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                    System.out.println("Bluetooth turned on");
                    WritableMap writableMap = Arguments.createMap();
                    writableMap.putInt("status", 1);
                    emitReactNativeEvent(EVENT_BLUETOOTH_STATE, writableMap);
                }
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                System.out.println("Start Scanning");
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    System.out.println("Off");
                }

                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    JSONObject deviceFound = new JSONObject();
                    try {
                        deviceFound.put("name", deviceName);
                        deviceFound.put("address", deviceHardwareAddress);
                    } catch (Exception e) {
                        Log.e(TAG, "Problem initializing object in Broadcast OnReceive()", e);
                    }

                    WritableMap writableMap = Arguments.createMap();
                    writableMap.putString("device", deviceFound.toString());
                    emitReactNativeEvent(EVENT_DEVICE_FOUND, writableMap);
                }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                System.out.println("Discovery started");
                Toast.makeText(reactContext, "Bluetooth discovery started", Toast.LENGTH_SHORT).show();
                emitReactNativeEvent(EVENT_DEVICE_DISCOVER_START, null);
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                System.out.println("Discovery stopped");
                Toast.makeText(reactContext, "Bluetooth discovery done", Toast.LENGTH_SHORT).show();
                emitReactNativeEvent(EVENT_DEVICE_DISCOVER_DONE, null);
            }
       /*     if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                System.out.println("Check if discovery has ended");
            }*/

            //if (BluetoothAdapter.STATE_OFF == )
        }
    };


    public void emitReactNativeEvent(String event, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event, params);
    }


/*
    @Override
    protected void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult " + resultCode);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(reactContext, "Bluetooth enabled", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(reactContext, "User denied permission to bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                System.out.println("User connected");

             } else if (resultCode == Activity.RESULT_CANCELED) {
                System.out.println("User cancelled connection");
            }
        }
   */
/*     switch (requestCode) {
            case REQUEST_CONNECT_DEVICE: {
                // When request to connect to device is allowed by user;
                if (resultCode == Activity.RESULT_OK) {
                    String address = intent.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                    Log.d(TAG, "onActivityResult() getExtras() address : " + address);
                    BluetoothDevice device = mmBluetoothAdapter.getRemoteDevice(address);

                }
            }
        }*//*

    }

*/

    @Override
    public void onBluetoothServiceStateChanged(int state, Map<String, Object> bundle) {
        Log.d(TAG, "on bluetoothServiceStatChange:" + state);
        switch (state) {
            case BluetoothService.STATE_CONNECTED:
            case MESSAGE_DEVICE_NAME: {
                // save the connected device's name
     /*           mConnectedDeviceName = (String) bundle.get(DEVICE_NAME);
                Log.d(TAG, "No Promise found.");
                WritableMap params = Arguments.createMap();
                params.putString(DEVICE_NAME, mConnectedDeviceName);*/
                emitReactNativeEvent(EVENT_CONNECTED, null);
                break;
            }
            case MESSAGE_CONNECTION_LOST: {
                emitReactNativeEvent(EVENT_CONNECTION_LOST, null);
                break;
            }
            case MESSAGE_UNABLE_CONNECT: { // 无法连接设备
                emitReactNativeEvent(EVENT_UNABLE_CONNECT, null);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onHostResume() {
        System.out.println("System resumed");
    }

    @Override
    public void onHostPause() {
        System.out.println("System paused");
    }

    @Override
    public void onHostDestroy() {
        System.out.println("System destroyed");
    }

 /*   @Override
    public void onBluetoothServiceStateChanged(int state, Map<String, Object> boundle) {

    }*/
}
