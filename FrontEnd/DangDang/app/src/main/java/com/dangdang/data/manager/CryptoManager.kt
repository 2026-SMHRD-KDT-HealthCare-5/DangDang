package com.dangdang.data.manager

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor() {

    companion object {
        private const val KEY_ALIAS = "AppPrefsKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    private fun getOrCreateSecretKey(): SecretKey {

        val keyStore = java.security.KeyStore.getInstance(
            ANDROID_KEYSTORE
        ).apply {
            load(null)
        }

        if (keyStore.containsAlias(KEY_ALIAS)) {
            return (keyStore.getEntry(
                KEY_ALIAS,
                null
            ) as java.security.KeyStore.SecretKeyEntry).secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(
                KeyProperties.BLOCK_MODE_GCM
            )
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)

        return keyGenerator.generateKey()
    }

    fun encrypt(value: String): String {

        if (value.isEmpty()) {
            return ""
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateSecretKey()
        )

        val encrypted = cipher.doFinal(
            value.toByteArray(StandardCharsets.UTF_8)
        )

        val iv = cipher.iv

        val combined = ByteArray(
            iv.size + encrypted.size
        )

        System.arraycopy(
            iv,
            0,
            combined,
            0,
            iv.size
        )

        System.arraycopy(
            encrypted,
            0,
            combined,
            iv.size,
            encrypted.size
        )

        return Base64.encodeToString(
            combined,
            Base64.NO_WRAP
        )
    }

    fun decrypt(value: String): String {

        if (value.isEmpty()) {
            return ""
        }

        val combined = Base64.decode(
            value,
            Base64.NO_WRAP
        )

        val ivSize = 12

        val iv = combined.copyOfRange(
            0,
            ivSize
        )

        val encrypted = combined.copyOfRange(
            ivSize,
            combined.size
        )

        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(
                128,
                iv
            )
        )

        val decrypted = cipher.doFinal(
            encrypted
        )

        return String(
            decrypted,
            StandardCharsets.UTF_8
        )
    }
}