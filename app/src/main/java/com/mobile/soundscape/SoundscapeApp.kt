package com.mobile.soundscape

import android.app.Application
import com.mobile.soundscape.api.client.RetrofitClient

class SoundscapeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 앱이 시작될 때 RetrofitClient에 Context를 심어줍니다.
        RetrofitClient.init(this)
    }
}