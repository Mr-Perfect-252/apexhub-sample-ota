package com.apexhub.ota.sample

import android.app.Application
import android.app.Dialog
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.apexhub.sdk.ApexHubConfig
import com.apexhub.sdk.ApexHubUpdater
import com.apexhub.sdk.UpdateCheckResult
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog
import kotlin.concurrent.thread

/**
 * Deterministic, machine-checkable proof that the ApexHub SDK:
 *   1. Detects that a newer version (1.0.1 / code 2) is available, and
 *   2. Prompts the user with the "Update available" dialog.
 *
 * The mock-backed tests are hermetic. The live-backend test exercises the real
 * production ApexHub deployment with the real public key for com.apexhub.ota.sample.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class OtaUpdateTest {

    private lateinit var server: MockWebServer

    private val v2Json = """
        {
          "updateAvailable": true,
          "latestVersion": "1.0.1",
          "versionCode": 2,
          "downloadUrl": "https://example.com/app-1.0.1.apk",
          "sha256": "",
          "mandatory": false,
          "releaseNotes": "OTA update delivery verified end-to-end via the ApexHub SDK.",
          "rolloutPercent": 100,
          "channel": "stable",
          "certificateFingerprint": ""
        }
    """.trimIndent()

    @Before fun setUp() { server = MockWebServer(); server.start() }
    @After fun tearDown() { server.shutdown() }

    private fun updater(baseUrl: String, pk: String = "pk_live_test_key_1234567890") =
        ApexHubUpdater(
            context = ApplicationProvider.getApplicationContext(),
            config = ApexHubConfig(
                publicKey = pk,
                packageName = "com.apexhub.ota.sample",
                channel = "stable",
                baseUrl = baseUrl,
            )
        )

    /** 1a. Detection against a mocked backend advertising v2. */
    @Test
    fun detectsUpdateFromMockedBackend() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody(v2Json))
        val result = updater(server.url("/").toString().trimEnd('/')).checkForUpdate()
        assertTrue("SDK must detect an update", result is UpdateCheckResult.UpdateAvailable)
        val info = (result as UpdateCheckResult.UpdateAvailable).info
        assertEquals("1.0.1", info.latestVersion)
        assertEquals(2, info.versionCode)
    }

    /** 1b. Detection against the REAL live ApexHub production backend. */
    @Test
    fun detectsUpdateFromLiveBackend() = runBlocking {
        val result = updater(
            baseUrl = "https://apex-hub-production.vercel.app",
            pk = "pk_live_x-ApA2FX5yvnnDD-GjxXUAl9k9bAx10n"
        ).checkForUpdate()
        assertTrue(
            "Live backend must advertise an update for an installed v1 client; got: $result",
            result is UpdateCheckResult.UpdateAvailable
        )
        val info = (result as UpdateCheckResult.UpdateAvailable).info
        assertEquals("1.0.1", info.latestVersion)
        assertEquals(2, info.versionCode)
    }

    /** 2. The SDK actually PROMPTS the user with the update dialog. */
    @Test
    fun showsUpdatePromptDialog() {
        server.enqueue(MockResponse().setResponseCode(200).setBody(v2Json))
        val activity = Robolectric.buildActivity(TestActivity::class.java).setup().get()
        val updater = updater(server.url("/").toString().trimEnd('/'))

        // checkAndPrompt blocks on a latch until the dialog is dismissed → run off the test thread.
        val worker = thread { runBlocking { updater.checkAndPrompt(activity) } }

        val looper = shadowOf(Looper.getMainLooper())
        var dialog: Dialog? = null
        val deadline = System.currentTimeMillis() + 15_000
        while (System.currentTimeMillis() < deadline) {
            looper.idle()
            val d = ShadowDialog.getLatestDialog()
            if (d != null && d.isShowing) { dialog = d; break }
            Thread.sleep(25)
        }

        assertNotNull("SDK should have shown an update dialog", dialog)
        val texts = collectTexts(dialog!!.window!!.decorView)
        assertTrue(
            "Dialog should be titled 'Update available'; saw: $texts",
            texts.any { it.contains("Update available") }
        )
        assertTrue(
            "Dialog should announce version 1.0.1; saw: $texts",
            texts.any { it.contains("Version 1.0.1 is available") }
        )

        // Dismiss ("Later") to release the SDK's latch and let the coroutine finish.
        val later = dialog.findViewById<View>(android.R.id.button2)
        (later as? android.widget.Button)?.performClick()
            ?: dialog.dismiss()
        looper.idle()
        worker.join(5_000)
    }

    private fun collectTexts(root: View): List<String> {
        val out = mutableListOf<String>()
        fun walk(v: View) {
            if (v is TextView) v.text?.toString()?.let { if (it.isNotBlank()) out.add(it) }
            if (v is ViewGroup) for (i in 0 until v.childCount) walk(v.getChildAt(i))
        }
        walk(root)
        return out
    }
}
