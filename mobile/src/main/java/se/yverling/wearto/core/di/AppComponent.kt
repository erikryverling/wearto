package se.yverling.wearto.core.di

import dagger.Component
import se.yverling.wearto.items.ImportDialogFragment
import se.yverling.wearto.items.ItemsActivity
import se.yverling.wearto.items.edit.ItemActivity
import se.yverling.wearto.login.LoginActivity
import se.yverling.wearto.sync.datalayer.DataLayerListenerService
import javax.inject.Singleton

@Singleton
@Component(modules = [(CoreModule::class), (ViewModelModule::class), (ResourceModule::class)])
interface AppComponent {
    fun inject(loginActivity: LoginActivity)
    fun inject(itemsActivity: ItemsActivity)
    fun inject(itemActivity: ItemActivity)
    fun inject(dataLayerListenerService: DataLayerListenerService)
    fun inject(importDialogFragment: ImportDialogFragment)
}