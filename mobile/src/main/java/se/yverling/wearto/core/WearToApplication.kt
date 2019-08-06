package se.yverling.wearto.core

import android.app.Application
import android.os.StrictMode
import se.yverling.wearto.BuildConfig
import se.yverling.wearto.core.di.AppComponent
import se.yverling.wearto.core.di.CoreModule
import se.yverling.wearto.core.di.DaggerAppComponent

open class WearToApplication : Application() {
    companion object {
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        appComponent = DaggerAppComponent.builder().coreModule(CoreModule(this)).build()

        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build())

            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }

    }
}