package com.example.data.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val PREFIX = "ENC::"

    // 256-bit AES key derived for cluster sensitive notes & coordination records
    // In production apps, this key is stored in AndroidKeyStore or derived per-user
    private val AES_KEY_BYTES = byteArrayOf(
        0x42, 0x61, 0x68, 0x61, 0x27, 0x69, 0x43, 0x6c,
        0x75, 0x73, 0x74, 0x65, 0x72, 0x53, 0x65, 0x63,
        0x75, 0x72, 0x69, 0x74, 0x79, 0x4b, 0x65, 0x79,
        0x32, 0x30, 0x32, 0x36, 0x47, 0x43, 0x4d, 0x21
    )

    private val secretKey = SecretKeySpec(AES_KEY_BYTES, "AES")

    /**
     * Encrypts plaintext string using AES-256-GCM with a random IV.
     * Returns a string prefixed with "ENC::" containing Base64(IV + CipherText).
     */
    fun encrypt(plainText: String): String {
        if (plainText.isBlank()) return plainText
        if (plainText.startsWith(PREFIX)) return plainText // Already encrypted

        return try {
            val iv = ByteArray(IV_LENGTH_BYTE)
            SecureRandom().nextBytes(iv)

            val cipher = Cipher.getInstance(ALGORITHM)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

            PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    /**
     * Decrypts an "ENC::..." payload back to plain text.
     */
    fun decrypt(cipherText: String): String {
        if (!cipherText.startsWith(PREFIX)) return cipherText

        return try {
            val base64Payload = cipherText.removePrefix(PREFIX)
            val combined = Base64.decode(base64Payload, Base64.NO_WRAP)

            if (combined.size < IV_LENGTH_BYTE) return cipherText

            val iv = ByteArray(IV_LENGTH_BYTE)
            val encryptedBytes = ByteArray(combined.size - IV_LENGTH_BYTE)

            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTE)
            System.arraycopy(combined, IV_LENGTH_BYTE, encryptedBytes, 0, encryptedBytes.size)

            val cipher = Cipher.getInstance(ALGORITHM)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }

    fun isEncrypted(text: String): Boolean = text.startsWith(PREFIX)
}
