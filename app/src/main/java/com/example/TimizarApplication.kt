package com.example

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class TimizarApplication : Application(), ImageLoaderFactory {

    companion object {
        private const val TAG = "TimizarApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "TimizarApplication initialized with robust Coil ImageLoader and OkHttp logging.")
    }

    override fun newImageLoader(): ImageLoader {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("CoilOkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestWithUserAgent = originalRequest.newBuilder()
                    .header("User-Agent", "TimizarGo-AndroidApp/1.0 (Tourism; Draa-Tafilalet)")
                    .build()
                chain.proceed(requestWithUserAgent)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024L * 1024L) // 50MB disk cache
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}
