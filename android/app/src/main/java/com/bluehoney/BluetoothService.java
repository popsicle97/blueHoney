package com.bluehoney;

import static com.facebook.react.views.textinput.ReactTextInputManager.TAG;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;

public class BluetoothService {

    // Debugging
    private static final String TAG = "BluetoothService";

    private static final String NAME_SECURE = "BluetoothHoneywell";

    private static List<BluetoothServiceStateObserver> observers = new ArrayList<BluetoothServiceStateObserver>();

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final int MESSAGE_STATE_CHANGE = 4;
    public static final int MESSAGE_READ = 5;
    public static final int MESSAGE_WRITE = 6;
    public static final int MESSAGE_DEVICE_NAME = 7;
    public static final int MESSAGE_CONNECTION_LOST = 8;
    public static final int MESSAGE_UNABLE_CONNECT = 9;

    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";

    public static ConnectedThread mConnectedThread;
    public static ConnectThread mConnectThread;

    private final BluetoothAdapter mAdapter;

    private Promise mBluetoothPromise;

    private int mState;
    private int mNewState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    public BluetoothService(ReactContext reactContext) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
    }


    private void connectionFailed() {
        mState = STATE_NONE;
        BluetoothService.this.stop(null);
    }

    private void connectionLost() {
        mState = STATE_NONE;
    }

    public synchronized int getState() {
     return mState;
    }


    public synchronized void connect(BluetoothDevice device, final Promise promise) {
        Log.d(TAG, "Connect to: " + device);

        mBluetoothPromise = promise;
        // Cancel any thread attempting to make a connection

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "Connected socket");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    public synchronized void stop(final Promise promise) {
        mBluetoothPromise = promise;
        Log.d(TAG, "Stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        promise.resolve("Device disconnected");
        mState = STATE_NONE;
    }

    public void write(byte[] out,final Promise promise) {
        ConnectedThread tempConnectedThread;

        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                promise.reject("Bluetooth not connected");
                return;
            };
            tempConnectedThread = mConnectedThread;
        }

        tempConnectedThread.write(out);
    }

    public class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmpSocket = null;
            mmDevice = device;

            try {
                tmpSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket creation failed ConnectThread", e);
            }

            mmSocket = tmpSocket;
            mState = STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.e(TAG, "Problem connecting socket 1st try", connectException);

                try {
                    mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                    mmSocket.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Failed with second method", e);
                    try {
                        mmSocket.close();
                    } catch (IOException closeException) {
                        closeException.printStackTrace();
                        Log.e(TAG,"Problem closing socket", closeException);
                    }
                    mBluetoothPromise.reject("Problem connecting to device");
                    return;
                }
                //Unable to connect, close socket and return;
            }

            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
                Thread.sleep(1000);
            } catch (IOException e) {
                Log.e(TAG, "Problem closing connection close()", e);
                mBluetoothPromise.reject("Failed to disconnect bluetooth device");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        //private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch(IOException e) {
                Log.e(TAG, "Problem initializing Input & Output Stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            mState = STATE_CONNECTED;
            Log.i(TAG, "Connected");
            WritableMap writableMap = Arguments.createMap();
            writableMap.putBoolean("connection", true);
            mBluetoothPromise.resolve(writableMap);
            mBluetoothPromise = null;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream.
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity.
        /*            bundle = new HashMap<String, Object>();
                    bundle.put("bytes", numBytes);
                    infoObervers(MESSAGE_READ, bundle);*/
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                mmOutStream.flush();

            } catch (IOException e) {
                Log.e(TAG, "Problem writing outstream", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() ConnectedThread Failed", e);
            }
        }
    }

    private synchronized void infoObervers(int code, Map<String, Object> bundle) {
        for (BluetoothServiceStateObserver ob : observers) {
            ob.onBluetoothServiceStateChanged(code, bundle);
        }
    }

}


