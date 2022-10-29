export const bluetoothErrorhandling = (error: any): string => {
  let errorString = '';
  if (typeof error !== 'undefined' || error) {
    if (typeof error === 'object') {
      errorString = error.message;
    } else if (typeof error === 'string') {
      errorString = error;
    }
  } else {
    console.error(error);
    return 'Error not indentifiable';
  }
  return errorString;
};
