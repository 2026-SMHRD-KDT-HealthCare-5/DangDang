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

        const val ExamplePictureUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQslryvsioHRQvLRob93TTh6aecy2XG7_hakwhdbzs2vQ&s=10"

        const val KakaoSdkKey = "14afeda3add41ff4b5a764e0853081f8"

        const val GoogleLoginKey = "1036782472267-k3sl6r7gotfjo52s62j1r2ld0mk6ci56.apps.googleusercontent.com"

        const val InquiryEmail = "songjesus1@gmail.com"
    }

    override fun onCreate() {
        super.onCreate()
        // 카카오지도 초기화
        KakaoMapSdk.init(this, KakaoSdkKey)
    }
}