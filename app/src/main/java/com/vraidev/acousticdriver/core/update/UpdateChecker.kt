package com.vraidev.acousticdriver.core.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val releasePageUrl: String,
    val isUpdateAvailable: Boolean
)

object UpdateChecker {
    private const val GITHUB_API_URL = "https://api.github.com/repos/vrai17/AcousticDriver/releases/latest"
    const val CURRENT_VERSION = "1.0.0"

    suspend fun checkForUpdates(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "AcousticDriver-Android")
                connectTimeout = 6000
                readTimeout = 6000
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val json = Json { ignoreUnknownKeys = true }
                val root = json.parseToJsonElement(jsonString).jsonObject

                val tagName = root["tag_name"]?.jsonPrimitive?.content ?: ""
                val releaseTitle = root["name"]?.jsonPrimitive?.content ?: tagName
                val releaseNotes = root["body"]?.jsonPrimitive?.content ?: ""
                val htmlUrl = root["html_url"]?.jsonPrimitive?.content ?: "https://github.com/vrai17/AcousticDriver"

                // Search for .apk asset in GitHub release
                var apkUrl = htmlUrl
                val assets = root["assets"]?.jsonArray
                assets?.forEach { assetElement ->
                    val assetObj = assetElement.jsonObject
                    val name = assetObj["name"]?.jsonPrimitive?.content ?: ""
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = assetObj["browser_download_url"]?.jsonPrimitive?.content ?: apkUrl
                    }
                }

                val cleanLatest = tagName.trimStart('v', 'V')
                val isAvailable = compareVersions(cleanLatest, CURRENT_VERSION) > 0

                Result.success(
                    UpdateInfo(
                        latestVersion = tagName,
                        releaseTitle = releaseTitle,
                        releaseNotes = releaseNotes,
                        downloadUrl = apkUrl,
                        releasePageUrl = htmlUrl,
                        isUpdateAvailable = isAvailable
                    )
                )
            } else if (responseCode == 404) {
                // Repository exists but no releases published yet
                Result.success(
                    UpdateInfo(
                        latestVersion = "v$CURRENT_VERSION",
                        releaseTitle = "Acoustic Driver v$CURRENT_VERSION",
                        releaseNotes = "You are currently running the initial release.",
                        downloadUrl = "https://github.com/vrai17/AcousticDriver",
                        releasePageUrl = "https://github.com/vrai17/AcousticDriver",
                        isUpdateAvailable = false
                    )
                )
            } else {
                Result.failure(Exception("GitHub API returned HTTP $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openUpdateUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = v2.split(".").mapNotNull { it.toIntOrNull() }
        val length = maxOf(parts1.size, parts2.size)
        for (i in 0 until length) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }
}
