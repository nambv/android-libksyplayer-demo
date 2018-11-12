package streaming.yeah1.com.streaming

import android.app.Application
import streaming.yeah1.com.streaming.utils.LoggerManager
import timber.log.Timber

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(LoggerManager())
    }
}