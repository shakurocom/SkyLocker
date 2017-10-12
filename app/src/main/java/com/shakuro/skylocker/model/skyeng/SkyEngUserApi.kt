package com.shakuro.skylocker.model.skyeng

import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface SkyEngUserApi {

    companion object {
        const val URL = "http://words.skyeng.ru"

        fun create(builder: Retrofit.Builder, converterFactory: Converter.Factory): SkyEngUserApi {
            return builder
                    .baseUrl(URL)
                    .addConverterFactory(converterFactory)
                    .build()
                    .create(SkyEngUserApi::class.java)
        }
    }

    @GET("/api/public/v1/users/meanings.json")
    fun userMeanings(@Query("email") email: String,
                     @Query("token") token: String): Call<List<SkyEngUserMeaning>>

    @PUT("/api/public/v1/words.json")
    fun putMeanings(@Query("email") email: String,
                    @Query("token") token: String,
                    @Field("meaningIds[]") meaningIds: ArrayList<Int>): Call<Unit>

    @GET("/api/public/v1/users/token.json")
    fun requestToken(@Query("email") email: String): Call<Unit>
}