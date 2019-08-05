package se.yverling.wearto.items.edit

import android.app.Dialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import androidx.databinding.DataBindingUtil.setContentView
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import org.jetbrains.anko.AnkoLogger
import se.yverling.wearto.R
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.databinding.ItemActivityBinding
import se.yverling.wearto.items.edit.ItemViewModel.Events.*
import se.yverling.wearto.ui.errorTryAgainDialog
import javax.inject.Inject

internal const val ITEM_UUID_KEY = "UUID"

class ItemActivity : AppCompatActivity(), AnkoLogger {
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: ItemViewModel

    private lateinit var errorDialog: Dialog

    private lateinit var binding: ItemActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WearToApplication.appComponent.inject(this)

        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ItemViewModel::class.java)

        binding = setContentView(this, R.layout.item_activity)!!

        errorDialog = errorTryAgainDialog(
                this,
                R.string.save_error_title,
                R.string.save_error_message,
                DialogInterface.OnClickListener { _, _ ->
                    viewModel.save()
                }
        )

        viewModel.events.observe(this, Observer {
            when (it) {
                FINISH_ACTIVITY_EVENT -> finish()

                HIDE_KEYBOARD_EVENT -> hideKeyboard()

                SHOW_SAVE_FAILED_DIALOG_EVENT -> errorDialog.show()
            }
        })

        binding.viewModel = viewModel

        setEditMode()

        showKeyboard()
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
        errorDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.spinner.adapter = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (intent.extras?.getString(ITEM_UUID_KEY) != null) {
            menuInflater.inflate(R.menu.item_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_item_action -> viewModel.delete()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (!viewModel.isValid()) {
            hideKeyboard()
            viewModel.showValidationMessage(getString(R.string.item_name_is_required_label))
            false
        } else {
            viewModel.save()
            false
        }
    }

    private fun setEditMode() {
        val uuid = intent.extras?.getString(ITEM_UUID_KEY)
        if (uuid != null) {
            viewModel.edit(uuid)
            initActionBar(R.string.edit_item_title)
        } else {
            initActionBar(R.string.add_item_title)
        }
    }

    private fun initActionBar(@StringRes id: Int) {
        supportActionBar?.title = getString(id)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp)
    }

    private fun showKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}