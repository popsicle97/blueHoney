import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {BluetoothDeviceT} from '../interfaces/bluetooth';
import CustomButton from './CustomButton';

type BluetoothDevice = {
  device: BluetoothDeviceT;
  onDeviceConnect: (device: BluetoothDeviceT) => void;
};

const BluetoothDevice: React.FC<BluetoothDevice> = ({
  device,
  onDeviceConnect,
}) => {
  const _onDeviceConnect = () => {
    onDeviceConnect({...device});
  };
  return (
    <View style={styles.bluetoothDevice}>
      <View style={styles.bluetoothDevice__info}>
        <Text style={styles.bluetoothDevice__name}>{device.name}</Text>
        <Text style={styles.bluetoothDevice__address}>{device.address}</Text>
      </View>
      <CustomButton
        onPress={_onDeviceConnect}
        title="Connect"
        color="#0062cc"
        padding={0}
        textColor="white"
        fontWeight="400"
        fontSize={12}
        disabled={false}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  bluetoothDevice: {
    flexDirection: 'row',
    backgroundColor: 'white',
    borderWidth: 0.5,
    borderColor: 'lightgray',
    padding: 10,
  },
  bluetoothDevice__info: {
    flexDirection: 'column',
    flex: 1,
  },
  bluetoothDevice__name: {
    fontSize: 15,
    color: 'black',
  },
  bluetoothDevice__address: {
    fontSize: 13,
    color: 'gray',
    fontWeight: '400',
  },
});

export default BluetoothDevice;
