import React from 'react';
import { View, Text } from 'react-native';

export default function FaceMatchScreen() {
  return (
    <View className="flex-1 bg-background justify-center items-center px-6">
      <Text className="text-white text-xl font-bold font-mono">Biometric Liveness Match</Text>
      <Text className="text-slate-400 text-xs mt-2 text-center">
        Position your face within the frame to complete Tier 2 verification
      </Text>
    </View>
  );
}
