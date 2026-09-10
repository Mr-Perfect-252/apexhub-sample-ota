package com.apexhub.ota.sample

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * End-to-end instrumented proof on a real Android emulator.
 *
 * Launches the installed v1 app (versionCode 1). On launch the ApexHub SDK
 * auto-checks the LIVE production backend, which advertises v1.0.1, so the SDK
 * shows its "Update available" dialog. We assert the dialog text and capture a
 * screenshot as visual evidence that the app "asked" the user to update.
 */
@RunWith(AndroidJUnit4::class)
class OtaPromptInstrumentedTest {

    @Test
    fun updatePromptAppearsAndIsCaptured() {
        val instr = InstrumentationRegistry.getInstrumentation()
        val ctx = instr.targetContext
        val device = UiDevice.getInstance(instr)

        // Launch the app fresh.
        val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)!!
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ctx.startActivity(intent)

        // The SDK performs a network check on launch, then shows the dialog.
        val appeared = device.wait(Until.hasObject(By.textContains("Update available")), 40_000)

        // Capture a screenshot regardless, for evidence. Write to the app's
        // INTERNAL files dir so it can be pulled via `adb run-as` on any API level
        // (scoped storage blocks adb access to /sdcard/Android/data on API 30+).
        val outDir = File(ctx.filesDir, "screenshots").apply { mkdirs() }
        val shot = File(outDir, "update_prompt.png")
        device.takeScreenshot(shot)

        assertTrue("The 'Update available' dialog should appear on launch", appeared)
        assertTrue(
            "The dialog should announce version 1.0.1",
            device.hasObject(By.textContains("1.0.1"))
        )
        assertTrue("Screenshot should have been written", shot.exists() && shot.length() > 0)
    }
}
