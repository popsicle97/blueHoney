import {ScrollView, StyleSheet, Text, View} from 'react-native';
import React, {useEffect} from 'react';
import BluetoothStatus from './components/BluetoothStatus';
import BluetoothDeviceList from './components/BluetoothDeviceList';
import useBluetooth from './hooks/bluetooth';
import {bluetoothRepo} from './utils/bluetoothRepo';
import Scanner from './components/scanner/Scanner';

// const FOUND_DEVICES: BluetoothDeviceT[] = [
//   {name: 'Amery', address: '00:11:22:33:FF:EE'},
//   {name: 'Amery', address: '00:11:22:33:FF:EE'},
//   {name: 'Amery', address: '00:11:22:33:FF:EE'},
// ];

function App() {
  // const {devices} = useDevices();
  const {
    bluetoothStatus,
    devices,
    setDevices,
    isScanning,
    connectDevice,
    connectLEDevice,
    connectedDevice,
    disconnectDevice,
    disconnectLEDevice,
  } = useBluetooth();

  useEffect(() => {}, []);

  if (typeof connectedDevice !== 'undefined' && bluetoothStatus) {
    return (
      <Scanner
        device={connectedDevice}
        onDisconnect={() => disconnectLEDevice()}
      />
    );
  }

  return (
    <ScrollView style={styles.main}>
      <BluetoothStatus
        bluetoothStatus={bluetoothStatus}
        bluetoothAction={() => bluetoothRepo.enableBluetooth()}
      />
      {bluetoothStatus ? (
        <BluetoothDeviceList
          devices={devices}
          onScan={() => {
            setDevices(oriDevices => {
              oriDevices.foundDevices = [];
              return {...oriDevices};
            });
            bluetoothRepo.startBluetoothLeDiscovery(10);
          }}
          onDeviceConnect={device => connectLEDevice(device)}
          isScanning={isScanning}
        />
      ) : (
        <View style={styles.main__bluetooth_off_container}>
          <Text style={styles.main__bluetooth_off_message}>
            Please turn bluetooth on
          </Text>
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  main: {
    paddingVertical: 0,
    backgroundColor: '#F5F5F5',
  },
  main__bluetooth_off_container: {
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: 250,
  },
  main__bluetooth_off_message: {
    fontSize: 18,
    fontWeight: '600',
    color: 'red',
  },
});

export default App;
