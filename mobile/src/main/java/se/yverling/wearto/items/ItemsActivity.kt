package se.yverling.wearto.items

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.browse
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.info
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity
import se.yverling.wearto.BuildConfig
import se.yverling.wearto.R
import se.yverling.wearto.auth.TokenManager
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.databinding.ItemsActivityBinding
import se.yverling.wearto.items.ItemsViewModel.Events.*
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
    internal lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    internal lateinit var databaseClient: DatabaseClient
    @Inject
    internal lateinit var sharedPreferences: SharedPreferences

    private lateinit var viewModel: ItemsViewModel
    private val disposables = CompositeDisposable()

    private lateinit var logoutDialog: Dialog
    private lateinit var syncFailedDueToNetworkDialog: Dialog
    private lateinit var syncFailedDueToDataLayerDialog: Dialog
    private lateinit var syncFailedDueToGeneralErrorDialog: Dialog

    private var binding: ItemsActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WearToApplication.appComponent.inject(this)

        super.onCreate(savedInstanceState)

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

                            viewModel = ViewModelProviders
                                    .of(this, viewModelFactory)
                                    .get(ItemsViewModel::class.java)

                             binding = DataBindingUtil.setContentView(this, R.layout.items_activity)!!

                            viewModel.events.observe(this, Observer {
                                when (it) {
                                    SHOW_ADD_TAP_TARGET_EVENT -> showAddTapTarget()

                                    SHOW_SYNC_TAP_TARGET_EVENT -> showSyncTapTarget()

                                    ROTATE_SYNC_ICON_EVENT -> rotateSyncIcon()

                                    SYNC_SUCCEEDED_SNACKBAR_EVENT -> snackbar(binding!!.root, R.string.sync_succeeded_message)

                                    START_ITEM_ACTIVITY_EVENT -> startActivity<ItemActivity>()

                                    SYNC_FAILED_DUE_TO_NETWORK_DIALOG_EVENT -> syncFailedDueToNetworkDialog.show()

                                    SYNC_FAILED_DUE_TO_DATA_LAYER_DIALOG_EVENT -> syncFailedDueToDataLayerDialog.show()

                                    SYNC_FAILED_DUE_TO_GENERAL_ERROR_EVENT -> syncFailedDueToGeneralErrorDialog.show()
                                }
                            })

                            viewModel.getItemToEditEvents().observe(this, Observer { uuid ->
                                startActivity(intentFor<ItemActivity>(ITEM_UUID_KEY to uuid))
                            })

                            binding!!.viewModel = viewModel
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
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.items?.adapter = null
        disposables.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.items_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sync_items_action -> viewModel.sync()

            R.id.licences_action -> browse(BuildConfig.ACKNOWLEDGMENTS_URL)

            R.id.logout_action -> logoutDialog.show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initDialogs() {
        logoutDialog = warningMessageDialog(
                this,
                R.string.logout_warning_title,
                R.string.logout_warning_message,
                DialogInterface.OnClickListener { _, _ ->
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
                                        viewModel.clear()
                                        startLogoutActivity()
                                    },

                                    onError = {
                                        error(it)
                                    }
                            )
                    disposables.add(disposable)
                }
        )

        syncFailedDueToNetworkDialog = errorTryAgainDialog(
                this,
                R.string.sync_error_title,
                R.string.sync_error_due_to_network_message,
                DialogInterface.OnClickListener { _, _ ->
                    viewModel.sync()
                }
        )

        syncFailedDueToDataLayerDialog = errorTryAgainDialog(
                this,
                R.string.sync_error_title,
                R.string.sync_error_due_to_data_layer_message,
                DialogInterface.OnClickListener { _, _ ->
                    viewModel.sync()
                }
        )

        syncFailedDueToGeneralErrorDialog = errorTryAgainDialog(
                this,
                R.string.sync_error_title,
                R.string.sync_error_due_to_general_error_message,
                DialogInterface.OnClickListener { _, _ ->
                    viewModel.sync()
                }
        )
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
                        .setTarget(findViewById<View>(tapTarget))
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

    private fun startLogoutActivity() {
        val intent = intentFor<LoginActivity>()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
