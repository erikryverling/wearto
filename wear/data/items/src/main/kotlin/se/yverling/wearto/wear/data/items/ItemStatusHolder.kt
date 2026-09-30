package se.yverling.wearto.wear.data.items

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.yverling.wearto.wear.data.items.di.ApplicationScope
import se.yverling.wearto.wear.data.items.model.ItemState
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

interface ItemStatusHolder {
    val statuses: StateFlow<Map<String, ItemState>>
    fun setLoading(name: String)
    fun setConfirmed(name: String, isSuccess: Boolean)
    fun reset(name: String)
    fun resetAll()
}

internal class ItemStatusHolderImpl @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
) : ItemStatusHolder {
    private val _statuses = MutableStateFlow<Map<String, ItemState>>(emptyMap())
    override val statuses: StateFlow<Map<String, ItemState>> = _statuses.asStateFlow()

    private val jobs = ConcurrentHashMap<String, Job>()

    override fun setLoading(name: String) {
        jobs[name]?.cancel()
        _statuses.update { it + (name to ItemState.Loading) }

        jobs[name] = scope.launch {
            delay(LOADING_TIMEOUT_MS)
            _statuses.update { it + (name to ItemState.Error) }
            delay(CONFIRMATION_DELAY_MS)
            _statuses.update { it - name }
            jobs.remove(name)
        }
    }

    override fun setConfirmed(name: String, isSuccess: Boolean) {
        jobs[name]?.cancel()
        val targetState = if (isSuccess) ItemState.Successful else ItemState.Error
        _statuses.update { it + (name to targetState) }

        jobs[name] = scope.launch {
            delay(CONFIRMATION_DELAY_MS)
            _statuses.update { it - name }
            jobs.remove(name)
        }
    }

    override fun reset(name: String) {
        jobs.remove(name)?.cancel()
        _statuses.update { it - name }
    }

    override fun resetAll() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
        _statuses.value = emptyMap()
    }

    internal companion object {
        const val LOADING_TIMEOUT_MS = 10_000L
        const val CONFIRMATION_DELAY_MS = 500L
    }
}
