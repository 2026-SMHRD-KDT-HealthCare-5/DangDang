package com.dangdang.component.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.kakao.vectormap.*
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.*
import com.kakao.vectormap.route.*

@Composable
fun KakaoMap(
    modifier: Modifier = Modifier,
    routePoints: List<Pair<Double, Double>> = emptyList()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var locationLabel by remember { mutableStateOf<Label?>(null) }
    var routeLine by remember { mutableStateOf<RouteLine?>(null) }
    var isInitialCameraSet by remember { mutableStateOf(false) }

    var isPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

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

    // RouteLine 업데이트
    LaunchedEffect(routePoints, kakaoMap) {
        val map = kakaoMap ?: return@LaunchedEffect
        if (routePoints.size < 2) {
            routeLine?.let {
                map.routeLineManager?.layer?.remove(it)
                routeLine = null
            }
            return@LaunchedEffect
        }

        val latLngs = routePoints.map { LatLng.from(it.first, it.second) }
        
        // 스타일 정의
        val routeStyle = RouteLineStyle.from(12f, Color.BLUE)
        val routeStyles = RouteLineStyles.from(routeStyle)
        val stylesSet = RouteLineStylesSet.from(routeStyles)
        
        val segment = RouteLineSegment.from(latLngs)
        // 스타일을 직접 설정하여 null 방지
        segment.setStyles(routeStyles)
        
        val currentLine = routeLine
        if (currentLine == null) {
            val options = RouteLineOptions.from(segment).setStylesSet(stylesSet)
            routeLine = map.routeLineManager?.layer?.addRouteLine(options)
        } else {
            currentLine.changeSegments(segment)
        }
    }

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
                    if (!isInitialCameraSet) {
                        map.moveCamera(CameraUpdateFactory.newCenterPosition(latLng))
                        isInitialCameraSet = true
                    }
                }
            }
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    val mapView = remember { MapView(context) }

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
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    start(
                        object : MapLifeCycleCallback() {
                            override fun onMapDestroy() { kakaoMap = null }
                            override fun onMapError(e: Exception?) { Log.e("KakaoMap", "Error", e) }
                        },
                        object : KakaoMapReadyCallback() {
                            override fun onMapReady(map: KakaoMap) {
                                kakaoMap = map
                                val labelManager = map.labelManager
                                val layer = labelManager?.layer
                                val styles = labelManager?.addLabelStyles(
                                    LabelStyles.from(LabelStyle.from(android.R.drawable.ic_menu_mylocation).setAnchorPoint(0.5f, 0.5f))
                                )
                                val options = LabelOptions.from(LatLng.from(37.402056, 127.108212)).setStyles(styles)
                                locationLabel = layer?.addLabel(options)

                                if (isPermissionGranted && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        location?.let {
                                            val currentLatLng = LatLng.from(it.latitude, it.longitude)
                                            locationLabel?.moveTo(currentLatLng)
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
