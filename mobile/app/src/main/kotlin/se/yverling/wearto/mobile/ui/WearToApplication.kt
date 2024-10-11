package se.yverling.wearto.mobile.ui

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import se.yverling.wearto.mobile.app.BuildConfig
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant

@HiltAndroidApp
class WearToApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        }
    }
}
