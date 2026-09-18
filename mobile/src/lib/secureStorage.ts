import * as SecureStore from 'expo-secure-store';

export class SecureStorage {
  public static async setItem(key: string, value: string): Promise<void> {
    await SecureStore.setItemAsync(key, value);
  }

  public static async getItem(key: string): Promise<string | null> {
    return await SecureStore.getItemAsync(key);
  }

  public static async removeItem(key: string): Promise<void> {
    await SecureStore.deleteItemAsync(key);
  }
}
