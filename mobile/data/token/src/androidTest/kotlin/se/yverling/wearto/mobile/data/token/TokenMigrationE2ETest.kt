package se.yverling.wearto.mobile.data.token

import android.content.Context
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.crypto.tink.Aead
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import se.yverling.wearto.mobile.data.token.crypto.CryptoManager
import se.yverling.wearto.mobile.data.token.crypto.TokenAeadManager
import se.yverling.wearto.mobile.data.token.datastore.LegacyTokenDataSourceImpl
import se.yverling.wearto.mobile.data.token.datastore.TokenDataSourceImpl
import se.yverling.wearto.mobile.data.token.datastore.TokenDataStoreSerializer
import java.io.File

@RunWith(AndroidJUnit4::class)
class TokenMigrationE2ETest {

    private lateinit var context: Context
    private var activeScope: CoroutineScope? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        cleanupFiles()
    }

    @After
    fun tearDown() {
        activeScope?.cancel()
        cleanupFiles()
    }

    private fun cleanupFiles() {
        val legacyFile = context.preferencesDataStoreFile("token")
        if (legacyFile.exists()) legacyFile.delete()

        val aeadFile = context.dataStoreFile("token_v1.json")
        if (aeadFile.exists()) aeadFile.delete()
    }

    private fun createTokenDataSource(aead: Aead): TokenDataSourceImpl {
        activeScope?.cancel()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        activeScope = scope
        val dataStore = TokenDataSourceImpl.createDataStore(
            aead = aead,
            scope = scope,
            produceFile = { context.dataStoreFile("token_v1.json") },
        )
        return TokenDataSourceImpl(dataStore)
    }

    @Test
    fun accountSignedInOnPreMigrationVersionSurvivesUpgradeRestartAndLogout() = runBlocking {
        // 1. Write credential using the real AndroidKeyStore AES-CBC legacy serializer
        val cryptoManager = CryptoManager()
        val legacySerializer = TokenDataStoreSerializer(cryptoManager)
        val legacyFile = context.preferencesDataStoreFile("token")
        legacyFile.parentFile?.mkdirs()

        val testToken = "e2e_legacy_todoist_token_999"
        legacySerializer.writeTo(testToken, legacyFile.outputStream())

        assertTrue("Legacy file should exist with ciphertext", legacyFile.exists() && legacyFile.length() > 0L)

        // 2. Initialize upgraded app repository with real Tink Keystore AEAD
        val aeadManager = TokenAeadManager(context)
        val aead = aeadManager.aead
        val tokenDataSource = createTokenDataSource(aead)
        val legacyDataSource = LegacyTokenDataSourceImpl(context, legacySerializer)
        val repository = TokenRepositoryImpl(tokenDataSource, legacyDataSource)

        // 3. Verify the token is migrated seamlessly and returned on first access
        assertEquals(testToken, repository.getToken().first())
        assertTrue(repository.hasToken().first())

        // 4. Verify the AEAD file now exists with encrypted token and legacy data is retired
        val aeadFile = context.dataStoreFile("token_v1.json")
        assertTrue("AEAD file should exist after migration", aeadFile.exists() && aeadFile.length() > 0L)
        assertFalse("Legacy file should be retired after migration", legacyFile.exists())

        // 5. Simulate process restart by recreating repository with a fresh scope
        val restartedTokenDataSource = createTokenDataSource(aead)
        val restartedLegacyDataSource = LegacyTokenDataSourceImpl(context, legacySerializer)
        val restartedRepository = TokenRepositoryImpl(restartedTokenDataSource, restartedLegacyDataSource)

        // Verify token survives process restart
        assertEquals(testToken, restartedRepository.getToken().first())
        assertTrue(restartedRepository.hasToken().first())

        // 6. Log out
        restartedRepository.clearToken()
        assertNull(restartedRepository.getToken().first())
        assertFalse(restartedRepository.hasToken().first())

        // 7. Simulate second process restart after logout
        val postLogoutTokenDataSource = createTokenDataSource(aead)
        val postLogoutLegacyDataSource = LegacyTokenDataSourceImpl(context, legacySerializer)
        val postLogoutRepository = TokenRepositoryImpl(postLogoutTokenDataSource, postLogoutLegacyDataSource)

        // Verify user remains logged out across restart
        assertNull(postLogoutRepository.getToken().first())
        assertFalse(postLogoutRepository.hasToken().first())
    }
}
