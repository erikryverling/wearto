package se.yverling.wearto.core.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import se.yverling.wearto.items.ItemsViewModel
import se.yverling.wearto.items.edit.ItemViewModel
import se.yverling.wearto.login.LoginViewModel
import kotlin.reflect.KClass

@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    internal abstract fun bindLoginViewModel(loginViewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ItemsViewModel::class)
    internal abstract fun bindItemsViewModel(itemsViewModel: ItemsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ItemViewModel::class)
    internal abstract fun bindItemViewModel(itemViewModel: ItemViewModel): ViewModel
}

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)