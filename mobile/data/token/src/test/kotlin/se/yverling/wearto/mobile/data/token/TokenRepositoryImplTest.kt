package se.yverling.wearto.mobile.data.token

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import se.yverling.wearto.mobile.data.token.datastore.LegacyTokenDataSource
import se.yverling.wearto.mobile.data.token.datastore.TokenDataSourceImpl
import java.io.File

private class TokenRepositoryImplTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var tokenFile: File
    private lateinit var testAead: Aead
    private lateinit var fakeLegacyDataSource: FakeLegacyTokenDataSource
    private var activeScope: CoroutineScope? = null

    @BeforeEach
    fun setUp() {
        AeadConfig.register()
        testAead = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        tokenFile = File(tempDir, TokenDataSourceImpl.DATASTORE_FILE_NAME)
        fakeLegacyDataSource = FakeLegacyTokenDataSource()
    }

    @AfterEach
    fun tearDown() {
        activeScope?.cancel()
    }

    private fun createRepository(
        file: File = tokenFile,
        aead: Aead = testAead,
        legacyDataSource: LegacyTokenDataSource = fakeLegacyDataSource,
    ): TokenRepositoryImpl {
        activeScope?.cancel()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        activeScope = scope
        val dataStore = TokenDataSourceImpl.createDataStore(
            aead = aead,
            scope = scope,
            produceFile = { file },
        )
        val tokenDataSource = TokenDataSourceImpl(dataStore)
        return TokenRepositoryImpl(
            tokenDataSource = tokenDataSource,
            legacyTokenDataSource = legacyDataSource,
        )
    }

    @Test
    fun `setToken should store credential using AEAD encrypted storage and emit it via getToken`() = runTest {
        val repository = createRepository()
        val token = "todoist_secret_token_123"

        repository.setToken(token)

        repository.getToken().first() shouldBe token
        repository.hasToken().first().shouldBeTrue()
        tokenFile.exists().shouldBeTrue()
        tokenFile.length() shouldBeGreaterThan 0L
    }

    @Test
    fun `launching repository with existing AEAD credential should load token across restart`() = runTest {
        val initialRepository = createRepository()
        val token = "persisted_token_456"
        initialRepository.setToken(token)

        // Simulate process restart by creating a new repository instance against the same file
        val restartedRepository = createRepository()

        restartedRepository.getToken().first() shouldBe token
        restartedRepository.hasToken().first().shouldBeTrue()
    }

    @Test
    fun `clearToken should explicitly persist absent state to AEAD store so token remains absent across restart`() = runTest {
        val repository = createRepository()
        repository.setToken("token_to_clear")

        repository.clearToken()

        repository.getToken().first().shouldBeNull()
        repository.hasToken().first().shouldBeFalse()

        // Simulate process restart
        val restartedRepository = createRepository()
        restartedRepository.getToken().first().shouldBeNull()
        restartedRepository.hasToken().first().shouldBeFalse()
    }

    @Test
    fun `clearToken should write non-empty ciphertext to disk rather than an empty file`() = runTest {
        val repository = createRepository()
        repository.setToken("some_token")
        repository.clearToken()

        // Explicitly verifies that logout does not rely on an empty stream (file is valid AEAD ciphertext)
        tokenFile.exists().shouldBeTrue()
        tokenFile.length() shouldBeGreaterThan 0L
    }

    @Test
    fun `clean install should emit null and hasToken false when both AEAD and legacy are absent`() = runTest {
        val repository = createRepository()

        repository.getToken().first().shouldBeNull()
        repository.hasToken().first().shouldBeFalse()
    }

    @Test
    fun `existing legacy connection should emit legacy token when AEAD store is unset`() = runTest {
        fakeLegacyDataSource.tokenState.value = "legacy_credential_789"
        val repository = createRepository()

        repository.getToken().first() shouldBe "legacy_credential_789"
        repository.hasToken().first().shouldBeTrue()
    }

    @Test
    fun `clearToken on existing legacy connection should persist absent state and prevent resurrecting legacy token on restart`() = runTest {
        fakeLegacyDataSource.tokenState.value = "legacy_credential_789"
        val repository = createRepository()

        repository.clearToken()

        repository.getToken().first().shouldBeNull()
        repository.hasToken().first().shouldBeFalse()

        // Even if legacy data were still somehow present, AEAD absent state takes precedence across restart
        fakeLegacyDataSource.tokenState.value = "legacy_credential_789"
        val restartedRepository = createRepository()

        restartedRepository.getToken().first().shouldBeNull()
        restartedRepository.hasToken().first().shouldBeFalse()
    }

    @Test
    fun `setToken on existing legacy connection should persist new credential to AEAD store and emit it across restart`() = runTest {
        fakeLegacyDataSource.tokenState.value = "old_legacy_token"
        val repository = createRepository()

        repository.setToken("new_aead_token")

        repository.getToken().first() shouldBe "new_aead_token"

        val restartedRepository = createRepository()
        restartedRepository.getToken().first() shouldBe "new_aead_token"
    }
}

private class FakeLegacyTokenDataSource(
    initialToken: String? = null,
) : LegacyTokenDataSource {
    val tokenState = MutableStateFlow(initialToken)
    override val tokenFlow: Flow<String?> = tokenState

    override suspend fun clearToken() {
        tokenState.value = null
    }
}
