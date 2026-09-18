import React from 'react';
import { View, ViewProps } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

export function ScreenWrapper({ children, className = '', style, ...props }: ViewProps) {
  const insets = useSafeAreaInsets();

  return (
    <View
      className={`flex-1 bg-background ${className}`}
      style={[{ paddingTop: insets.top, paddingBottom: insets.bottom }, style]}
      {...props}
    >
      {children}
    </View>
  );
}
