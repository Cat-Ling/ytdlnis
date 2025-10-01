import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

actual class Downloader {
    actual fun download(url: String): Flow<DownloadStatus> = callbackFlow {
        try {
            val process = ProcessBuilder(
                "yt-dlp",
                "--progress",
                "--newline",
                "-o",
                "%(title)s.%(ext)s",
                url
            )
                .directory(File(System.getProperty("user.home"), "Downloads"))
                .redirectErrorStream(true)
                .start()

            process.inputStream.bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val progress = parseProgress(line)
                    if (progress != null) {
                        trySend(DownloadStatus.Progress(progress))
                    }
                }
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                trySend(DownloadStatus.Success("Download complete"))
            } else {
                trySend(DownloadStatus.Error("Download failed with exit code $exitCode"))
            }
        } catch (e: Exception) {
            trySend(DownloadStatus.Error(e.message ?: "An unknown error occurred"))
        }
        close()
    }

    private fun parseProgress(line: String?): Float? {
        if (line == null || !line.contains("%")) {
            return null
        }
        val progressString = line.substringAfter("[download]").trim().substringBefore("%")
        return progressString.toFloatOrNull()
    }
}

@Composable
actual fun rememberDownloader(): Downloader {
    return remember { Downloader() }
}