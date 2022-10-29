import {Text, TouchableOpacity} from 'react-native';
import React from 'react';

const CustomButton: React.FC<{
  onPress: () => void;
  title: string;
  color: string;
  padding: number;
  textColor: string;
  fontWeight: string;
  fontSize: number;
  disabled: boolean;
}> = ({
  onPress,
  title,
  color,
  padding,
  textColor = 'white',
  fontWeight = 'normal',
  fontSize = 14,
  disabled = false,
}) => {
  return (
    <TouchableOpacity
      onPress={onPress}
      style={styles.button(color, padding)}
      disabled={disabled}>
      <Text style={styles.button__text(textColor, fontSize, fontWeight)}>
        {title}
      </Text>
    </TouchableOpacity>
  );
};

const styles = {
  button: (color: string, padding: number): any => ({
    backgroundColor: color,
    paddingHorizontal: 10,
    padding: padding,
    justifyContent: 'center',
    borderRadius: 4,
  }),
  button__text: (
    textColor: string,
    fontSize: number,
    fontWeight: string,
  ): any => ({
    color: textColor,
    fontSize: fontSize,
    fontWeight: fontWeight,
  }),
};

export default CustomButton;
