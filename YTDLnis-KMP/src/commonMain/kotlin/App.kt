import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// A simple data class to hold download information
data class DownloadItem(val url: String, val status: DownloadStatus)

@Composable
fun App() {
    val downloader = rememberDownloader()
    var url by remember { mutableStateOf("") }
    val downloads = remember { mutableStateListOf<DownloadItem>() }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Video URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (url.isNotBlank()) {
                    val newItem = DownloadItem(url, DownloadStatus.Idle)
                    downloads.add(newItem)
                    val itemIndex = downloads.size - 1

                    coroutineScope.launch {
                        downloader.download(newItem.url).collect { status ->
                            downloads[itemIndex] = downloads[itemIndex].copy(status = status)
                        }
                    }
                    url = ""
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Download")
        }
        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            itemsIndexed(downloads) { index, item ->
                DownloadCard(item)
            }
        }
    }
}

@Composable
fun DownloadCard(item: DownloadItem) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.url, style = MaterialTheme.typography.body2)
                when (val status = item.status) {
                    is DownloadStatus.Progress -> {
                        LinearProgressIndicator(
                            progress = status.percentage / 100f,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                        Text("${"%.2f".format(status.percentage)}%", style = MaterialTheme.typography.caption)
                    }
                    is DownloadStatus.Success -> {
                        Text("Completed: ${status.filePath}", color = MaterialTheme.colors.primary)
                    }
                    is DownloadStatus.Error -> {
                        Text("Error: ${status.message}", color = MaterialTheme.colors.error)
                    }
                    DownloadStatus.Idle -> {
                        Text("Queued...", style = MaterialTheme.typography.caption)
                    }
                }
            }
        }
    }
}