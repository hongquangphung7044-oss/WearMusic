package com.example.wearmusic

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WearMusicApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
