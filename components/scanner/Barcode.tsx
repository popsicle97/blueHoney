'use strict';
import React, {useState} from 'react';
import {Text, TextStyle} from 'react-native';
import {
  Button,
  StyleSheet,
  TextInput,
  View,
  Switch,
  // Image,
  Keyboard,
} from 'react-native';
// import {BluetoothEscposPrinter} from 'react-native-bluetooth-escpos-printer';
// import {printCPCLBarcode} from './utils/bluetoothRepo';
// import Barcode from '@kichiyaki/react-native-barcode-generator';
// import ViewShot, {captureRef} from 'react-native-view-shot';

const Barcode: React.FC = () => {
  const [barcode, setBarcode] = useState('');
  const [inputFocus, setInputFocus] = useState(false);
  const [scannedValues, setScannedValues] = useState([]);
  const [isEnabled, setIsEnabled] = useState(true);

  const toggleSwitch = () => {
    setIsEnabled(previousState => !previousState);
    !isEnabled && Keyboard.dismiss();
    setBarcode('');
  };

  const onChangeTextScan = async (barCode: string) => {
    // if (!isConnected) {
    //   return 'No device detected';
    // }
    const _clonedScannedValues = [...scannedValues];
    // const isPrinted = await printCPCLBarcode(barCode);
    // if (isPrinted) {
    //   _clonedScannedValues.push(barCode);
    //   setScannedValues(_clonedScannedValues);
    //   setBarcode('');
    // }
  };

  const onChangeTextInput = async (barCode: string) => {
    setBarcode(barCode);
  };

  const isScanToPrint = () => {
    // if (!isConnected) {
    //   return 'No device detected';
    // }
    if (isEnabled) {
      return inputFocus
        ? 'Now scan with built in scanner'
        : 'Tap here to register scanner value';
    }
    return inputFocus ? 'Type in barcode' : 'Tap here to key in barcode';
  };

  // const onCapture = useCallback(async uri => {
  //   // console.log('tmp file', uri.toString('base64'));
  //   await printEscLabel(uri);
  // }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.h1}>
        For scanner to register, please tap on input below
      </Text>
      <View style={styles.switch}>
        <Text>Scan to print</Text>
        <Switch
          trackColor={{false: '#767577', true: '#81b0ff'}}
          thumbColor={isEnabled ? '#0062cc' : '#f4f3f4'}
          onValueChange={toggleSwitch}
          value={isEnabled}
        />
        ƒ
      </View>
      <TextInput
        pointerEvents="none"
        placeholder={isScanToPrint()}
        value={barcode}
        onChangeText={text =>
          isEnabled ? onChangeTextScan(text) : onChangeTextInput(text)
        }
        onFocus={() => setInputFocus(true)}
        onBlur={() => setInputFocus(false)}
        style={styles.scanInput(inputFocus)}
        showSoftInputOnFocus={!isEnabled}
        placeholderTextColor={'black'}
        autoCapitalize={'characters'}

        // editable={!isEnabled}
      />
      <Text style={styles.recentScanned}>
        Recent scan :{' '}
        {scannedValues[scannedValues.length - 1]
          ? scannedValues[scannedValues.length - 1]
          : 'N/A'}
      </Text>
      {!isEnabled && (
        <View>
          <Button
            onPress={() => onChangeTextScan(barcode)}
            color="green"
            title="Generate barcode"
          />
        </View>
      )}
      {/* <View style={styles.historyBtn}>
        <Button onPress={onPress} color="black" title="Show history" />
      </View> */}
    </View>
  );
};

export default Barcode;

const styles = {
  container: {
    borderTopWidth: 1,
    borderColor: 'gray',
    paddingTop: 25,
    marginVertical: 10,
  },
  h1: {
    fontSize: 17,
    fontWeight: 'bold',
    marginBottom: 5,
  } as TextStyle,
  switch: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    justifyContent: 'flex-end',
    display: 'flex',
    marginVertical: 5,
  } as TextStyle,
  recentScanned: {
    marginVertical: 5,
    marginBottom: 10,
  },
  scanInput: (isFocused: boolean) => {
    let initialStyle = {
      borderWidth: 1,
      padding: 25,
      borderRadius: 3,
      fontSize: 15,
    };
    if (isFocused) {
      return {
        ...initialStyle,
        borderColor: 'green',
        backgroundColor: '#cefad0',
      };
    }
    return {...initialStyle, borderColor: 'red', backgroundColor: '#ffc9bb'};
  },
};
