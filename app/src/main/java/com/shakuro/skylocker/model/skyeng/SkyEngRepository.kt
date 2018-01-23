package com.shakuro.skylocker.model.skyeng

import android.content.Context
import com.shakuro.skylocker.R
import com.shakuro.skylocker.model.skyeng.models.db.*
import com.shakuro.skylocker.model.skyeng.models.skyeng.SkyEngError
import com.shakuro.skylocker.model.skyeng.models.skyeng.SkyEngMeaning
import com.shakuro.skylocker.model.skyeng.models.skyeng.SkyEngUserMeaning
import com.shakuro.skylocker.model.skyeng.models.skyeng.SkyEngWord
import com.squareup.moshi.Moshi
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class SkyEngRepository @Inject constructor(private val dictionaryApi: SkyEngDictionaryApi,
                                           private val userApi: SkyEngUserApi,
                                           private val daoSession: DaoSession) {
    private val MIN_ALTERNATIVES_COUNT = 3

    companion object {
        const val DB_FILE_NAME = "skylocker-db"
    }

    fun refreshUserMeanings(email: String, token: String, callback: (User?, Throwable?) -> Unit) {
        userApi.userMeanings(email, token).enqueue(object : Callback<List<SkyEngUserMeaning>> {

            override fun onResponse(call: Call<List<SkyEngUserMeaning>>?, response: Response<List<SkyEngUserMeaning>>?) {
                try {
                    if (response?.isSuccessful == true) {
                        val user = updateActiveUser(email, token)

                        val allUserMeanings = mutableListOf<Long>()
                        val userMeaningsToLoad = mutableListOf<Long>()

                        val meaningExistsQuery = daoSession.meaningDao.queryBuilder().where(MeaningDao.Properties.Id.eq(0)).buildCount()
                        response.body()?.forEach {
                            allUserMeanings.add(it.meaningId)

                            meaningExistsQuery.setParameter(0, it.meaningId)
                            if (meaningExistsQuery.count() == 0L) {
                                userMeaningsToLoad.add(it.meaningId)
                            }
                        }

                        assingUserMeanings(user, allUserMeanings)

                        if (userMeaningsToLoad.size > 0) {
                            loadUserMeanings(user, userMeaningsToLoad) { error ->
                                callback(user, error)
                            }
                        } else {
                            callback(user, null)
                        }
                    } else {
                        val error = handleUserMeaningsError(response)
                        callback(null, error)
                    }
                } catch (e: Exception) {
                    callback(null, e)
                }
            }

            override fun onFailure(call: Call<List<SkyEngUserMeaning>>?, t: Throwable?) {
                callback(null, t)
            }
        })
    }

    fun requestUserMeaningsUpdate() {
        activeUser()?.let {
            println("Refreshing started")
            refreshUserMeanings(it.email, it.token) { _, error ->
                println("Refresh ended")
                error?.let {
                    println("Error: ${error.localizedMessage}")
                }
            }
        }
    }

    fun randomMeaning(useTop1000Words: Boolean, useUserWords: Boolean): Meaning? {
        val user = activeUser()
        val builder = daoSession.meaningDao.queryBuilder()

        when {
            useTop1000Words && !useUserWords -> builder.where(MeaningDao.Properties.AddedByUserWithId.eq(0))
            !useTop1000Words && (useUserWords && user != null) -> builder.where(MeaningDao.Properties.AddedByUserWithId.eq(user.id))
            !useTop1000Words && (!useUserWords || user == null) -> builder.where(MeaningDao.Properties.AddedByUserWithId.eq(-1))
        }
        builder.orderRaw("RANDOM()").limit(1).build()
        return builder.list()?.firstOrNull()
    }

    fun meaningsExist(useTop1000Words: Boolean, useUserWords: Boolean): Boolean {
        return (randomMeaning(useTop1000Words, useUserWords) != null)
    }

    fun activeUser(): User? {
        return daoSession.userDao.loadAll()?.firstOrNull()
    }

    fun disconnectActiveUser() {
        daoSession.userDao.deleteAll()

        val userColumnName = MeaningDao.Properties.AddedByUserWithId.columnName
        val sql = "update ${MeaningDao.TABLENAME} set $userColumnName = 0 where $userColumnName > 0"
        daoSession.database.execSQL(sql)

        daoSession.clear()
    }

    fun requestToken(email: String, callback: (Throwable?) -> Unit) {
        userApi.requestToken(email).enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>?, response: Response<Unit>?) {
                callback(null)
            }

            override fun onFailure(call: Call<Unit>?, error: Throwable?) {
                callback(error)
            }
        })
    }

    private fun loadUserMeanings(user: User?, toLoad: MutableList<Long>, callback: (Throwable?) -> Unit) {
        val ids = toLoad.joinToString()
        dictionaryApi.meanings(ids).enqueue(object : Callback<MutableList<SkyEngMeaning>> {

            override fun onResponse(call: Call<MutableList<SkyEngMeaning>>?, response: Response<MutableList<SkyEngMeaning>>?) {
                daoSession.runInTx {
                    response?.body()?.forEach {
                        saveWord(null, it, user, false)
                    }
                }
                callback(null)
            }

            override fun onFailure(call: Call<MutableList<SkyEngMeaning>>?, t: Throwable?) {
                callback(t)
            }
        })
    }

    private fun assingUserMeanings(user: User?, userMeaningsIds: MutableList<Long>) {
        if (user == null) {
            return
        }
        daoSession.runInTx {
            // clean no more user meanings
            val noMoreUserMeanings = daoSession.meaningDao.queryBuilder().where(
                    MeaningDao.Properties.AddedByUserWithId.eq(user.id),
                    MeaningDao.Properties.Id.notIn(userMeaningsIds)
            ).list()
            noMoreUserMeanings.forEach {
                it.addedByUserWithId = 0
                it.update()
            }

            // bind already existing meanings to user
            val userMeanings = daoSession.meaningDao.queryBuilder().where(
                    MeaningDao.Properties.AddedByUserWithId.notEq(user.id),
                    MeaningDao.Properties.Id.`in`(userMeaningsIds)
            ).list()
            userMeanings.forEach {
                it.addedByUserWithId = user.id
                it.update()
            }
        }
    }

    private fun updateActiveUser(email: String, token: String): User? {
        var user = activeUser()
        if (user != null) {
            user.email = email
            user.token = token
            daoSession.userDao.update(user)
        } else {
            user = User(null, email, token)
            daoSession.userDao.insert(user)
        }
        return user
    }

    private data class SearchWordResult(val skyEngWord: SkyEngWord?, val skyEngMeaning: SkyEngMeaning?)

    private fun searchWord(search: String): SearchWordResult {
        val words = dictionaryApi.words(search).execute().body()
        val word = words?.filter { search.equals(it.text, true) }?.first()

        var meaning: SkyEngMeaning? = null
        word?.meanings?.first()?.let {
            meaning = dictionaryApi.meanings(it.id.toString()).execute().body()?.first()
        }
        return SearchWordResult(word, meaning)
    }

    private fun saveWord(skyEngWord: SkyEngWord?, skyEngMeaning: SkyEngMeaning, user: User? = null, inTransaction: Boolean = true) {
        val alternatives = skyEngMeaning.alternativeTranslations
        if (alternatives != null && alternatives.size >= MIN_ALTERNATIVES_COUNT) {
            val saveRunnable = Runnable {
                alternatives.forEach {
                    if (it.text != null && it.translation?.text != null) {
                        val alternative = Alternative(null, it.text, it.translation.text, skyEngMeaning.id)
                        daoSession.insert(alternative)
                    }
                }
                val meaning = Meaning()
                with(meaning) {
                    id = skyEngMeaning.id
                    wordId = skyEngWord?.id ?: skyEngMeaning.wordId
                    text = skyEngMeaning.text
                    translation = skyEngMeaning.translation?.text
                    definition = skyEngMeaning.definition?.text
                    user?.let {
                        addedByUserWithId = user.id
                    }
                }
                daoSession.insertOrReplace(meaning)
                println("${meaning.id} ${meaning.text}")
            }

            if (inTransaction) {
                daoSession.runInTx(saveRunnable)
            } else {
                saveRunnable.run()
            }
        }
    }

    /**
     * This function used only on development step to fill default db with N most used english words
     */
    @Suppress("unused")
    fun fillWithWords(words: List<String>, callback: () -> Unit) = async(CommonPool) {
        words.forEach {
            val exists = daoSession.meaningDao
                    .queryBuilder()
                    .where(MeaningDao.Properties.Text.like(it))
                    .count() > 0L
            if (!exists) {
                try {
                    val (skyEngWord, skyEngMeaning) = searchWord(it)
                    if (skyEngWord != null && skyEngMeaning != null) {
                        saveWord(skyEngWord, skyEngMeaning)
                        println("success: " + it)
                    }
                } catch (e: Exception) {
                    println("fail: $it $e")
                }
            }
        }
        callback()
    }

    /**
     * This function used only on development step to fill default db with N most used english words
     */
    @Suppress("unused")
    private fun words(context: Context): List<String> {
        val stream = context.resources.openRawResource(R.raw.words)
        val rdr = BufferedReader(InputStreamReader(stream))
        var line = rdr.readLine()
        val words = mutableListOf<String>()
        while (line != null) {
            line = rdr.readLine()
            if ((line?.length ?: 0) > 0) {
                words.add(line)
            }
        }
        rdr.close()
        stream.close()
        return words
    }

    private fun handleUserMeaningsError(response: Response<List<SkyEngUserMeaning>>?): Throwable {
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