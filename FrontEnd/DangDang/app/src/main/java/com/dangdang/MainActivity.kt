package com.dangdang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.AppRoute
import com.dangdang.data.manager.SessionManager
import com.dangdang.ui.navhost.AppNavHost
import com.dangdang.ui.theme.DangDangTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appPrefs: AppPrefs
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        ).launch {
            sessionManager.initialize()
        }

        setContent {
            DangDangTheme {
                val navController = rememberNavController()

                //로그인 콜백 시
                LaunchedEffect(Unit) {
                    sessionManager.logoutEvent.collect {
                        // 로그인 스크린으로 이동하며 백스택을 모두 비움
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    AppNavHost(
                        navController = navController,
                        appPrefs = appPrefs
                    )
                }
            }
        }
    }
}