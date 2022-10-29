import {StyleSheet, Text, View} from 'react-native';
import {BluetoothDeviceT} from '../interfaces/bluetooth';
import React from 'react';
import CustomButton from './CustomButton';
import BluetoothDevice from './BluetoothDevice';

type BluetoothDeviceList = {
  devices: {
    foundDevices: BluetoothDeviceT[];
    pairedDevices: BluetoothDeviceT[];
  };
  onScan: () => void;
  isScanning: boolean;
  onDeviceConnect: (device: BluetoothDeviceT) => void;
};

const BluetoothDeviceList: React.FC<BluetoothDeviceList> = ({
  devices,
  onScan,
  isScanning,
  onDeviceConnect,
}) => {
  return (
    <View style={styles.bluetoothDeviceList}>
      <View style={styles.bluetoothDeviceList__pairedDevices_list}>
        <View style={styles.bluetoothDeviceList__pairedDevices_list_header}>
          <Text
            style={styles.bluetoothDeviceList__pairedDevices_list_header_title}>
            Paired Devices
          </Text>
          <CustomButton
            onPress={onScan}
            title={isScanning ? 'Searching' : 'Search'}
            color=""
            padding={0}
            textColor="#0062cc"
            fontWeight="500"
            fontSize={18}
            disabled={isScanning}
          />
        </View>
        {devices.pairedDevices.map((pairedDevice, index) => (
          <BluetoothDevice
            onDeviceConnect={onDeviceConnect}
            device={pairedDevice}
            key={index}
          />
        ))}
      </View>
      <View style={styles.bluetoothDeviceList__foundDevices_list}>
        <View style={styles.bluetoothDeviceList__foundDevices_list_header}>
          <Text
            style={styles.bluetoothDeviceList__foundDevices_list_header_title}>
            Devices
          </Text>
        </View>
        {devices.foundDevices.map((foundDevice, index) => (
          <BluetoothDevice
            onDeviceConnect={onDeviceConnect}
            device={foundDevice}
            key={index}
          />
        ))}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  bluetoothDeviceList: {
    marginTop: 20,
  },
  bluetoothDeviceList__pairedDevices_list: {
    marginTop: 10,
  },
  bluetoothDeviceList__pairedDevices_list_header: {
    marginBottom: 10,
    paddingHorizontal: 10,
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  bluetoothDeviceList__pairedDevices_list_header_title: {
    fontSize: 17,
    fontWeight: '500',
  },
  bluetoothDeviceList__foundDevices_list: {
    marginTop: 20,
  },
  bluetoothDeviceList__foundDevices_list_header: {
    marginBottom: 10,
    paddingHorizontal: 10,
  },
  bluetoothDeviceList__foundDevices_list_header_title: {
    fontSize: 17,
    fontWeight: '500',
  },
});

export default BluetoothDeviceList;
