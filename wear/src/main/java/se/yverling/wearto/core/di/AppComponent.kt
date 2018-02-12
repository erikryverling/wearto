package se.yverling.wearto.core.di

import dagger.Component
import se.yverling.wearto.items.ItemsActivity
import se.yverling.wearto.sync.datalayer.DataLayerListenerService
import javax.inject.Singleton

@Singleton
@Component(modules = [(CoreModule::class), (ViewModelModule::class)])
interface AppComponent {
    fun inject(itemsActivity: ItemsActivity)
    fun inject(dataLayerListenerService: DataLayerListenerService)
}
