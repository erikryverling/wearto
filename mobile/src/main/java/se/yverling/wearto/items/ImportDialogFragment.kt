package se.yverling.wearto.items

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.core.di.ViewModelFactory
import se.yverling.wearto.databinding.ImportDialogFragmentBinding
import javax.inject.Inject


class ImportDialogFragment : DialogFragment() {
    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<ItemsViewModel>

    private lateinit var viewModel: ItemsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        WearToApplication.appComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(activity!!, viewModelFactory).get(ItemsViewModel::class.java)
        val binding = ImportDialogFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    companion object {
        fun newInstance(): ImportDialogFragment {
            return ImportDialogFragment()
        }
    }
}