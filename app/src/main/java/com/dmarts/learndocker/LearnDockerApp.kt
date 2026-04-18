package com.dmarts.learndocker

import android.app.Application
import com.google.android.gms.ads.MobileAds

class LearnDockerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
        MobileAds.initialize(this)
    }
}
