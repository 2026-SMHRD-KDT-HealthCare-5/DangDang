package com.dangdang.ui.viewmodel.first

import android.content.Context
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.Application.Companion.GoogleLoginKey
import com.dangdang.common.utils.AppPrefs
import com.dangdang.data.repository.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val appPrefs: AppPrefs,
    private val userRepository: UserRepository
) : ViewModel() {
    //이메일 로그인
    fun emailLogin(context: Context, onLoginSuccess: () -> Unit, email: String, password: String){
        viewModelScope.launch {
            val response = userRepository.emailLogin(email, password)

            if(response.isSuccessful){
                val responseBody = response.body()
                appPrefs.setAccessToken(responseBody?.accessToken?:"")
                appPrefs.setRefreshToken(responseBody?.refreshToken?:"")

                appPrefs.setAutoLogin(true)

                // 로그인 성공 후 상세 정보(알림 설정 등) 동기화
                syncUserInfo()

                onLoginSuccess()
            }else{
                Toast.makeText(
                    context,
                    "아이디 또는 비밀번호를 확인해주세요.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    //구글 로그인
    fun googleLogin(context: Context, onLoginSuccess: (isSignUp: Boolean) -> Unit){
        viewModelScope.launch {
            val idToken = getGoogleIdToken(context)?: ""

            val response = userRepository.googleLogin(idToken)

            if(response.isSuccessful){
                val responseBody = response.body()
                appPrefs.setAccessToken(responseBody?.accessToken?:"")
                appPrefs.setRefreshToken(responseBody?.refreshToken?:"")

                val isSignUp = responseBody?.user?.isSignUp

                appPrefs.setAutoLogin(isSignUp != true)

                // 로그인 성공 후 상세 정보(알림 설정 등) 동기화
                syncUserInfo()

                onLoginSuccess(isSignUp == true)
            }else{
                Toast.makeText(
                    context,
                    "로그인에 실패했습니다.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun getGoogleIdToken(context: Context): String? {
        val credentialManager = CredentialManager.create(context)

        // Google ID 로그인 옵션 설정
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // 처음 로그인하는 유저도 허용
            .setServerClientId(GoogleLoginKey)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            // 사용자에게 계정 선택 창 띄우기
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            // 결과에서 ID Token 추출
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                idToken
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    //카카오 로그인
    fun kakaoLogin(context: Context, onLoginSuccess: (isSignUp: Boolean) -> Unit){
        getKakaoLoginToken(
            context = context,
            onLoginSuccess = { token ->
                viewModelScope.launch {
                    val response = userRepository.kakaoLogin(token)

                    if(response.isSuccessful){
                        val responseBody = response.body()
                        appPrefs.setAccessToken(responseBody?.accessToken?:"")
                        appPrefs.setRefreshToken(responseBody?.refreshToken?:"")

                        val isSignUp = responseBody?.user?.isSignUp

                        appPrefs.setAutoLogin(isSignUp != true)

                        // 로그인 성공 후 상세 정보(알림 설정 등) 동기화
                        syncUserInfo()

                        onLoginSuccess(isSignUp == true)
                    }else{
                        Toast.makeText(
                            context,
                            "로그인에 실패했습니다.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    fun getKakaoLoginToken(context: Context, onLoginSuccess: (token: String) -> Unit){
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            // 카카오톡이 설치되어 있는 경우
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    // 로그인 실패 처리
                } else if (token != null) {
                    // 로그인 성공
                    onLoginSuccess(token.accessToken)
                }
            }
        } else {
            // 카카오톡이 없는 경우 카카오계정(웹)으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                if (error != null) {
                    // 로그인 실패 처리
                } else if (token != null) {
                    // 로그인 성공
                    onLoginSuccess(token.accessToken)
                }
            }
        }
    }

    private fun syncUserInfo() {
        viewModelScope.launch {
            val response = userRepository.getUserInfoDetail()
            if (response.isSuccessful) {
                response.body()?.let {
                    appPrefs.setNotificationEnabled(it.notification_enabled)
                }
            }
        }
    }
}
