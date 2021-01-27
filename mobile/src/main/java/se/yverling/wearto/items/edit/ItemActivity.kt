package se.yverling.wearto.items.edit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProvider
import org.jetbrains.anko.AnkoLogger
import se.yverling.wearto.R
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.core.di.ViewModelFactory
import se.yverling.wearto.databinding.ItemActivityBinding
import se.yverling.wearto.items.edit.ItemViewModel.Event.FinishActivity
import se.yverling.wearto.items.edit.ItemViewModel.Event.HideKeyboard
import se.yverling.wearto.items.edit.ItemViewModel.Event.ShowSaveFailedDialog
import se.yverling.wearto.ui.errorTryAgainDialog
import javax.inject.Inject

internal const val ITEM_UUID_KEY = "UUID"

class ItemActivity : AppCompatActivity(), AnkoLogger {
    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<ItemViewModel>

    private lateinit var viewModel: ItemViewModel

    private lateinit var errorDialog: Dialog

    private lateinit var binding: ItemActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WearToApplication.appComponent.inject(this)

        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory).get(ItemViewModel::class.java)

        binding = setContentView(this, R.layout.item_activity)!!
        binding.lifecycleOwner = this

        errorDialog = errorTryAgainDialog(
                this,
                R.string.save_error_title,
                R.string.save_error_message
        ) { _, _ ->
            viewModel.save()
        }

        viewModel.events.observe(this) {
            when (it) {
                FinishActivity -> finish()

                HideKeyboard -> hideKeyboard()

                ShowSaveFailedDialog -> errorDialog.show()
            }
        }

        viewModel.name.observe(this) { viewModel.onNameChanged() }

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