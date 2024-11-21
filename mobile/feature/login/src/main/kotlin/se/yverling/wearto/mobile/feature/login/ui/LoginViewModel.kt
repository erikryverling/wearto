package se.yverling.wearto.mobile.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import se.yverling.wearto.mobile.data.token.TokenRepository
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: TokenRepository) : ViewModel() {
    suspend fun setToken(token: String) {
        repository.setToken(token)
    }
}
