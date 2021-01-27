package se.yverling.wearto.items.edit

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.error
import se.yverling.wearto.R
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.core.entities.Project
import se.yverling.wearto.items.edit.ItemViewModel.Event.FinishActivity
import se.yverling.wearto.items.edit.ItemViewModel.Event.HideKeyboard
import se.yverling.wearto.items.edit.ItemViewModel.Event.ShowSaveFailedDialog
import javax.inject.Inject

internal const val LATEST_SELECTED_PROJECT_PREFERENCES_KEY = "LATEST_SELECTED_PROJECT"

class ItemViewModel @Inject constructor(
        app: Application,
        private val databaseClient: DatabaseClient,
        private val sharedPreferences: SharedPreferences,
        private val analytics: FirebaseAnalytics
) : AndroidViewModel(app), AnkoLogger {

    val uuid = MutableLiveData<String>()
    val name = MutableLiveData<String>()
    val projectName = MutableLiveData<String>()
    val itemErrorMessage = MutableLiveData("")
    internal val events = MutableLiveData<Event>()

    private val disposables = CompositeDisposable()

    val arrayAdapter = ArrayAdapter<String>(getApplication() as Context, R.layout.spinner_list_header, arrayListOf())

    init {
        arrayAdapter.setDropDownViewResource(R.layout.spinner_list_item)

        val disposable = databaseClient.findAllProjects()
                .doOnSuccess {
                    arrayAdapter.addAll(getSpinnerArray(it))
                }
                .flatMapMaybe {
                    Maybe.fromCallable<String> {
                        val selectedProjectName = sharedPreferences.getString(LATEST_SELECTED_PROJECT_PREFERENCES_KEY, "Inbox")

                        if (it.find { it.name == selectedProjectName } != null) {
                            selectedProjectName
                        } else {
                            null
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            projectName.value = it
                        },

                        onError = {
                            error(it)
                        }
                )
        disposables.add(disposable)

        // TODO Hmm...
        Transformations.map(name) {
            if (!name.value.isNullOrBlank()) {
                itemErrorMessage.value = ""
            }
        }
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun edit(uuid: String) {
        val disposable = databaseClient.findItemWithProjectByUuid(uuid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            this.uuid.value = it.item.uuid
                            name.value = it.item.name
                            projectName.value = it.project.name
                        },

                        onError = {
                            error(it)
                        }
                )
        disposables.add(disposable)
    }

    fun save() {
        if (uuid.value != null) {
            val disposable = databaseClient.updateItem(uuid.value!!, name.value!!, projectName.value!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onComplete = {
                                analytics.logEvent("update_item", bundleOf(Pair("result", "succeeded")))
                                events.value = FinishActivity
                            },

                            onError = {
                                analytics.logEvent("update_item", bundleOf(Pair("result", "failed")))
                                events.value = ShowSaveFailedDialog
                            }
                    )
            disposables.add(disposable)
        } else {
            val disposable = databaseClient.saveItem(name.value!!, projectName.value!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onComplete = {
                                analytics.logEvent("save_item", bundleOf(Pair("result", "succeeded")))
                                events.value = FinishActivity
                            },

                            onError = {
                                analytics.logEvent("save_item", bundleOf(Pair("result", "failed")))
                                events.value = ShowSaveFailedDialog
                            }
                    )
            disposables.add(disposable)
        }
        saveLastSelectedProject()
    }

    fun delete() {
        val disposable = databaseClient.deleteItem(uuid.value!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            analytics.logEvent("delete_item", bundleOf(Pair("result", "succeeded")))
                            events.value = FinishActivity
                        },

                        onError = {
                            error(it)
                            analytics.logEvent("delete_item", bundleOf(Pair("result", "failed")))
                        }
                )
        disposables.add(disposable)
    }

    fun isValid(): Boolean {
        return name.value?.isNotBlank() ?: false
    }

    fun showValidationMessage(message: String) {
        itemErrorMessage.value = message
    }

    fun onNameChanged() {
        if (!name.value.isNullOrBlank()) {
            itemErrorMessage.value = ""
        }
    }

    fun editorActionListener(): TextView.OnEditorActionListener {
        return TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!isValid()) {
                    showValidationMessage(getApplication<WearToApplication>().getString(R.string.item_name_is_required_label))
                    events.value = HideKeyboard
                } else {
                    save()
                }
            }
            false
        }
    }

    private fun getSpinnerArray(projects: List<Project>): ArrayList<String> {
        val names = projects.asSequence().filter { it.name != "Inbox" }.map { it.name }.toList()
        return ArrayList(listOf("Inbox") + names)
    }

    private fun saveLastSelectedProject() {
        val disposable = Completable.fromRunnable {
            val editor = sharedPreferences.edit()
            editor.putString(LATEST_SELECTED_PROJECT_PREFERENCES_KEY, projectName.value!!)
            editor.apply()
        }
                .subscribeBy(
                        onError = {
                            error(it)
                        }
                )
        disposables.add(disposable)
    }

    sealed class Event {
        object FinishActivity : Event()
        object HideKeyboard : Event()
        object ShowSaveFailedDialog : Event()
    }

}

@BindingAdapter("errorMessage")
fun setErrorMessage(textInputLayout: TextInputLayout, errorMessage: String) {
    textInputLayout.error = errorMessage
}

@BindingAdapter("hint")
fun setHint(editText: EditText, @StringRes resourceId: Int) {
    editText.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            editText.setHint(resourceId)
        } else {
            editText.hint = ""
        }
    }
}

@BindingAdapter("editorActionListener")
fun setEditorActionListener(editText: EditText, listener: TextView.OnEditorActionListener) {
    editText.setOnEditorActionListener(listener)
}
