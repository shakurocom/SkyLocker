package com.shakuro.skylocker.model.skyeng

import com.shakuro.skylocker.model.skyeng.models.skyeng.SkyEngUserMeaning
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface SkyEngUserApi {

    companion object {
        const val URL = "http://words.skyeng.ru"

        fun create(): SkyEngUserApi {
            return Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(MoshiConverterFactory.create())
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