import {useEffect, useState} from 'react';
import {BluetoothDeviceT} from '../interfaces/bluetooth';
import {bluetoothRepo} from '../utils/bluetoothRepo';

export const useDevices = () => {
  const [devices, setDevices] = useState<{
    foundDevices: BluetoothDeviceT[];
    pairedDevices: BluetoothDeviceT[];
  }>({foundDevices: [], pairedDevices: []});

  useEffect(() => {
    const getPairedDevices = async () => {
      const pairedDevices: BluetoothDeviceT[] =
        await bluetoothRepo.getPairedDevices();
      setDevices(oriDevices => {
        oriDevices.pairedDevices = [...pairedDevices];
        return oriDevices;
      });
    };
    getPairedDevices();
  }, []);

  return {devices};
};
