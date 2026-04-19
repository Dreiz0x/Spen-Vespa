package dev.vskelk.cdf.core.datastore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CipherService - Cifrado de API Keys con AES/GCM
 *
 * Per spec: "API keys cifradas con AES/GCM vía Android Keystore"
 *
 * Las API keys nunca se almacenan en texto plano.
 * Se cifran con una clave almacenada en el Android Keystore,
 * que es hardware-backed en dispositivos compatibles.
 */
@Singleton
class CipherService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "vespa_api_key_encryption_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    init {
        // Generar clave si no existe
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // Para acceso offline
            .build()

        keyGenerator.init(keySpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    /**
     * Cifra un texto (como API key) usando AES/GCM
     * @param plainText Texto a cifrar
     * @return CipherResult con IV + ciphertext codificados en Base64
     */
    fun encrypt(plainText: String): CipherResult {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return CipherResult(
            iv = iv,
            ciphertext = encryptedBytes
        )
    }

    /**
     * Descifra un texto previamente cifrado
     * @param iv Vector de inicialización usado en el cifrado
     * @param ciphertext Texto cifrado
     * @return Texto original
     */
    fun decrypt(iv: ByteArray, ciphertext: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val decryptedBytes = cipher.doFinal(ciphertext)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Descifra un resultado de cifrado completo
     */
    fun decrypt(result: CipherResult): String {
        return decrypt(result.iv, result.ciphertext)
    }

    /**
     * Verifica si la clave de cifrado está disponible
     */
    fun isKeyAvailable(): Boolean {
        return try {
            keyStore.containsAlias(KEY_ALIAS) && getSecretKey() != null
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Resultado del cifrado AES/GCM
 */
data class CipherResult(
    /** Vector de inicialización (12 bytes) */
    val iv: ByteArray,
    /** Texto cifrado */
    val ciphertext: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CipherResult

        if (!iv.contentEquals(other.iv)) return false
        if (!ciphertext.contentEquals(other.ciphertext)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + ciphertext.contentHashCode()
        return result
    }

    /**
     * Codifica el resultado a formato almacenable
     * Formato: Base64(IV) + ":" + Base64(Ciphertext)
     */
    fun toStorageString(): String {
        val ivBase64 = android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP)
        val cipherBase64 = android.util.Base64.encodeToString(ciphertext, android.util.Base64.NO_WRAP)
        return "$ivBase64:$cipherBase64"
    }

    companion object {
        /**
         * Decodifica desde formato almacenable
         */
        fun fromStorageString(storageString: String): CipherResult {
            val parts = storageString.split(":")
            require(parts.size == 2) { "Invalid storage format" }
            val iv = android.util.Base64.decode(parts[0], android.util.Base64.NO_WRAP)
            val ciphertext = android.util.Base64.decode(parts[1], android.util.Base64.NO_WRAP)
            return CipherResult(iv, ciphertext)
        }
    }
}
