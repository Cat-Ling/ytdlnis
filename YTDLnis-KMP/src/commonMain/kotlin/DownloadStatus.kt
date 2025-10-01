sealed class DownloadStatus {
    data class Progress(val percentage: Float) : DownloadStatus()
    data class Success(val filePath: String) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
    object Idle : DownloadStatus()
}