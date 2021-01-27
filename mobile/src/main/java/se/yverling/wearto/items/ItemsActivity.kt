package se.yverling.wearto.items

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.*
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.browse
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.info
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity
import se.yverling.wearto.BuildConfig
import se.yverling.wearto.R
import se.yverling.wearto.auth.TokenManager
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.core.di.ViewModelFactory
import se.yverling.wearto.databinding.ItemsActivityBinding
import se.yverling.wearto.items.ItemsViewModel.Event.DismissImportDialog
import se.yverling.wearto.items.ItemsViewModel.Event.RotateSyncIcon
import se.yverling.wearto.items.ItemsViewModel.Event.ShowAddTapTarget
import se.yverling.wearto.items.ItemsViewModel.Event.ShowImportFailedDialog
import se.yverling.wearto.items.ItemsViewModel.Event.ShowSyncTapTarget
import se.yverling.wearto.items.ItemsViewModel.Event.StartItemActivity
import se.yverling.wearto.items.ItemsViewModel.Event.SyncFailedDueToDataLayerDialog
import se.yverling.wearto.items.ItemsViewModel.Event.SyncFailedDueToGeneralError
import se.yverling.wearto.items.ItemsViewModel.Event.SyncFailedDueToNetworkDialog
import se.yverling.wearto.items.ItemsViewModel.Event.SyncSucceededSnackbar
import se.yverling.wearto.items.edit.ITEM_UUID_KEY
import se.yverling.wearto.items.edit.ItemActivity
import se.yverling.wearto.login.LoginActivity
import se.yverling.wearto.ui.errorTryAgainDialog
import se.yverling.wearto.ui.warningMessageDialog
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import javax.inject.Inject

internal const val ADD_ITEM_TAP_TARGET_PREFERENCES_KEY = "ADD_ITEM_TAP_TARGET_PREFERENCES"
internal const val SYNC_TAP_TARGET_PREFERENCES_KEY = "SYNC_TAP_TARGET_PREFERENCES"

class ItemsActivity : AppCompatActivity(), AnkoLogger {

    @Inject
    internal lateinit var tokenManager: TokenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<ItemsViewModel>

    @Inject
    internal lateinit var databaseClient: DatabaseClient

    @Inject
    internal lateinit var sharedPreferences: SharedPreferences

    @Inject
    internal lateinit var analytics: FirebaseAnalytics

    private lateinit var viewModel: ItemsViewModel
    private val disposables = CompositeDisposable()

    private lateinit var logoutDialog: Dialog

    private lateinit var syncFailedDueToNetworkDialog: Dialog
    private lateinit var syncFailedDueToDataLayerDialog: Dialog
    private lateinit var syncFailedDueToGeneralErrorDialog: Dialog

    private lateinit var importFailedDueToGeneralErrorDialog: Dialog

    private lateinit var importDialog: ImportDialogFragment

    private lateinit var binding: ItemsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WearToApplication.appComponent.inject(this)

        super.onCreate(savedInstanceState)

        binding = setContentView(this, R.layout.items_activity)!!
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(ItemsViewModel::class.java)

        binding.viewModel = viewModel

        initDialogs()

        val disposable = tokenManager.getAccessToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            // No access token found. Redirect to login screen
                            startActivity<LoginActivity>()
                            finish()
                        },

                        onSuccess = {
                            supportActionBar?.title = getString(R.string.items_title)

                            viewModel.events.observe(this, {
                                when (it) {
                                    ShowAddTapTarget -> showAddTapTarget()

                                    ShowSyncTapTarget -> showSyncTapTarget()

                                    RotateSyncIcon -> rotateSyncIcon()

                                    DismissImportDialog -> dismissImportDialog()

                                    SyncSucceededSnackbar -> snackbar(binding.root, R.string.sync_succeeded_message)

                                    StartItemActivity -> startActivity<ItemActivity>()

                                    SyncFailedDueToNetworkDialog -> syncFailedDueToNetworkDialog.show()

                                    SyncFailedDueToDataLayerDialog -> syncFailedDueToDataLayerDialog.show()

                                    SyncFailedDueToGeneralError -> syncFailedDueToGeneralErrorDialog.show()

                                    ShowImportFailedDialog -> importFailedDueToGeneralErrorDialog.show()
                                }
                            })

                            viewModel.getItemToEditEvents().observe(this, { uuid ->
                                startActivity(intentFor<ItemActivity>(ITEM_UUID_KEY to uuid))
                            })
                        },

                        onError = {
                            error(it)
                        }
                )
        disposables.add(disposable)
    }

    override fun onPause() {
        super.onPause()

        logoutDialog.dismiss()

        syncFailedDueToNetworkDialog.dismiss()
        syncFailedDueToDataLayerDialog.dismiss()
        syncFailedDueToGeneralErrorDialog.dismiss()

        if (::importDialog.isInitialized) importDialog.dismiss()
        importFailedDueToGeneralErrorDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.items.adapter = null
        disposables.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.items_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sync_items_action -> viewModel.sync()

            R.id.import_items_action -> showImportDialog()

            R.id.licences_action -> browse(BuildConfig.LICENCES_URL)

            R.id.privacy_policy_action -> browse(BuildConfig.PRIVACY_POLICY_URL)

            R.id.logout_action -> logoutDialog.show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initDialogs() {
        logoutDialog = warningMessageDialog(
                this,
                R.string.logout_warning_title,
                R.string.logout_warning_message
        ) { _, _ ->
            val disposable = databaseClient.deleteAll()
                    .doOnComplete {
                        info("LOGOUT: All local items and projects removed")
                    }
                    .andThen(tokenManager.removeAccessToken())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onComplete = {
                                info("LOGOUT: Access token removed")
                                analytics.logEvent("logout", bundleOf(Pair("result", "succeeded")))
                                viewModel.clear()
                                startLogoutActivity()
                            },

                            onError = {
                                analytics.logEvent("logout", bundleOf(Pair("result", "failed")))
                                error(it)
                            }
                    )
            disposables.add(disposable)
        }

        errorTryAgainDialog(
                this,
                R.string.sync_error_title,
                R.string.sync_error_due_to_network_message
        ) { _, _ ->
            viewModel.sync()
        }.also { syncFailedDueToNetworkDialog = it }

        syncFailedDueToDataLayerDialog = errorTryAgainDialog(
                this,
                R.string.sync_error_title,
                R.string.sync_error_due_to_data_layer_message
        ) { _, _ ->
            viewModel.sync()
        }

        errorTryAgainDialog(
                this,
                R.string.sync_error_title,
                R.string.sync_error_due_to_general_error_message
        ) { _, _ ->
            viewModel.sync()
        }.also { syncFailedDueToGeneralErrorDialog = it }

        errorTryAgainDialog(
                this,
                R.string.import_error_title,
                R.string.import_error_due_to_general_error_message
        ) { _, _ ->
            viewModel.importItems()
        }.also { importFailedDueToGeneralErrorDialog = it }
    }

    private fun showAddTapTarget() {
        val disposable = showTapTarget(
                ADD_ITEM_TAP_TARGET_PREFERENCES_KEY,
                R.id.add_item_button,
                R.string.add_item_tap_target_primary,
                R.string.add_item_tap_target_secondary)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            it.show()
                        },

                        onError = {
                            error(it)
                        }
                )
        disposables.add(disposable)
    }

    private fun showSyncTapTarget() {
        val disposable = showTapTarget(
                SYNC_TAP_TARGET_PREFERENCES_KEY,
                R.id.sync_items_action,
                R.string.sync_item_tap_target_primary,
                R.string.sync_item_tap_target_secondary)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            it.show()
                        },

                        onError = {
                            error(it)
                        }
                )
        disposables.add(disposable)
    }

    private fun showTapTarget(
            preferencesKey: String,
            @IdRes tapTarget: Int,
            @StringRes primaryText: Int,
            @StringRes secondaryText: Int
    ): Maybe<MaterialTapTargetPrompt.Builder> {
        return Maybe.fromCallable {
            val editor = sharedPreferences.edit()

            val baseBuilder = MaterialTapTargetPrompt.Builder(this)
                    .setFocalColour(getColor(R.color.primary_dark))
                    .setBackgroundColour(getColor(R.color.primary))
                    .setAutoDismiss(true)

            val hasTapTargetBeenShown = sharedPreferences.getBoolean(preferencesKey, false)
            if (!hasTapTargetBeenShown) {
                baseBuilder
                        .setTarget(findViewById(tapTarget))
                        .setPrimaryText(getString(primaryText))
                        .setSecondaryText(getString(secondaryText))

                editor.putBoolean(preferencesKey, true)
                editor.apply()
                baseBuilder
            } else {
                null
            }
        }
    }

    private fun rotateSyncIcon() {
        val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_sync)
        findViewById<View>(R.id.sync_items_action).startAnimation(rotation)
    }

    private fun dismissImportDialog() {
        importDialog.dismiss()
    }

    private fun startLogoutActivity() {
        val intent = intentFor<LoginActivity>()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showImportDialog() {
        val fm: FragmentManager = supportFragmentManager
        importDialog = ImportDialogFragment.newInstance()
        importDialog.show(fm, "import_dialog_fragment")
    }
}