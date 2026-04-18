package com.dmarts.learndocker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dmarts.learndocker.ads.InterstitialAdManager
import com.dmarts.learndocker.ads.RewardedAdManager
import com.dmarts.learndocker.navigation.AppNavHost
import com.dmarts.learndocker.ui.theme.LearndockerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RewardedAdManager.preload(this)
        InterstitialAdManager.preload(this)
        setContent {
            LearndockerTheme {
                AppNavHost()
            }
        }
    }
}
