import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

expect class Downloader {
    fun download(url: String): Flow<DownloadStatus>
}

@Composable
expect fun rememberDownloader(): Downloader