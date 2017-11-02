package com.shakuro.skylocker.model.skyeng

import com.shakuro.skylocker.model.models.skyeng.SkyEngMeaning
import com.shakuro.skylocker.model.models.skyeng.SkyEngWord
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface SkyEngDictionaryApi {

    companion object {
        const val URL = "http://dictionary.skyeng.ru"

        fun create(builder: Retrofit.Builder, converterFactory: Converter.Factory): SkyEngDictionaryApi {
            return builder
                    .baseUrl(URL)
                    .addConverterFactory(converterFactory)
                    .build()
                    .create(SkyEngDictionaryApi::class.java)
        }
    }

    @GET("/api/public/v1/words/search?_format=json")
    fun words(@Query("search") search: String): Call<MutableList<SkyEngWord>>

    @GET("/api/public/v1/meanings?_format=json")
    fun meanings(@Query("ids") ids: String): Call<MutableList<SkyEngMeaning>>
}