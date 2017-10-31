package com.shakuro.skylocker.model.skyeng

import com.squareup.moshi.Moshi
import retrofit2.Response

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


class SkyEngApi(val dictionaryApi: SkyEngDictionaryApi, val userApi: SkyEngUserApi) {

    companion object {

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
}



