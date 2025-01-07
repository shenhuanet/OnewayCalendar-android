package com.shenhua.onewaycalendar

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage

/**
 *
 * @since 2025/1/7
 * @author 倒挂草(daoguacao fuxh@igancao.com)
 */
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val (url, file) = this.source()
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = file.path,
                        contentDescription = "",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}