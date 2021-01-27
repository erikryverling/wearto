package se.yverling.wearto.login

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import androidx.annotation.TransitionRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProvider
import org.jetbrains.anko.browse
import org.jetbrains.anko.intentFor
import se.yverling.wearto.R
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.core.di.ViewModelFactory
import se.yverling.wearto.databinding.LoginActivityBinding
import se.yverling.wearto.items.ItemsActivity
import se.yverling.wearto.login.LoginViewModel.Event.LoginFailedDueToGeneralError
import se.yverling.wearto.login.LoginViewModel.Event.LoginFailedDueToNetworkDialog
import se.yverling.wearto.login.LoginViewModel.Event.OpenTodoistUrl
import se.yverling.wearto.login.LoginViewModel.Event.StartItemsActivity
import se.yverling.wearto.ui.errorTryAgainDialog
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory<LoginViewModel>

    private lateinit var binding: LoginActivityBinding

    private lateinit var loginFailedDueToNetworkErrorDialog: Dialog
    private lateinit var loginFailedDueToGeneralErrorDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        WearToApplication.appComponent.inject(this)

        super.onCreate(savedInstanceState)

        binding = setContentView(this, R.layout.login_activity)!!
        binding.lifecycleOwner = this

        val viewModel = ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)

        viewModel.events.observe(this, {
            when (it) {
                StartItemsActivity -> startItemsActivity()

                LoginFailedDueToNetworkDialog -> loginFailedDueToNetworkErrorDialog.show()

                LoginFailedDueToGeneralError -> loginFailedDueToGeneralErrorDialog.show()

                OpenTodoistUrl -> browse(getString(R.string.todoist_link))
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
                R.string.login_failed_due_to_network_message
        ) { _, _ ->
            viewModel.login()
        }

        loginFailedDueToGeneralErrorDialog = errorTryAgainDialog(
                this,
                R.string.login_failed_title,
                R.string.login_failed_due_to_general_error_message
        ) { _, _ ->
            viewModel.login()
        }
    }

    private fun startItemsActivity() {
        val intent = intentFor<ItemsActivity>()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}