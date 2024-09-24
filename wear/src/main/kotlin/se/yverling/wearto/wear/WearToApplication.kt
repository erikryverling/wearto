package se.yverling.wearto.wear

import android.app.Application
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant

class WearToApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        }
    }
}
