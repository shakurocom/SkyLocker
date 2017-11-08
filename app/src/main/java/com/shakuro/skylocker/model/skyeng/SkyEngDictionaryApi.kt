package com.shakuro.skylocker.model.skyeng

import com.shakuro.skylocker.model.skyeng.models.skyeng.SkyEngMeaning
import com.shakuro.skylocker.model.skyeng.models.skyeng.SkyEngWord
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject

interface SkyEngDictionaryApi {

    companion object {
        const val URL = "http://dictionary.skyeng.ru"

        fun create(): SkyEngDictionaryApi {
            return Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
                    .create(SkyEngDictionaryApi::class.java)
        }
    }

    @GET("/api/public/v1/words/search?_format=json")
    fun words(@Query("search") search: String): Call<MutableList<SkyEngWord>>

    @GET("/api/public/v1/meanings?_format=json")
    fun meanings(@Query("ids") ids: String): Call<MutableList<SkyEngMeaning>>
}