import React from 'react';
import { View, Text } from 'react-native';

export default function ScanQrScreen() {
  return (
    <View className="flex-1 bg-background justify-center items-center px-4">
      <Text className="text-white text-xl font-bold font-mono">Scan Any UPI QR</Text>
      <Text className="text-slate-400 text-xs mt-2 text-center">
        Point camera at BharatQR, dynamic invoice QR, or merchant code
      </Text>
    </View>
  );
}
