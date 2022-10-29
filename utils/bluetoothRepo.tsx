import {Alert, NativeModules, ToastAndroid} from 'react-native';
import {BluetoothDeviceT} from '../interfaces/bluetooth';
import {rnAlert} from './alert';
import {bluetoothErrorhandling} from './errorHandling';

const {BluetoothModule} = NativeModules;

const EVENT_BLUETOOTH_STATE = 'EVENT_BLUETOOTH_STATE';
const EVENT_DEVICE_DISCOVER_DONE = 'EVENT_DEVICE_DISCOVER_DONE';
const EVENT_DEVICE_DISCOVER_START = 'EVENT_DEVICE_DISCOVER_START';
const EVENT_DEVICE_FOUND = 'EVENT_DEVICE_FOUND';

const checkAndEnableBluetooth = async () => {
  try {
    const isEnabled: boolean = await checkBluetooth();
    if (!isEnabled) {
      const isEnabledResponse: string = await enableBluetooth();
      return ToastAndroid.show(isEnabledResponse, ToastAndroid.SHORT);
    }
    ToastAndroid.show('Bluetooth is turned on', ToastAndroid.SHORT);
  } catch (error: any) {
    console.error(error);
    Alert.prompt('Bluetooth activation', error);
  }
};

//checkBluetooth only resolves to true or false, no rejects;
const checkBluetooth = async () => {
  const isEnabled = await BluetoothModule.checkBluetooth();
  return isEnabled;
};

const enableBluetooth = async () => {
  let response: string = '';
  try {
    response = await BluetoothModule.enableBluetooth();
    ToastAndroid.show(response, ToastAndroid.SHORT);
  } catch (error: any) {
    // console.error(error);
    Alert.prompt('Bluetooth activation', error);
    response = error;
  }
  return response;
};

const connectDevice = async (device: BluetoothDeviceT) => {
  let connected: boolean = false;
  try {
    const isConnected = await BluetoothModule.connectDevice(device.address);
    console.log('Check device', isConnected);
    if (isConnected && typeof isConnected !== 'undefined') {
      const connection = JSON.parse(isConnected.connection as any);
      connected = connection;
    }
  } catch (error) {
    console.error(error);
    rnAlert('Device connection', bluetoothErrorhandling(error), '', 'Ok');
    connected = false;
  }
  return connected;
};

const connectLEDevice = async (device: BluetoothDeviceT) => {
  let connected: boolean = false;
  try {
    const isConnected = await BluetoothModule.connectLEDevice(device.address);
    console.log(isConnected);
    connected = true;
  } catch (error) {
    console.error(error);
    rnAlert('Device connection', bluetoothErrorhandling(error), '', 'Ok');
    connected = false;
  }
  return connected;
};

const disconnectDevice = async () => {
  let disconnected: boolean = false;
  try {
    const disconnectStatus = await BluetoothModule.disconnectDevice();
    ToastAndroid.show(disconnectStatus, ToastAndroid.SHORT);
    disconnected = true;
  } catch (error) {
    console.error(error);
    rnAlert('Device disconnection', bluetoothErrorhandling(error));
  }
  return disconnected;
};

const disconnectLEDevice = async () => {
  let disconnected: boolean = false;
  try {
    const status = await BluetoothModule.disconnectLEDevice();
    ToastAndroid.show(status, ToastAndroid.SHORT);
    disconnected = true;
  } catch (error) {
    console.error(error);
    rnAlert('Device disconnection', bluetoothErrorhandling(error));
    disconnected = false;
  }
  return disconnected;
};

const startBluetoothDiscovery = async () =>
  BluetoothModule.startBluetoothDiscovery();

const startBluetoothLeDiscovery = async (scanDurationSeconds: number) => {
  let scanStatus: string;
  try {
    scanStatus = await BluetoothModule.startBluetoothLEDiscovery(
      scanDurationSeconds,
    );
  } catch (error) {
    console.error(error);
    scanStatus = bluetoothErrorhandling(error);
  }

  ToastAndroid.show(scanStatus, ToastAndroid.SHORT);
};

const getPairedDevices = async () => {
  let pairedDevices: BluetoothDeviceT[] = [];
  try {
    const pairedDevicesRaw = await BluetoothModule.getPairedDevices();
    console.log(pairedDevicesRaw);
    if (pairedDevicesRaw) {
      pairedDevices = JSON.parse(pairedDevicesRaw.devices);
    }
  } catch (error: any) {
    ToastAndroid.show(bluetoothErrorhandling(error), ToastAndroid.SHORT);
  }

  return pairedDevices;
};

export const bluetoothRepo = {
  EVENT_DEVICE_FOUND,
  EVENT_BLUETOOTH_STATE,
  EVENT_DEVICE_DISCOVER_DONE,
  EVENT_DEVICE_DISCOVER_START,
  checkAndEnableBluetooth,
  startBluetoothDiscovery,
  startBluetoothLeDiscovery,
  checkBluetooth,
  enableBluetooth,
  getPairedDevices,
  connectDevice,
  connectLEDevice,
  disconnectDevice,
  disconnectLEDevice,
};
