package com.example.test.api

import android.content.Context
import android.util.Log
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.android.gms.samples.vision.face.facetracker.BuildConfig
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class Client {

    companion object {
        val HTTP_LOG_TAG = "HttpLog"
        val DEFAULT_TIMEOUT = 30L

        var sClient: OkHttpClient? = null

        fun get(ctx: Context): OkHttpClient? {
            if (sClient == null) {
                val httpClientBuilder = OkHttpClient.Builder()

                // Tool/Profiling configuration
                configNetworkProfiling(ctx, httpClientBuilder)
                configureSSL(ctx, httpClientBuilder)

                // Normal configuration
                sClient = httpClientBuilder
                    .addInterceptor(object : Interceptor {
                        override fun intercept(chain: Interceptor.Chain): Response {
                            val request =
                                chain.request().newBuilder().addHeader("Connection", "close")
                                    .build()
                            return chain.proceed(request)
                        }
                    })
                    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .build()
            }
            return sClient
        }

        private fun configNetworkProfiling(ctx: Context, httpClientBuilder:OkHttpClient.Builder) {
            // Network Sniffer
            val HTTP_LOGGING_INTERCEPTOR =
                HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) {
                        Log.d(HTTP_LOG_TAG, message)
                    }
                })
            HTTP_LOGGING_INTERCEPTOR.level = HttpLoggingInterceptor.Level.BODY
            httpClientBuilder .addInterceptor(HTTP_LOGGING_INTERCEPTOR)

            // Stetho
            httpClientBuilder .addNetworkInterceptor(StethoInterceptor())

            // Network payload profiling
            val CHUNK_INTERCEPTOR = ChuckInterceptor(ctx).showNotification(BuildConfig.DEBUG)
            if(BuildConfig.DEBUG) {
                httpClientBuilder.addInterceptor(CHUNK_INTERCEPTOR)
            }


        }

        private fun configureSSL(ctx: Context, httpClientBuilder:OkHttpClient.Builder) {
            // SSL Configuration
            var sslSocketFactory: SSLSocketFactory? = null
            var trustAllCerts: Array<TrustManager>? = null
            try {
                // Create a trust manager that does not validate certificate chains
                trustAllCerts = arrayOf(object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<java.security.cert.X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<java.security.cert.X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                })

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                // Create an ssl socket factory with our all-trusting manager
                sslSocketFactory = sslContext.socketFactory
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (sslSocketFactory != null && trustAllCerts != null && trustAllCerts.isNotEmpty()) {
                httpClientBuilder.sslSocketFactory(
                    sslSocketFactory,
                    trustAllCerts[0] as X509TrustManager
                ).hostnameVerifier(HostnameVerifier { _, _ -> true })
            }
        }
    }
}