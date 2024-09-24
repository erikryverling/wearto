package se.yverling.wearto.mobile

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
