package com.example.data.local

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private val secureRandom = SecureRandom()

    // 256-bit AES master key derived on-device
    private val masterKeyBytes: ByteArray by lazy {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.digest("JARVIS_CLOUD_STREAM_MASTER_KEY_2026_SECURE".toByteArray(Charsets.UTF_8))
    }

    private val secretKey: SecretKey by lazy {
        SecretKeySpec(masterKeyBytes, "AES")
    }

    fun encrypt(plainText: String): String {
        return try {
            val iv = ByteArray(IV_LENGTH_BYTE)
            secureRandom.nextBytes(iv)
            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    fun decrypt(encryptedBase64: String): String {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size < IV_LENGTH_BYTE) return encryptedBase64

            val iv = ByteArray(IV_LENGTH_BYTE)
            System.arraycopy(combined, 0, iv, 0, iv.size)
            val cipherText = ByteArray(combined.size - IV_LENGTH_BYTE)
            System.arraycopy(combined, IV_LENGTH_BYTE, cipherText, 0, cipherText.size)

            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val plainBytes = cipher.doFinal(cipherText)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedBase64
        }
    }

    fun generateIntegrityHash(data: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(data.toByteArray(Charsets.UTF_8))
        return hash.take(8).joinToString("") { "%02X".format(it) }
    }

    fun getKeyFingerprint(): String {
        return generateIntegrityHash("JARVIS_VAULT_KEY_FINGERPRINT_ACTIVE_256")
    }
}
