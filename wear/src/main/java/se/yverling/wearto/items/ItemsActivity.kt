package se.yverling.wearto.items

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.ConfirmationActivity
import android.support.wearable.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE
import android.support.wearable.activity.ConfirmationActivity.EXTRA_MESSAGE
import android.support.wearable.activity.ConfirmationActivity.FAILURE_ANIMATION
import android.support.wearable.activity.ConfirmationActivity.SUCCESS_ANIMATION
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.intentFor
import se.yverling.wearto.R
import se.yverling.wearto.chars.CHAR_EXTRA
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.core.db.AppDatabase
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.databinding.ItemsActivityBinding
import se.yverling.wearto.items.Event.ShowConfirmationAndFinish
import se.yverling.wearto.items.Event.ShowItemSelectionFailed
import se.yverling.wearto.sync.datalayer.DataLayerClient
import javax.inject.Inject

private const val FINISH_ACTIVITY: Int = 100

class ItemsActivity : FragmentActivity(), AnkoLogger {
    @Inject
    internal lateinit var dataLayerClient: DataLayerClient

    @Inject
    internal lateinit var viewAdapter: ItemsRecyclerViewAdapter

    @Inject
    internal lateinit var dataBase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        WearToApplication.appComponent.inject(this)

        super.onCreate(savedInstanceState)

        val char: Char? = intent?.extras?.getChar(CHAR_EXTRA)

        val viewModelFactory = ItemsViewModelFactory(
                application,
                dataBase,
                dataLayerClient,
                viewAdapter,
                char
        )

        val viewModel = ViewModelProvider(this, viewModelFactory).get(ItemsViewModel::class.java)

        viewModel.events.observe(this, {
            when (it) {
                is ShowItemSelectionFailed -> showErrorView()
                is ShowConfirmationAndFinish -> showConfirmationViewAndFinish(it.item)
                is Event.SendItem -> viewModel.sendItem(it.item)
            }
        })

        val binding: ItemsActivityBinding = DataBindingUtil.setContentView(this, R.layout.items_activity)!!
        binding.lifecycleOwner = this

        binding.viewModel = viewModel
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FINISH_ACTIVITY -> finish()
        }
    }

    private fun showConfirmationViewAndFinish(item: Item) {
        startActivityForResult(intentFor<ConfirmationActivity>(
                EXTRA_ANIMATION_TYPE to SUCCESS_ANIMATION,
                EXTRA_MESSAGE to "${item.name} ${getString(R.string.to_label)} ${item.projectName}"
        ), FINISH_ACTIVITY)
    }

    private fun showErrorView() {
        startActivityForResult(intentFor<ConfirmationActivity>(
                EXTRA_ANIMATION_TYPE to FAILURE_ANIMATION,
                EXTRA_MESSAGE to getString(R.string.error_sending_item_message)
        ), 0)
    }

    class ItemsViewModelFactory(
            private val app: Application,
            private val dataBase: AppDatabase,
            private val dataLayerClient: DataLayerClient,
            private val viewAdapter: ItemsRecyclerViewAdapter,
            private val char: Char?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ItemsViewModel(
                    app,
                    dataBase,
                    dataLayerClient,
                    viewAdapter,
                    char
            ) as T
        }
    }
}