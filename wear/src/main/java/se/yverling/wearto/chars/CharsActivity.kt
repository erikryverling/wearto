package se.yverling.wearto.chars

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.intentFor
import se.yverling.wearto.R
import se.yverling.wearto.chars.Event.StartItemsActivity
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.core.di.ViewModelFactory
import se.yverling.wearto.databinding.CharsActivityBinding
import se.yverling.wearto.items.ItemsActivity
import javax.inject.Inject

const val CHAR_EXTRA = "CHAR_EXTRA"

class CharsActivity : FragmentActivity(), AnkoLogger {
    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<CharsViewModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        WearToApplication.appComponent.inject(this)

        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this, viewModelFactory).get(CharsViewModel::class.java)

        viewModel.events.observe(this, {
            when (it) {
                is StartItemsActivity -> {
                    startItemsActivity(it.char)
                    finish()
                }
            }
        })

        val binding: CharsActivityBinding = DataBindingUtil.setContentView(this, R.layout.chars_activity)!!

        binding.viewModel = viewModel
    }

    private fun startItemsActivity(char: Char?) {
        if (char == null) {
            startActivity(intentFor<ItemsActivity>())
        } else {
            startActivity(intentFor<ItemsActivity>(CHAR_EXTRA to char))
        }
    }
}