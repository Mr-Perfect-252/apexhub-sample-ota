package com.apexhub.ota.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apexhub.sdk.UpdateCheckResult
import com.apexhub.ota.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * On launch the SDK auto-checks the ApexHub backend for a newer version and,
 * if one exists, shows the "Update available" prompt via [ApexHubUpdater.checkAndPrompt].
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val updater get() = SampleApplication.updater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvVersion.text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        binding.tvPackage.text = BuildConfig.APEXHUB_PACKAGE
        binding.tvKey.text     = BuildConfig.APEXHUB_PUBLIC_KEY.take(20) + "…"

        // The real integration: one call checks the backend and prompts if newer exists.
        lifecycleScope.launch {
            updater.checkAndPrompt(activity = this@MainActivity)
        }

        binding.btnCheckUpdate.setOnClickListener { checkForUpdateManually() }
    }

    private fun checkForUpdateManually() {
        binding.btnCheckUpdate.isEnabled = false
        binding.updateStatus.visibility = View.VISIBLE
        setStatus("Checking for updates…", StatusType.LOADING)

        lifecycleScope.launch {
            when (val result = updater.checkForUpdate()) {
                is UpdateCheckResult.UpdateAvailable -> {
                    val info = result.info
                    setStatus(
                        "Update available!\nVersion: ${info.latestVersion}\n${info.releaseNotes ?: ""}",
                        StatusType.SUCCESS
                    )
                }
                is UpdateCheckResult.UpToDate ->
                    setStatus("You're on the latest version.", StatusType.SUCCESS)
                is UpdateCheckResult.Error ->
                    setStatus("Check failed:\n${result.message}", StatusType.ERROR)
            }
            binding.btnCheckUpdate.isEnabled = true
        }
    }

    private enum class StatusType { LOADING, SUCCESS, ERROR }

    private fun setStatus(msg: String, type: StatusType) {
        binding.updateStatus.text = msg
        binding.updateStatus.setTextColor(
            when (type) {
                StatusType.SUCCESS -> getColor(R.color.apexhub_green)
                StatusType.ERROR   -> getColor(R.color.apexhub_red)
                StatusType.LOADING -> getColor(R.color.apexhub_gray)
            }
        )
    }
}
