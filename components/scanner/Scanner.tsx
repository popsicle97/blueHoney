import {View, StyleSheet, Text} from 'react-native';
import React from 'react';
import {BluetoothDeviceT} from '../../interfaces/bluetooth';
import CustomButton from '../CustomButton';
import Barcode from './Barcode';

type Scanner = {
  device: BluetoothDeviceT;
  onDisconnect: () => void;
};

const Scanner: React.FC<Scanner> = ({device, onDisconnect}) => {
  return (
    <View style={styles.scanner}>
      <View style={styles.scanner__bluetooth}>
        <View style={styles.scanner__bluetooth__device}>
          <Text style={styles.scanner__bluetooth__device_name}>
            {device.name}
          </Text>
          <Text>{device.address}</Text>
        </View>
        <CustomButton
          title="Disconnect"
          color="red"
          textColor="white"
          onPress={onDisconnect}
          padding={0}
          fontWeight={''}
          fontSize={15}
          disabled={false}
        />
      </View>
      {/* <Barcode /> */}
    </View>
  );
};

export const styles = StyleSheet.create({
  scanner: {
    // backgroundColor: 'black',
    minHeight: '100%',
    margin: 10,
  },
  scanner__bluetooth: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    backgroundColor: '#F3F3F3',
    borderWidth: 0.5,
    borderColor: 'lightgray',
    padding: 10,
  },
  scanner__bluetooth__device: {
    flex: 1,
  },
  scanner__bluetooth__device_name: {
    fontSize: 16,
    color: 'black',
    fontWeight: 'bold',
  },
  scannerInput: {
    backgroundColor: 'white',
    borderWidth: 1,
    marginTop: 100,
    borderRadius: 2,
  },
});

export default Scanner;
