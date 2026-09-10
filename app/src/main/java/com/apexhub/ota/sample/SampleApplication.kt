package com.apexhub.ota.sample

import android.app.Application
import android.util.Log
import com.apexhub.sdk.ApexHubConfig
import com.apexhub.sdk.ApexHubUpdater
import com.apexhub.sdk.UpdateStrategy

/**
 * Initializes the ApexHub SDK once, at process start.
 * The public key below was issued by the ApexHub backend when this app
 * (com.apexhub.ota.sample) was registered.
 */
class SampleApplication : Application() {

    companion object {
        lateinit var updater: ApexHubUpdater
            private set
    }

    override fun onCreate() {
        super.onCreate()

        updater = ApexHubUpdater(
            context = this,
            config = ApexHubConfig(
                publicKey           = BuildConfig.APEXHUB_PUBLIC_KEY,
                packageName         = BuildConfig.APEXHUB_PACKAGE,
                channel             = "stable",
                checkIntervalHours  = 6,
                updateStrategy      = UpdateStrategy.FLEXIBLE,
                allowMeteredNetwork = false,
            )
        )

        updater.schedulePeriodicCheck(appDisplayName = getString(R.string.app_name))

        Log.i("ApexHub", "SDK initialized. Public key: ${BuildConfig.APEXHUB_PUBLIC_KEY.take(16)}…")
    }
}
