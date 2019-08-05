package se.yverling.wearto.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject constructor(private val creators: Map<Class<out ViewModel>,
        @JvmSuppressWildcards Provider<ViewModel>>) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator: Provider<ViewModel>? = creators[modelClass] ?: provider(modelClass)
        val viewModel = creator?.get() ?: throw IllegalArgumentException("unknown model class " + modelClass)
        return viewModel as T
    }

    private fun <T : ViewModel> provider(modelClass: Class<T>): Provider<ViewModel>? {
        for ((key, value) in creators) {
            if (modelClass.isAssignableFrom(key)) {
                return value
            }
        }
        return null
    }
}