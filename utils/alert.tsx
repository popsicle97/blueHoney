import {Alert} from 'react-native';

export const rnAlert = (
  title: string,
  message?: string,
  negativeButtonText: string = 'Cancel',
  positiveButtonText: string = 'Confirm',
) => {
  return new Promise(resolve => {
    Alert.alert(title, message, [
      {text: negativeButtonText, onPress: () => resolve(false)},
      {
        text: positiveButtonText,
        onPress: () => resolve(true),
      },
    ]);
  });
};
