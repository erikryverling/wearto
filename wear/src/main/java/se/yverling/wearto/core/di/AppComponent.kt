package se.yverling.wearto.core.di

import dagger.Component
import se.yverling.wearto.chars.CharsActivity
import se.yverling.wearto.items.ItemsActivity
import se.yverling.wearto.sync.datalayer.DataLayerListenerService
import javax.inject.Singleton

@Singleton
@Component(modules = [(CoreModule::class)])
interface AppComponent {
    fun inject(activity: ItemsActivity)
    fun inject(activity: CharsActivity)
    fun inject(service: DataLayerListenerService)
}