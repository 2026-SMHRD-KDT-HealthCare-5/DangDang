import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("kotlin-parcelize")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use {
        localProperties.load(it)
    }
}

android {
    namespace = "com.dangdang"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.dangdang"
        minSdk = 27
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "KAKAO_NATIVE_APP_KEY",
            "\"${localProperties.getProperty("KAKAO_NATIVE_APP_KEY", "")}\""
        )

        buildConfigField(
            "String",
            "InquiryEmail",
            "\"${localProperties.getProperty("InquiryEmail", "")}\""
        )

        buildConfigField(
            "String",
            "GoogleLoginKey",
            "\"${localProperties.getProperty("GoogleLoginKey", "")}\""
        )

        buildConfigField(
            "String",
            "ExamplePictureUrl",
            "\"${localProperties.getProperty("ExamplePictureUrl", "")}\""
        )

        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"${localProperties.getProperty("API_BASE_URL", "")}\""
        )

        manifestPlaceholders["KAKAO_AUTH_SCHEME"] =
            "kakao${localProperties.getProperty("KAKAO_NATIVE_APP_KEY", "")}"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // Retrofit & Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // ViewModel & Compose Integration
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.logging.interceptor)

    //wearable
    implementation(libs.androidx.compose.material)

    debugImplementation(
        libs.androidx.wear.tooling.preview
    )

    //health connect
    implementation(libs.androidx.connect.client)

    //kakao map
    implementation(libs.v2.maps)

    implementation(libs.play.services.location)

    //카카오 로그인
    implementation(libs.v2.user)

    //chart
    implementation(libs.compose)
    implementation(libs.compose.m3)

    //lifecycle runtime compose
    implementation(libs.androidx.lifecycle.runtime.compose)

    //이미지 미리보기 시
    implementation(libs.coil3.coil.compose)

    //datastore preference
    implementation(libs.androidx.datastore.preferences)
}