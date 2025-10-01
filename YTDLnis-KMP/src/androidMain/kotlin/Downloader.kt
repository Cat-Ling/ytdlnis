import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.chaquo.python.PyException
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

actual class Downloader(private val context: Context) {
    actual fun download(url: String): Flow<DownloadStatus> = flow {
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(context))
            }
            val request = YoutubeDLRequest(url)
            // Add any necessary options to the request
            // request.addOption("-f", "best")

            YoutubeDL.getInstance().execute(request) { progress ->
                emit(DownloadStatus.Progress(progress))
            }
            // I can't get the file path from the library directly, so I'll just emit a generic success message.
            emit(DownloadStatus.Success("Download complete"))
        } catch (e: PyException) {
            emit(DownloadStatus.Error(e.message ?: "A Python error occurred"))
        } catch (e: Exception) {
            emit(DownloadStatus.Error(e.message ?: "An unknown error occurred"))
        }
    }
}

@Composable
actual fun rememberDownloader(): Downloader {
    val context = LocalContext.current
    return remember { Downloader(context) }
}