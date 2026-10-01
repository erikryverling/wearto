package se.yverling.wearto.mobile.data.token

import androidx.datastore.core.CorruptionException
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.InternalSerializationApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import se.yverling.wearto.mobile.data.token.datastore.LegacyTokenDataSource
import se.yverling.wearto.mobile.data.token.datastore.StoredTokenState
import se.yverling.wearto.mobile.data.token.datastore.TokenDataSource
import se.yverling.wearto.mobile.data.token.datastore.TokenDataSourceImpl
import java.io.File
import java.io.IOException

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
    fun `upgrading with valid legacy credential should transfer token to AEAD store and retire legacy data`() = runTest {
        val legacyToken = "legacy_secret_token_123"
        fakeLegacyDataSource.tokenState.value = legacyToken
        val repository = createRepository()

        repository.getToken().first() shouldBe legacyToken
        repository.hasToken().first().shouldBeTrue()

        // Verifies AEAD store now durably holds the transferred credential
        tokenFile.exists().shouldBeTrue()
        tokenFile.length() shouldBeGreaterThan 0L

        // Verifies legacy data was retired
        fakeLegacyDataSource.retireCalls shouldBe 1
        fakeLegacyDataSource.tokenState.value.shouldBeNull()
    }

    @Test
    fun `persisted AEAD token should survive process restart without re-running migration`() = runTest {
        val legacyToken = "legacy_secret_token_456"
        fakeLegacyDataSource.tokenState.value = legacyToken
        val repository = createRepository()

        repository.getToken().first() shouldBe legacyToken
        val callsAfterMigration = fakeLegacyDataSource.getLegacyTokenCalls

        // Simulate process restart
        val restartedRepository = createRepository()

        restartedRepository.getToken().first() shouldBe legacyToken
        restartedRepository.hasToken().first().shouldBeTrue()

        // Migration must not re-run because AEAD store is already initialized
        fakeLegacyDataSource.getLegacyTokenCalls shouldBe callsAfterMigration
    }

    @Test
    fun `upgrading with no legacy credential should remain disconnected without creating empty credential artifacts`() = runTest {
        fakeLegacyDataSource.tokenState.value = null
        val repository = createRepository()

        repository.getToken().first().shouldBeNull()
        repository.hasToken().first().shouldBeFalse()

        // Verifies no empty credential file is created
        tokenFile.exists().shouldBeFalse()
        fakeLegacyDataSource.retireCalls shouldBe 0
    }

    @Test
    fun `interrupted transfer should safely retry on next launch without losing credential`() = runTest {
        val legacyToken = "unlost_credential_789"
        fakeLegacyDataSource.tokenState.value = legacyToken

        // Simulate an interrupted/failed write on the first launch
        val failingTokenDataSource = FakeFailingTokenDataSource(failOnPersist = true)
        val failingRepository = TokenRepositoryImpl(
            tokenDataSource = failingTokenDataSource,
            legacyTokenDataSource = fakeLegacyDataSource,
        )

        shouldThrow<IOException> {
            failingRepository.getToken().first()
        }

        // Verifies legacy data was NOT retired and is still intact
        fakeLegacyDataSource.retireCalls shouldBe 0
        fakeLegacyDataSource.tokenState.value shouldBe legacyToken

        // Next launch: working DataStore completes transfer successfully
        val workingRepository = createRepository()
        workingRepository.getToken().first() shouldBe legacyToken

        fakeLegacyDataSource.retireCalls shouldBe 1
        fakeLegacyDataSource.tokenState.value.shouldBeNull()
    }

    @Test
    fun `logout after migration should permanently remove credential and prevent resurrection`() = runTest {
        fakeLegacyDataSource.tokenState.value = "token_to_logout"
        val repository = createRepository()

        repository.getToken().first() shouldBe "token_to_logout"
        repository.clearToken()

        repository.getToken().first().shouldBeNull()
        repository.hasToken().first().shouldBeFalse()

        // Even if legacy data is forcefully resurrected on disk, AEAD Absent state prevents resurrecting
        fakeLegacyDataSource.tokenState.value = "resurrected_ghost_token"
        val restartedRepository = createRepository()

        restartedRepository.getToken().first().shouldBeNull()
        restartedRepository.hasToken().first().shouldBeFalse()
    }

    @Test
    fun `transient legacy read error should preserve legacy data and allow retry on next launch`() = runTest {
        val legacyToken = "transient_token"
        fakeLegacyDataSource.tokenState.value = legacyToken
        fakeLegacyDataSource.shouldFailOnGet = IOException("Temporary I/O error")

        val repository = createRepository()

        shouldThrow<IOException> {
            repository.getToken().first()
        }

        // Legacy data preserved, not retired
        fakeLegacyDataSource.retireCalls shouldBe 0
        fakeLegacyDataSource.tokenState.value shouldBe legacyToken

        // Resolved transient error on next launch
        fakeLegacyDataSource.shouldFailOnGet = null
        val nextLaunchRepository = createRepository()

        nextLaunchRepository.getToken().first() shouldBe legacyToken
        fakeLegacyDataSource.retireCalls shouldBe 1
    }

    @Test
    fun `corrupted legacy data should throw UnrecoverableTokenException and preserve legacy data`() = runTest {
        fakeLegacyDataSource.tokenState.value = "corrupted_token"
        fakeLegacyDataSource.shouldFailOnGet = CorruptionException("Legacy decryption failed")

        val repository = createRepository()

        shouldThrow<UnrecoverableTokenException> {
            repository.getToken().first()
        }

        // Legacy data is not retired or deleted prematurely on corruption
        fakeLegacyDataSource.retireCalls shouldBe 0
        fakeLegacyDataSource.tokenState.value shouldBe "corrupted_token"
    }

    @Test
    fun `corrupted AEAD data should throw UnrecoverableTokenException and preserve stored data`() = runTest {
        // Write garbage bytes to tokenFile to simulate disk corruption
        tokenFile.writeBytes(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08))

        val repository = createRepository()

        shouldThrow<UnrecoverableTokenException> {
            repository.getToken().first()
        }

        // File remains on disk rather than being deleted or overwritten with null
        tokenFile.exists().shouldBeTrue()
        tokenFile.length() shouldBeGreaterThan 0L
    }

    @Test
    fun `AEAD data with wrong key should throw UnrecoverableTokenException and preserve stored data`() = runTest {
        val repository = createRepository()
        repository.setToken("valid_token")

        val wrongAead = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)

        val wrongKeyRepository = createRepository(aead = wrongAead)

        shouldThrow<UnrecoverableTokenException> {
            wrongKeyRepository.getToken().first()
        }

        // File remains on disk
        tokenFile.exists().shouldBeTrue()
        tokenFile.length() shouldBeGreaterThan 0L
    }

    @Test
    fun `concurrent collectors during migration should all receive token and run migration once`() = runTest {
        val legacyToken = "concurrent_token"
        fakeLegacyDataSource.tokenState.value = legacyToken
        val repository = createRepository()

        val token1 = async { repository.getToken().first() }
        val token2 = async { repository.getToken().first() }
        val hasToken = async { repository.hasToken().first() }

        token1.await() shouldBe legacyToken
        token2.await() shouldBe legacyToken
        hasToken.await().shouldBeTrue()

        fakeLegacyDataSource.retireCalls shouldBe 1
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
    fun `clearToken should write non-empty ciphertext to disk rather than an empty file`() = runTest {
        val repository = createRepository()
        repository.setToken("some_token")
        repository.clearToken()

        // Explicitly verifies that logout does not rely on an empty stream (file is valid AEAD ciphertext)
        tokenFile.exists().shouldBeTrue()
        tokenFile.length() shouldBeGreaterThan 0L
    }
}

private class FakeLegacyTokenDataSource(
    initialToken: String? = null,
) : LegacyTokenDataSource {
    val tokenState = MutableStateFlow(initialToken)
    var getLegacyTokenCalls = 0
    var retireCalls = 0
    var shouldFailOnGet: Throwable? = null

    override val tokenFlow: Flow<String?> = tokenState

    override suspend fun getLegacyToken(): String? {
        getLegacyTokenCalls++
        shouldFailOnGet?.let { throw it }
        return tokenState.value
    }

    override suspend fun clearToken() {
        tokenState.value = null
    }

    override suspend fun retireLegacyData() {
        retireCalls++
        tokenState.value = null
    }
}

@OptIn(InternalSerializationApi::class)
private class FakeFailingTokenDataSource(
    private val failOnPersist: Boolean,
) : TokenDataSource {
    private val state = MutableStateFlow<StoredTokenState>(StoredTokenState.Unset)
    override val stateFlow: Flow<StoredTokenState> = state
    override val tokenFlow: Flow<String?> = MutableStateFlow(null)

    override suspend fun persistToken(token: String) {
        if (failOnPersist) {
            throw IOException("Simulated write failure during persistToken")
        }
        state.value = StoredTokenState.Present(token)
    }

    override suspend fun clearToken() {
        state.value = StoredTokenState.Absent
    }
}
