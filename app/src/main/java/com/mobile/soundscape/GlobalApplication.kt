package com.mobile.soundscape

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "23027bc8d10e3a8f7abcb09a61efdc13")
    }
}