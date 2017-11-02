package com.shakuro.skylocker.model.skyeng

import com.shakuro.skylocker.model.models.skyeng.SkyEngError
import com.shakuro.skylocker.model.models.skyeng.SkyEngUserMeaning
import com.squareup.moshi.Moshi
import retrofit2.Response


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



