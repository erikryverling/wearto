package se.yverling.wearto.login

import android.app.Dialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import androidx.databinding.DataBindingUtil.setContentView
import android.os.Bundle
import androidx.annotation.TransitionRes
import androidx.appcompat.app.AppCompatActivity
import android.transition.Transition
import android.transition.TransitionInflater
import org.jetbrains.anko.browse
import org.jetbrains.anko.intentFor
import se.yverling.wearto.R
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.databinding.LoginActivityBinding
import se.yverling.wearto.items.ItemsActivity
import se.yverling.wearto.login.LoginViewModel.Events.LOGIN_FAILED_DUE_TO_GENERAL_ERROR_EVENT
import se.yverling.wearto.login.LoginViewModel.Events.LOGIN_FAILED_DUE_TO_NETWORK_DIALOG_EVENT
import se.yverling.wearto.login.LoginViewModel.Events.OPEN_TODOIST_URL
import se.yverling.wearto.login.LoginViewModel.Events.START_ITEMS_ACTIVITY_EVENT
import se.yverling.wearto.ui.errorTryAgainDialog
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: LoginActivityBinding

    private lateinit var loginFailedDueToNetworkErrorDialog: Dialog
    private lateinit var loginFailedDueToGeneralErrorDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        WearToApplication.appComponent.inject(this)

        super.onCreate(savedInstanceState)

        binding = setContentView(this, R.layout.login_activity)!!

        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel::class.java)

        viewModel.events.observe(this, Observer {
            when (it) {
                START_ITEMS_ACTIVITY_EVENT -> startItemsActivity()

                LOGIN_FAILED_DUE_TO_NETWORK_DIALOG_EVENT -> loginFailedDueToNetworkErrorDialog.show()

                LOGIN_FAILED_DUE_TO_GENERAL_ERROR_EVENT -> loginFailedDueToGeneralErrorDialog.show()

                OPEN_TODOIST_URL -> browse(getString(R.string.todoist_link))
            }
        })

        initDialogs(viewModel)

        binding.viewModel = viewModel
    }

    override fun onPause() {
        super.onPause()
        loginFailedDueToNetworkErrorDialog.dismiss()
        loginFailedDueToGeneralErrorDialog.dismiss()
    }

    private fun initDialogs(viewModel: LoginViewModel) {
        loginFailedDueToNetworkErrorDialog = errorTryAgainDialog(
                this,
                R.string.login_failed_title,
                R.string.login_failed_due_to_network_message,
                DialogInterface.OnClickListener { _, _ ->
                    viewModel.login()
                })

        loginFailedDueToGeneralErrorDialog = errorTryAgainDialog(
                this,
                R.string.login_failed_title,
                R.string.login_failed_due_to_general_error_message,
                DialogInterface.OnClickListener { _, _ ->
                    viewModel.login()
                })
    }

    private fun startItemsActivity() {
        val intent = intentFor<ItemsActivity>()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun inflateTransition(@TransitionRes resourceId: Int): Transition {
        return TransitionInflater.from(this).inflateTransition(resourceId)
    }
}