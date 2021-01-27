package se.yverling.wearto.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import se.yverling.wearto.auth.TokenManager
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.login.LoginViewModel.Event.LoginFailedDueToGeneralError
import se.yverling.wearto.login.LoginViewModel.Event.LoginFailedDueToNetworkDialog
import se.yverling.wearto.login.LoginViewModel.Event.OpenTodoistUrl
import se.yverling.wearto.login.LoginViewModel.Event.StartItemsActivity
import se.yverling.wearto.sync.network.NetworkClient
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class LoginViewModel @Inject constructor(
        app: Application,
        private val tokenManager: TokenManager,
        private val networkClient: NetworkClient,
        private val databaseClient: DatabaseClient,
        private val analytics: FirebaseAnalytics
) : AndroidViewModel(app), AnkoLogger {

    val accessToken = MutableLiveData<String>()
    val isLoggingIn = MutableLiveData<Boolean>()

    internal val events = MutableLiveData<Event>()

    private val disposables = CompositeDisposable()

    override fun onCleared() {
        disposables.clear()
    }

    fun login() {
        isLoggingIn.value = true

        val disposable = networkClient.getProjects(accessToken.value!!)
                .flatMapCompletable {
                    databaseClient.replaceAllProjects(it.projects)
                }
                .doOnComplete {
                    info("LOGIN: All projects fetched and saved")
                }
                .andThen(tokenManager.persistToken(accessToken.value!!))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            info("LOGIN: Access token saved")
                            analytics.logEvent("login", bundleOf(Pair("result", "succeeded")))
                            accessToken.value = ""
                            isLoggingIn.value = false
                            events.value = StartItemsActivity
                        },

                        onError = {
                            error(it)
                            analytics.logEvent("login", bundleOf(Pair("result", "failed")))

                            if (it is UnknownHostException || it is SocketTimeoutException) {
                                events.value = LoginFailedDueToNetworkDialog
                            } else {
                                events.value = LoginFailedDueToGeneralError
                            }

                            isLoggingIn.value = false
                        }
                )
        disposables.add(disposable)
    }

    fun onLinkClick() {
        events.value = OpenTodoistUrl
    }

    sealed class Event {
        object StartItemsActivity : Event()

        object LoginFailedDueToNetworkDialog : Event()
        object LoginFailedDueToGeneralError : Event()

        object OpenTodoistUrl : Event()
    }
}