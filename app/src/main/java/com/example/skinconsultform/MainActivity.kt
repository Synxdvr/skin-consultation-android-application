package com.example.skinconsultform

import android.os.Build
import android.os.Bundle
import androidx.activity.*
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.skinconsultform.ui.navigation.StheticNavGraph
import com.example.skinconsultform.ui.theme.StheticTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StheticTheme {
                StheticNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
    }
}