package se.yverling.wearto.items

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import android.support.wearable.activity.ConfirmationActivity
import android.support.wearable.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE
import android.support.wearable.activity.ConfirmationActivity.EXTRA_MESSAGE
import android.support.wearable.activity.ConfirmationActivity.FAILURE_ANIMATION
import android.support.wearable.activity.ConfirmationActivity.SUCCESS_ANIMATION
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.intentFor
import se.yverling.wearto.R
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.databinding.ItemsActivityBinding
import se.yverling.wearto.items.Events.*
import javax.inject.Inject

private const val FINISH_ACTIVITY: Int = 100

class ItemsActivity : FragmentActivity(), LifecycleOwner, AnkoLogger {
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var lifecycleRegistry: LifecycleRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        WearToApplication.appComponent.inject(this)

        super.onCreate(savedInstanceState)

        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.markState(Lifecycle.State.CREATED)

        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(ItemsViewModel::class.java)

        viewModel.getSelectedItem().observe(this, Observer {
            it?.let {
                showConfirmationViewAndFinish(it)
            }
        })

        viewModel.events.observe(this, Observer {
            when (it) {
                SHOW_ITEM_SELECTION_FAILED_EVENT -> showErrorView()
            }
        })

        val binding: ItemsActivityBinding = DataBindingUtil.setContentView(this, R.layout.items_activity)!!

        binding.viewModel = viewModel
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FINISH_ACTIVITY -> finish()
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
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
}