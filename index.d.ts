import 'react-native';

export interface BluetoothModuleInterface {
  checkBluetooth: () => Promise<boolean>;
  enableBluetooth: () => Promise<string>;
  getPairedDevices: () => Promise<{devices: string}>;
  startBluetoothDiscovery: () => void;
  startBluetoothLEDiscovery: (scanDurationSeconds: number) => Promise<string>;
  connectDevice: (deviceAddress: string) => Promise<{connection: boolean}>;
  connectLEDevice: (address: string) => Promise<string>;
  disconnectDevice: () => Promise<string>;
  disconnectLEDevice: () => Promise<string>;
  EVENT_BLUETOOTH_STATE: string = 'EVENT_BLUETOOTH_STATE';
  EVENT_DEVICE_DISCOVER_DONE: string = 'EVENT_DEVICE_DISCOVER_DONE';
  EVENT_DEVICE_FOUND: string = 'EVENT_DEVICE_FOUND';
}

declare module 'react-native' {
  interface NativeModulesStatic {
    BluetoothModule: BluetoothModuleInterface;
  }
}
