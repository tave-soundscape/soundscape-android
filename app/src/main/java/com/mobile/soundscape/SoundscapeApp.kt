package com.mobile.soundscape

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.mobile.soundscape.api.client.RetrofitClient

class SoundscapeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        RetrofitClient.init(this)

        // 카카오 globalApplication
        KakaoSdk.init(this, "23027bc8d10e3a8f7abcb09a61efdc13")


    }
}