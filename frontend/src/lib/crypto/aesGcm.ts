/**
 * Native Browser WebCrypto AES-256-GCM Authenticated Encryption
 * Encrypts Aadhaar numbers, PAN, and bank accounts prior to network transmission.
 */

export class AesGcmCrypto {
  /**
   * Generates a 256-bit AES-GCM session key.
   */
  public static async generateKey(): Promise<CryptoKey> {
    return window.crypto.subtle.generateKey(
      {
        name: 'AES-GCM',
        length: 256,
      },
      true,
      ['encrypt', 'decrypt']
    );
  }

  /**
   * Encrypts plaintext string using AES-256-GCM with a fresh 12-byte IV.
   * Returns Base64 string containing: IV (12 bytes) + Ciphertext + Tag (16 bytes).
   */
  public static async encrypt(key: CryptoKey, plaintext: string): Promise<string> {
    const encoder = new TextEncoder();
    const encodedData = encoder.encode(plaintext);

    // 96-bit (12 bytes) Initialization Vector
    const iv = window.crypto.getRandomValues(new Uint8Array(12));

    const ciphertextWithTag = await window.crypto.subtle.encrypt(
      {
        name: 'AES-GCM',
        iv,
        tagLength: 128,
      },
      key,
      encodedData
    );

    // Concatenate IV + Ciphertext (with tag)
    const combined = new Uint8Array(iv.length + ciphertextWithTag.byteLength);
    combined.set(iv, 0);
    combined.set(new Uint8Array(ciphertextWithTag), iv.length);

    return this.uint8ArrayToBase64(combined);
  }

  private static uint8ArrayToBase64(bytes: Uint8Array): string {
    let binary = '';
    const len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary);
  }
}
