package se.yverling.wearto.mobile.data.token.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CryptoManager @Inject constructor() {
    private val keyStore = KeyStore.getInstance(KEY_STORE_NAME).apply {
        load(null)
    }

    fun encrypt(bytes: ByteArray, outputStream: DataOutputStream): ByteArray {
        val encryptCipher = getEncryptCipher()
        val encryptedBytes = encryptCipher.doFinal(bytes)

        outputStream.use {
            it.write(encryptCipher.iv.size)
            it.write(encryptCipher.iv)
            it.writeInt(encryptedBytes.size)
            it.write(encryptedBytes)
        }

        return encryptedBytes
    }

    fun decrypt(inputStream: DataInputStream): ByteArray {
        return inputStream.use {
            val ivSize = it.read()
            val iv = ByteArray(ivSize)
            it.read(iv)

            val encryptedBytesSize = it.readInt()
            val encryptedBytes = ByteArray(encryptedBytesSize)
            it.read(encryptedBytes)

            getDecryptCipherForIv(iv).doFinal(encryptedBytes)
        }
    }

    private fun getEncryptCipher(): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getKey())
        }
    }

    private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(SECRET_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    SECRET_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

        private const val KEY_STORE_NAME = "AndroidKeyStore"
        private const val SECRET_KEY_ALIAS = "WearToSecretKey"
    }
}
