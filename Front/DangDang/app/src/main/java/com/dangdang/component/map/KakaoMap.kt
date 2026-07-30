package com.dangdang.component.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.label.LabelTextStyle

@Preview
@Composable
fun KakaoMapPreview(){
    KakaoMap()
}

@Composable
fun KakaoMap(

){
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // 지도의 상태 관리
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var locationLabel by remember { mutableStateOf<Label?>(null) }
    var isInitialCameraSet by remember { mutableStateOf(false) }

    // 권한 상태 관리
    var isPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 위치 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isPermissionGranted) {
            Log.d("KakaoMap", "위치 권한 허용됨")
        }
    }

    // 초기 권한 요청
    LaunchedEffect(Unit) {
        if (!isPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // 실시간 위치 업데이트 및 마커 이동
    DisposableEffect(kakaoMap, locationLabel, isPermissionGranted) {
        val map = kakaoMap
        val label = locationLabel
        if (map == null || label == null || !isPermissionGranted) return@DisposableEffect onDispose {}

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val latLng = LatLng.from(location.latitude, location.longitude)
                    label.moveTo(latLng)

                    // 초기 1회만 현위치로 카메라 이동
                    if (!isInitialCameraSet) {
                        map.moveCamera(CameraUpdateFactory.newCenterPosition(latLng))
                        isInitialCameraSet = true
                    }
                }
            }
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // MapView를 remember로 관리
    val mapView = remember { MapView(context) }

    // 생명주기 연결
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ){
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    start(
                        object : MapLifeCycleCallback() {
                            override fun onMapDestroy() {
                                kakaoMap = null
                            }
                            override fun onMapError(e: Exception?) {
                            }
                        },
                        object : KakaoMapReadyCallback() {
                            override fun onMapReady(map: KakaoMap) {
                                kakaoMap = map

                                val labelManager = map.labelManager
                                val layer = labelManager?.layer

                                val styles = labelManager?.addLabelStyles(
                                    LabelStyles.from(
                                        LabelStyle.from(android.R.drawable.ic_menu_mylocation)
                                            .setAnchorPoint(0.5f, 0.5f)
                                    )
                                )

                                // 현위치 라벨 생성
                                val options = LabelOptions.from(LatLng.from(37.402056, 127.108212))
                                    .setStyles(styles)
                                val label = layer?.addLabel(options)
                                locationLabel = label

                                // 마지막으로 확인된 위치를 즉시 가져와서 마커를 이동시키고 카메라를 1회 이동시킴
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        location?.let {
                                            val currentLatLng = LatLng.from(it.latitude, it.longitude)
                                            label?.moveTo(currentLatLng)

                                            if (!isInitialCameraSet) {
                                                map.moveCamera(CameraUpdateFactory.newCenterPosition(currentLatLng))
                                                isInitialCameraSet = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        )
    }
}