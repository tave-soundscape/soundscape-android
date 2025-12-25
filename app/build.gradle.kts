plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin") version "2.7.5"
}

android {
    namespace = "com.mobile.soundscape"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mobile.soundscape"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["redirectSchemeName"] = "com.mobile.soundscape"
        manifestPlaceholders["redirectHostName"] = "callback"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // 바인딩 활성화
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Gson
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // OkHttp (Retrofit이 내부적으로 사용) 및 Logging Interceptor (API 통신 로그 확인용)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // 브라우저 사용
    implementation("androidx.browser:browser:1.8.0")


    // Spotify 음악 재생 라이브러리 (App Remote SDK)
    // implementation("com.spotify.android:auth:1.2.6")
    // implementation("com.spotify.android:app-remote:0.8.0")
    implementation(files("../spotify-app-remote-release-0.8.0.aar"))
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(files("../spotify-auth-release-2.1.0.aar"))
    implementation("jp.wasabeef:glide-transformations:4.3.0")


}