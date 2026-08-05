package com.pocketlegal.advice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pocketlegal.advice.presentation.navigation.AppNavGraph
import com.pocketlegal.advice.presentation.theme.PocketLawyerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketLawyerTheme {
                AppNavGraph()
            }
        }
    }
}
