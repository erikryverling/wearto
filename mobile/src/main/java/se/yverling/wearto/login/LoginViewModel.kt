package se.yverling.wearto.login

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.browse
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import se.yverling.wearto.R
import se.yverling.wearto.auth.TokenManager
import se.yverling.wearto.core.SingleLiveEvent
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.login.LoginViewModel.Events.*
import se.yverling.wearto.sync.network.NetworkClient
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class LoginViewModel @Inject constructor(
        private val app: Application,
        private val tokenManager: TokenManager,
        private val networkClient: NetworkClient,
        private val databaseClient: DatabaseClient
) : AndroidViewModel(app), AnkoLogger {

    val accessToken: ObservableField<String> = ObservableField()
    val isLoggingIn = ObservableBoolean()

    internal val events = SingleLiveEvent<Events>()

    private val disposables = CompositeDisposable()

    init {
        val disposable = tokenManager.getAccessToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            if (it == null) {
                                accessToken.set("")
                            } else {
                                accessToken.set(it)
                            }
                        },

                        onError = {
                            error(it)
                        }
                )
        disposables.add(disposable)
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun login() {
        isLoggingIn.set(true)

        val disposable = networkClient.getProjects(accessToken.get()!!)
                .flatMapCompletable {
                    databaseClient.replaceAllProjects(it.projects)
                }
                .doOnComplete {
                    info("LOGIN: All projects fetched and saved")
                }
                .andThen(tokenManager.persistToken(accessToken.get()!!))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            info("LOGIN: Access token saved")
                            accessToken.set("")
                            isLoggingIn.set(false)
                            events.value = START_ITEMS_ACTIVITY_EVENT
                        },

                        onError = {
                            error(it)

                            if (it is UnknownHostException || it is SocketTimeoutException) {
                                events.value = LOGIN_FAILED_DUE_TO_NETWORK_DIALOG_EVENT
                            } else {
                                events.value = LOGIN_FAILED_DUE_TO_GENERAL_ERROR_EVENT
                            }

                            isLoggingIn.set(false)
                        }
                )
        disposables.add(disposable)
    }

    fun onLinkClick() {
            events.value = OPEN_TODOIST_URL
    }

    enum class Events {
        START_ITEMS_ACTIVITY_EVENT,
        LOGIN_FAILED_DUE_TO_NETWORK_DIALOG_EVENT,
        LOGIN_FAILED_DUE_TO_GENERAL_ERROR_EVENT,

        OPEN_TODOIST_URL
    }
}