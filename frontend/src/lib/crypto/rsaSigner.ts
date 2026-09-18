/**
 * Native Browser WebCrypto RSA-2048 Digital Signer
 * Implements non-repudiation signatures conforming to UPI Mesh Payment Service requirements.
 */

export interface KeyPairResult {
  publicKeyBase64: string;
  privateKey: CryptoKey;
}

export class RsaSigner {
  /**
   * Generates a 2048-bit RSA-PSS keypair for non-repudiation digital signatures.
   */
  public static async generateKeyPair(): Promise<KeyPairResult> {
    if (typeof window === 'undefined' || !window.crypto || !window.crypto.subtle) {
      throw new Error('WebCrypto API is not available in current environment');
    }

    const keyPair = await window.crypto.subtle.generateKey(
      {
        name: 'RSA-PSS',
        modulusLength: 2048,
        publicExponent: new Uint8Array([1, 0, 1]), // 65537
        hash: 'SHA-256',
      },
      true,
      ['sign', 'verify']
    );

    const exportedPublic = await window.crypto.subtle.exportKey('spki', keyPair.publicKey);
    const publicKeyBase64 = this.arrayBufferToBase64(exportedPublic);

    return {
      publicKeyBase64,
      privateKey: keyPair.privateKey,
    };
  }

  /**
   * Signs canonical transaction data: sender|receiver|amount|timestamp|nonce
   */
  public static async signTransactionPayload(
    privateKey: CryptoKey,
    canonicalPayload: string
  ): Promise<string> {
    const encoder = new TextEncoder();
    const data = encoder.encode(canonicalPayload);

    const signature = await window.crypto.subtle.sign(
      {
        name: 'RSA-PSS',
        saltLength: 32,
      },
      privateKey,
      data
    );

    return this.arrayBufferToBase64(signature);
  }

  /**
   * Generates a cryptographically random 128-bit nonce.
   */
  public static generateNonce(): string {
    const array = new Uint8Array(16);
    if (typeof window !== 'undefined' && window.crypto) {
      window.crypto.getRandomValues(array);
    }
    return Array.from(array, (byte) => byte.toString(16).padStart(2, '0')).join('');
  }

  private static arrayBufferToBase64(buffer: ArrayBuffer): string {
    let binary = '';
    const bytes = new Uint8Array(buffer);
    const len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary);
  }
}
