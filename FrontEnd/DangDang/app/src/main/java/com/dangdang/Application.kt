package com.dangdang

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : Application(){
    companion object {
        //여기에 자주 쓰는 상수 기재
        const val REFRESH_PATH = "/api/auth/refresh"

        const val API_BASE_URL = BuildConfig.API_BASE_URL

        const val ExamplePictureUrl = BuildConfig.ExamplePictureUrl

        const val KakaoSdkKey = BuildConfig.KAKAO_NATIVE_APP_KEY

        const val GoogleLoginKey = BuildConfig.GoogleLoginKey

        const val InquiryEmail = BuildConfig.InquiryEmail

        const val AuthPath = "/api/auth"
        const val AnalyzePath = "/api/intake-logs"
        const val ChatPath = "/api/chat"
    }

    override fun onCreate() {
        super.onCreate()
        // 카카오지도 초기화
        KakaoMapSdk.init(this, KakaoSdkKey)
        KakaoSdk.init(this, KakaoSdkKey)
    }
}