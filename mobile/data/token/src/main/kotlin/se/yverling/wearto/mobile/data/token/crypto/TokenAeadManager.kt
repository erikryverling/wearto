package se.yverling.wearto.mobile.data.token.crypto

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TokenAeadManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val aead: Aead by lazy {
        AeadConfig.register()
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(KeyTemplates.get(KEY_TEMPLATE_NAME))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
        keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    companion object {
        const val KEYSET_NAME = "wearto_token_keyset"
        const val PREF_FILE_NAME = "wearto_token_keyset_prefs"
        const val MASTER_KEY_URI = "android-keystore://wearto_token_master_key"
        private const val KEY_TEMPLATE_NAME = "AES256_GCM"
    }
}
