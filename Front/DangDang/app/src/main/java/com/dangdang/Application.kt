package com.dangdang

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : Application(){
    companion object {
        //여기에 자주 쓰는 상수 기재
        const val REFRESH_PATH = "/auth/refresh"

        const val API_BASE_URL = "http://apiserver.com/"

        const val KakaoSdkKey = "14afeda3add41ff4b5a764e0853081f8"
    }

    override fun onCreate() {
        super.onCreate()
        // 카카오지도 초기화
        KakaoMapSdk.init(this, KakaoSdkKey)
    }
}