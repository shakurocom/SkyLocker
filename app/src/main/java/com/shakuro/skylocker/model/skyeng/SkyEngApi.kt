package com.shakuro.skylocker.model.skyeng

import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query
import com.squareup.moshi.Moshi



data class SkyEngTranslation(val text: String?)

data class SkyEngAlternativeTranslation(
        val text: String?,
        val translation: SkyEngTranslation?)

data class SkyEngUserMeaning(val meaningId: Long, val progress: Float)

data class SkyEngMeaning(
        val id: Long,
        val text: String?,
        val wordId: Long?,
        val translation: SkyEngTranslation?,
        val definition: SkyEngTranslation?,
        val alternativeTranslations: MutableList<SkyEngAlternativeTranslation>?)

data class SkyEngWord(
        val id: Long,
        val text: String,
        val meanings: MutableList<SkyEngMeaning>)


data class SkyEngErrorMessage(val message: String?)
data class SkyEngError(val errors: MutableList<SkyEngErrorMessage>?)


interface SkyEngDictionaryApi {

    @GET("/api/public/v1/words/search?_format=json")
    fun words(@Query("search") search: String): Call<MutableList<SkyEngWord>>

    @GET("/api/public/v1/meanings?_format=json")
    fun meanings(@Query("ids") ids: String): Call<MutableList<SkyEngMeaning>>
}

interface SkyEngUserApi {

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

object SkyEngApi {
    val dictionaryApi: SkyEngDictionaryApi by lazy {
        Retrofit.Builder()
                .baseUrl("http://dictionary.skyeng.ru")
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(SkyEngDictionaryApi::class.java)
    }

    val userApi: SkyEngUserApi by lazy {
        Retrofit.Builder()
                .baseUrl("http://words.skyeng.ru")
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(SkyEngUserApi::class.java)
    }

    fun handleUserMeaningsError(response: Response<List<SkyEngUserMeaning>>?): Throwable {
        var result: Throwable = Error("No response")
        if (response != null) {
            result = Error("Error: ${response.code()}")
            if (response.errorBody() != null) {
                try {
                    val string = response.errorBody()?.string()
                    val adapter = Moshi.Builder().build().adapter<SkyEngError>(SkyEngError::class.java)
                    val error = adapter.fromJson(string)
                    val message = error.errors?.firstOrNull()
                    message?.let {
                        result = Error(it.message)
                    }
                } catch (e: Throwable) {
                    println("Error: " + e.localizedMessage)
                }
            }
        }
        return result
    }
}



