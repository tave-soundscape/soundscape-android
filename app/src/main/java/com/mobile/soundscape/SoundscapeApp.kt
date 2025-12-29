package com.mobile.soundscape

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.mobile.soundscape.api.client.RetrofitClient

class SoundscapeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 앱이 시작될 때 RetrofitClient에 Context를 심어줍니다.
        RetrofitClient.init(this)

        // 카카오 globalApplication
        KakaoSdk.init(this, "23027bc8d10e3a8f7abcb09a61efdc13")


    }
}