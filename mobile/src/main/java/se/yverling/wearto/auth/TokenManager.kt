package se.yverling.wearto.auth

import android.content.SharedPreferences
import io.reactivex.Completable
import io.reactivex.Maybe
import javax.inject.Inject

private const val ACCESS_TOKEN_PREFERENCES_KEY = "ACCESS_TOKEN"

class TokenManager @Inject constructor(private val sharedPreferences: SharedPreferences) {
    fun getAccessToken(): Maybe<String> {
        return Maybe.fromCallable {
            sharedPreferences.getString(ACCESS_TOKEN_PREFERENCES_KEY, null)
        }
    }

    fun persistToken(token: String): Completable {
        return Completable.fromRunnable {
            val editor = sharedPreferences.edit()
            editor.putString(ACCESS_TOKEN_PREFERENCES_KEY, token)
            editor.apply()
        }
    }

    fun removeAccessToken(): Completable {
        return Completable.fromRunnable {
            val editor = sharedPreferences.edit()
            editor.remove(ACCESS_TOKEN_PREFERENCES_KEY)
            editor.apply()
        }
    }
}