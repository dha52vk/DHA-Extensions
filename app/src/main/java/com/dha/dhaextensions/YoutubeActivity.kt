package com.dha.dhaextensions

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.alpha
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

class YoutubeActivity : ComponentActivity() {
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
                val urlToLoad = "https://yt1d.com/en307/"


                if (sharedText != null) {
                    webView.loadUrl(urlToLoad)
                    isLoading.value = true
                }

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
                        }

                        AndroidView(factory = {
                            webView.apply {
                                visibility = View.GONE
                                settings.javaScriptEnabled = true
                                settings.setSupportMultipleWindows(true)
                                settings.domStorageEnabled = true
                                setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                                    val customTabsIntent = CustomTabsIntent.Builder()
                                        .setShowTitle(true)
                                        .build()

                                    customTabsIntent.launchUrl(context, url.toUri())
                                    Toast.makeText(context, "Đang tải về...", Toast.LENGTH_LONG).show()
                                    if (checkCloseAfterDownload(context)) finish()
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        view?.evaluateJavascript(
                                            "$(\"input#txt-url\")[0].value=\"${input.value}\"\n" +
                                                    "\$(\"a#btn-submit\").click();\n"
                                        ) { }
                                        view?.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }, update = {
                        })
                    }
                }
            }
        }
    }
}