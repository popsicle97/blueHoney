import {useCallback, useEffect, useState} from 'react';
import {DeviceEventEmitter} from 'react-native';
import {BluetoothDeviceT} from '../interfaces/bluetooth';
import {bluetoothRepo} from '../utils/bluetoothRepo';

const useBluetooth = () => {
  const [bluetoothStatus, setBluetoothStatus] = useState<boolean>(false);
  const [isScanning, setIsScanning] = useState<boolean>(false);
  const [devices, setDevices] = useState<{
    foundDevices: BluetoothDeviceT[];
    pairedDevices: BluetoothDeviceT[];
  }>({
    foundDevices: [],
    pairedDevices: [],
  });
  const [connectedDevice, setConnectedDevice] = useState<BluetoothDeviceT>();

  const checkBluetoothStatus = useCallback(async () => {
    const isEnabled: boolean = await bluetoothRepo.checkBluetooth();
    setBluetoothStatus(isEnabled);
    if (!isEnabled) {
      await bluetoothRepo.enableBluetooth();
      await getPairedDevices();
    }
  }, []);

  // const checkBluetoothStatus = async () => {
  //   const isEnabled: boolean = await bluetoothRepo.checkBluetooth();
  //   setBluetoothStatus(isEnabled);
  //   if (!isEnabled) {
  //     await bluetoothRepo.enableBluetooth();
  //     getPairedDevices();
  //   }
  // };

  const connectDevice = async (device: BluetoothDeviceT) => {
    const isConnected = await bluetoothRepo.connectDevice(device);
    if (isConnected) {
      setConnectedDevice(device);
    }
  };

  const connectLEDevice = async (device: BluetoothDeviceT) => {
    const isConnected = await bluetoothRepo.connectLEDevice(device);
    if (isConnected) {
      setConnectedDevice(device);
    }
  };

  const disconnectDevice = async () => {
    const isDisconnected = await bluetoothRepo.disconnectDevice();
    if (isDisconnected) {
      setConnectedDevice(undefined);
    }
  };

  const disconnectLEDevice = async () => {
    const isDisconnected = await bluetoothRepo.disconnectLEDevice();
    console.log('LE', isDisconnected);
    if (isDisconnected) {
      setConnectedDevice(undefined);
    }
  };

  const getPairedDevices = async () => {
    const pairedDevices: BluetoothDeviceT[] =
      await bluetoothRepo.getPairedDevices();
    setDevices(oriDevices => {
      oriDevices.pairedDevices = [...pairedDevices];
      return {...oriDevices};
    });
  };

  useEffect(() => {
    const bluetoothStatusListener = (event: {status: number}) => {
      if (event || typeof event !== 'undefined') {
        setBluetoothStatus(event.status === 0 ? false : true);
      }
    };
    const bluetoothDiscoverListener = (event: {device: BluetoothDeviceT}) => {
      if (event || typeof event !== 'undefined') {
        const device: BluetoothDeviceT = JSON.parse(event.device as any);
        if (device.name) {
          setDevices(oriDevices => {
            if (
              !oriDevices.foundDevices.some(
                oriFoundDevice => oriFoundDevice.address === device.address,
              )
            ) {
              oriDevices.foundDevices = [...oriDevices.foundDevices, device];
            }
            return {...oriDevices};
          });
        }
      }
    };
    const bluetoothDiscoverStartListener = () => {
      setIsScanning(true);
    };
    const bluetoothDiscoverDoneListener = () => {
      setIsScanning(false);
    };
    (async () => {
      await checkBluetoothStatus();
      await getPairedDevices();
    })();
    const EVENT_BLUETOOTH_STATE_LT = DeviceEventEmitter.addListener(
      bluetoothRepo.EVENT_BLUETOOTH_STATE,
      bluetoothStatusListener,
    );
    const EVENT_DEVICE_FOUND_LT = DeviceEventEmitter.addListener(
      bluetoothRepo.EVENT_DEVICE_FOUND,
      bluetoothDiscoverListener,
    );
    const EVENT_DEVICE_DISCOVER_START_LT = DeviceEventEmitter.addListener(
      bluetoothRepo.EVENT_DEVICE_DISCOVER_START,
      bluetoothDiscoverStartListener,
    );
    const EVENT_DEVICE_DISCOVER_DONE_LT = DeviceEventEmitter.addListener(
      bluetoothRepo.EVENT_DEVICE_DISCOVER_DONE,
      bluetoothDiscoverDoneListener,
    );
    return () => {
      EVENT_BLUETOOTH_STATE_LT.remove();
      EVENT_DEVICE_FOUND_LT.remove();
      EVENT_DEVICE_DISCOVER_START_LT.remove();
      EVENT_DEVICE_DISCOVER_DONE_LT.remove();
    };
  }, [checkBluetoothStatus]);

  return {
    bluetoothStatus,
    devices,
    setDevices,
    isScanning,
    connectDevice,
    connectedDevice,
    connectLEDevice,
    disconnectDevice,
    disconnectLEDevice,
  };
};

export default useBluetooth;
