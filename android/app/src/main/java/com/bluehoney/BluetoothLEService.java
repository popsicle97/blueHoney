package com.bluehoney;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactContext;

import org.json.JSONObject;

public class BluetoothLEService extends Service {

    private static final String TAG = "BluetoothLEService";

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bluetoothAdapter;

    public ReactContext reactContext;

    public Promise mPromise = null;


    public BluetoothLEService(ReactContext reactContext){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.reactContext = reactContext;
    }

 /*   private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            String deviceName = device.getName();
            String deviceAddress = device.getAddress();

            if (device != null && deviceName != null) {
                JSONObject deviceFound = new JSONObject();
                try {
                    deviceFound.put("name", deviceName);
                    deviceFound.put("address", deviceAddress);
                } catch (Exception e) {
                    Log.e(TAG, "Problem initializing device object onScanResult()", e);
                }
            }
        }
    };*/


    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //successfully connected to the GATT Server

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //disconnected from the GATT Server
            }
        }
    };

  /*  public void scanLEDevices() {

    }*/

    public void connect(final String address, final Promise promise) {
        if (bluetoothAdapter == null) {
            Log.d(TAG, "Bluetooth might not be supported or is off");
            promise.reject("Bluetooth is not supported or might be off");
        }

        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = device.connectGatt(reactContext, false, bluetoothGattCallback);
            promise.resolve("Connected");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Device not found with provided address", e);
            promise.reject("Device not found with provided address");
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
