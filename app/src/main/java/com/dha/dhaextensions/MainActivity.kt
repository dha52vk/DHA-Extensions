package com.dha.dhaextensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dha.dhaextensions.ui.theme.DHAExtensionsTheme
import java.io.File
import kotlin.reflect.KClass

class MainActivity : ComponentActivity() {
    var extensions = listOf(
        ExtensionInfo(name = "ZingMp3 Downloader", iconId = R.mipmap.zingmp3, activity = ZingMp3Activity::class),
        ExtensionInfo(name = "Youtube Downloader", iconId = R.mipmap.youtube, activity = YoutubeActivity::class),
        ExtensionInfo(name = "NhacCuaTui Downloader", iconId = R.mipmap.nhaccuatui, activity = NhacCuaTuiActivity::class)
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DHAExtensionsTheme {
                val context = applicationContext
                val closeAfterDownload = remember { mutableStateOf(true) }
                if (File(applicationContext.filesDir, "dontcloseafterdownload").exists()){
                    closeAfterDownload.value = false
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {
                        item {
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = {
                                    closeAfterDownload.value = toggleCloseAfterDownload(context)
                                })
                                .padding(16.dp)) {
                                Box(modifier = Modifier.size(70.dp)){
                                    Switch(
                                        modifier = Modifier.size(30.dp).align(Alignment.Center),
                                        checked = closeAfterDownload.value,
                                        onCheckedChange = {
                                            closeAfterDownload.value =
                                                toggleCloseAfterDownload(context)
                                        })
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "Đóng cửa sổ sau khi tải xong",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                        items(extensions.size) { index ->
                            val extension = extensions[index]
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = {
                                    startActivity(
                                        Intent(
                                            this@MainActivity,
                                            extension.activity.java
                                        )
                                    )
                                })
                                .padding(16.dp)) {
                                if (extension.iconId != null) {
                                    Image(
                                        modifier = Modifier.height(60.dp),
                                        painter = painterResource(extension.iconId!!),
                                        contentDescription = extension.name
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = extension.name,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

fun checkCloseAfterDownload(context: Context): Boolean {
    return !File(context.filesDir, "dontcloseafterdownload").exists()
}

fun toggleCloseAfterDownload(context: Context): Boolean {
    var file = File(context.filesDir, "dontcloseafterdownload")
    if (file.exists()){
        file.delete()
        return true;
    }else{
        file.createNewFile()
        return false
    }
}

data class ExtensionInfo(
    var name: String,
    var description: String = "",
    @DrawableRes var iconId: Int? = null,
    var activity: KClass<out Activity>
)