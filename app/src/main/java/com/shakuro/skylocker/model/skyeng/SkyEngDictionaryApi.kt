package com.shakuro.skylocker.model.skyeng

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SkyEngDictionaryApi {

    companion object {
        const val URL = "http://dictionary.skyeng.ru"
    }

    @GET("/api/public/v1/words/search?_format=json")
    fun words(@Query("search") search: String): Call<MutableList<SkyEngWord>>

    @GET("/api/public/v1/meanings?_format=json")
    fun meanings(@Query("ids") ids: String): Call<MutableList<SkyEngMeaning>>
}