package com.dha.dhaextensions

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.URLDecoder
import androidx.core.net.toUri

class NhacCuaTuiActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT)

        setContent {
            MaterialTheme {
                val input = remember { mutableStateOf(sharedText ?: "") }
                val links = remember { mutableStateListOf<String>() }
                val isLoading = remember { mutableStateOf(false) }
                val error = remember { mutableStateOf("") }

                val context = LocalContext.current
                val webView = remember { WebView(context) }
                val urlToLoad = "https://m.vuiz.net/getlink/nhaccuatui/"


                if (sharedText != null) {
                    webView.loadUrl(urlToLoad)
                    isLoading.value = true
                }

                AndroidView(factory = {
                    webView.apply {
                        visibility = View.GONE
                        settings.javaScriptEnabled = true
                        settings.setSupportMultipleWindows(true)
                        settings.domStorageEnabled = true
                        setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                            val request = DownloadManager.Request(url.toUri())
                            request.setMimeType(mimeType)
                            request.addRequestHeader("File-downloader", userAgent)
                            request.setDescription("Downloading file...")
                            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
                            request.allowScanningByMediaScanner()
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType))
                            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            dm.enqueue(request)
                            Toast.makeText(context, "Đang tải về...", Toast.LENGTH_LONG).show()
                            if (checkCloseAfterDownload(context)) finish()
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                view?.evaluateJavascript(
                                    "$(\"#link\")[0].value = \"${input.value}\";\n" +
                                            "$(\"#submit\")[0].click();\n"
                                ) { }

                                var s: String? = null
                                lifecycleScope.launch {
                                    while (!isLoading.value || s == null || s == "null") {
                                        delay(300)
                                        s = getUrls(webView)
                                    }
                                    links.clear()
                                    val jsonArray = JSONArray(s)
                                    for (i in 0 until jsonArray.length()) {
                                        links.add(jsonArray.getString(i))
                                    }
                                    error.value = ""
                                    isLoading.value = false
                                }
                            }
                        }
                    }
                }, update = {
                })

                Spacer(modifier = Modifier.height(16.dp))

                Dialog(
                    onDismissRequest = { finish() },
                    properties = DialogProperties(
                        dismissOnClickOutside = true,
                        dismissOnBackPress = true,
                        usePlatformDefaultWidth = false
                    )
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .wrapContentHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OutlinedTextField(
                                label = { Text("Link") },
                                value = input.value,
                                onValueChange = { input.value = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                modifier = Modifier.padding(top = 16.dp),
                                onClick = {
                                    webView.loadUrl(urlToLoad)
                                    isLoading.value = true
                                }, enabled = !isLoading.value) {
                                if (!isLoading.value) {
                                    Text("Lấy link")
                                }else{
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                thickness = 2.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            if (error.value.isNotEmpty()) {
                                Text(error.value, color = Color.Red, textAlign = TextAlign.Center)
                            }
                            links.forEachIndexed() { ind, value ->
                                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text(modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .weight(1f),
                                        text = URLDecoder.decode(
                                            value.substringAfterLast("/").substringAfter("&filename=").substringBefore(".mp3")
                                        ) + "(${ind + 1})"
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Button(modifier = Modifier.wrapContentWidth(), onClick = {
                                        webView.loadUrl(value)
                                    }) {
                                        Text(text = "Tải về")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun getUrls(webView: WebView): String? {
        var list: String? = null;
        var hasResult = false;
        webView.evaluateJavascript(
            "(function () {\n" +
                    "  var gmenu = \$(\".gmenu\")[0];\n" +
                    "  if (\n" +
                    "    \$(\"#DownloadLink\")[0].children.length > 0 &&\n" +
                    "    (!gmenu || gmenu.children[0].tagName.toLowerCase() !== \"img\")\n" +
                    "  ) {\n" +
                    "    const links = \$(\"a[rel~='noreferrer']\")\n" +
                    "      .get()\n" +
                    "      .map((a) => a.href);\n" +
                    "    return JSON.stringify(links);\n" +
                    "  }else return null\n" +
                    "})();\n"
        ) { jsResult ->
            try {
                list = jsResult.removeSurrounding("\"").replace("\\\"", "\"")
                hasResult = true
            } catch (e: Exception) {
                list = null;
            }
        }
        while (!hasResult) {
            delay(300)
        }
        return list;
    }
}