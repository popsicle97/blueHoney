import {StyleSheet, Text, View} from 'react-native';
import React from 'react';
import CustomButton from './CustomButton';

type BluetoothStatus = {
  bluetoothStatus: boolean;
  bluetoothAction: () => void;
};

const BluetoothStatus: React.FC<BluetoothStatus> = props => {
  const isBluetoothStatusOn = (onValue: string, offValue: string) => {
    return props.bluetoothStatus ? onValue : offValue;
  };
  return (
    <View style={styles.bluetoothStatus__container}>
      <View style={styles.bluetoothStatus__action__container}>
        <Text style={styles.bluetoothStatus__action__text}>
          Please allow permission for bluetooth when prompted
        </Text>
        <CustomButton
          onPress={props.bluetoothAction}
          title={isBluetoothStatusOn('•' as string, 'Turn on')}
          color={isBluetoothStatusOn('white', '#F3F3F3')}
          padding={props.bluetoothStatus ? 0 : 5}
          textColor={isBluetoothStatusOn('green', 'red')}
          fontWeight="bold"
          fontSize={props.bluetoothStatus ? 40 : 15}
          disabled={false}
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  bluetoothStatus__container: {
    backgroundColor: 'white',
    padding: 10,
    borderBottomWidth: 0.5,
    borderTopWidth: 0.5,
    borderColor: 'lightgray',
  },
  bluetoothStatus__action__container: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  bluetoothStatus__action__text: {
    flex: 1,
  },
});

export default BluetoothStatus;
