package com.example.test.api

import android.content.Context
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap

class ApiInstMgr {
    companion object {
        val sUrlRetroMap = ConcurrentHashMap<String , Retrofit>()
        val sGson = GsonBuilder()
            .setLenient()
            .create()

        inline fun <InterfaceTypeClz> getInstnace(ctx: Context, serverUrl: String, apiInterfClz: Class<InterfaceTypeClz>): InterfaceTypeClz? {
            if (!sUrlRetroMap.containsKey(serverUrl)) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(serverUrl)
                    .client(Client.get(ctx))
                    .addConverterFactory(GsonConverterFactory.create(sGson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                    .addCallAdapterFactory(LiveDataCallAdapterFactory())
                    .build()
                sUrlRetroMap.put(serverUrl, retrofit)
            }
            return sUrlRetroMap.get(serverUrl)?.create(apiInterfClz)
        }

    }
}