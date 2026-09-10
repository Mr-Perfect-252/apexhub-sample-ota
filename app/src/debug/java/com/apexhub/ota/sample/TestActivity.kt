package com.apexhub.ota.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Minimal AppCompat-themed host activity for Robolectric tests.
 * The SDK shows its update dialog against this activity's context.
 */
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ApexHubOtaSample)
        super.onCreate(savedInstanceState)
    }
}
