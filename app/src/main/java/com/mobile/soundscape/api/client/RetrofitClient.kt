package com.mobile.soundscape.api.client

import com.mobile.soundscape.api.apis.EvaluationApi
import com.mobile.soundscape.api.apis.LoginApi
import com.mobile.soundscape.api.apis.OnboardingApi
import com.mobile.soundscape.api.apis.RecommendationApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java
import android.annotation.SuppressLint
import android.content.Context
import com.mobile.soundscape.api.apis.ExploreApi
import com.mobile.soundscape.api.apis.LibraryApi
import com.mobile.soundscape.api.apis.MypageApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {

    private const val BASE_URL = "https://soundscape.higu.kr/"

    // Context를 저장할 변수
    private lateinit var appContext: Context

    // 앱이 켜질 때(Application Class)에서 이 함수를 호출
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // OkHttpClient 생성
    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(appContext))
            .addInterceptor(logging)
            .build()
    }

    // Retrofit 객체 생성
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 위에서 만든 retrofit 객체를 재사용해서 여러개의 api 생성
    // 로그인 api
    val loginApi: LoginApi by lazy {
        retrofit.create(LoginApi::class.java)
    }

    // 온보딩 api (서버로 보내는 request만 있음)
    val onboardingApi: OnboardingApi by lazy {
        retrofit.create(OnboardingApi::class.java)
    }

    // (노래추천) 장소, 데시벨, 목표 api
    val recommendationApi: RecommendationApi by lazy {
        retrofit.create(RecommendationApi::class.java)
    }

    // 리스트 평가 api
    val evaluationApi: EvaluationApi by lazy {
        retrofit.create(EvaluationApi::class.java)
    }

    // 수정용 api
    val mypageApi: MypageApi by lazy {
        retrofit.create(MypageApi::class.java)
    }

    // 라이브러리에서 사용자가 저장한 플리 가져오는 api
     val libraryApi: LibraryApi by lazy {
         retrofit.create(LibraryApi::class.java)
    }

    val exploreApi: ExploreApi by lazy {
        retrofit.create(ExploreApi::class.java)
    }
}