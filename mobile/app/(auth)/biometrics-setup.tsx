import React from 'react';
import { View, Text } from 'react-native';

export default function BiometricsSetupScreen() {
  return (
    <View className="flex-1 bg-background justify-center items-center px-6">
      <Text className="text-white text-2xl font-bold font-mono">Biometric Security</Text>
      <Text className="text-slate-400 text-sm mt-2">Enable FaceID or Fingerprint for high-speed authorization</Text>
    </View>
  );
}
