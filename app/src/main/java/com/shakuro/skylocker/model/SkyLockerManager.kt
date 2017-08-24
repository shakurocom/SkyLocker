package com.shakuro.skylocker.model

import android.content.Context
import android.content.SharedPreferences
import com.shakuro.skylocker.R
import com.shakuro.skylocker.model.entities.*
import com.shakuro.skylocker.model.entities.DaoMaster.DevOpenHelper
import com.shakuro.skylocker.model.skyeng.*
import kotlinx.coroutines.experimental.*
import org.apache.commons.io.IOUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.*

private const val MIN_ALTERNATIVES_COUNT = 3
private const val VIEW_ALTERNATIVES_COUNT = 6

class SkyLockerManager private constructor(context: Context) {
    private val preferences: SharedPreferences
    private val daoSession: DaoSession

    var useTop1000Words: Boolean
        get() = preferences.getBoolean(USE_TOP_1000_WORDS_KEY, true)
        set(value) = preferences.edit().putBoolean(USE_TOP_1000_WORDS_KEY, value).apply()

    var useUserWords: Boolean
        get() = preferences.getBoolean(USE_USER_WORDS_KEY, true)
        set(value) = preferences.edit().putBoolean(USE_USER_WORDS_KEY, value).apply()

    init {
        preferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val dbFile = File(context.filesDir, "skylocker-db")
        if (!dbFile.exists()) {
            val inputStream = context.resources.openRawResource(R.raw.skylockerdb)
            val outputStream = FileOutputStream(dbFile)
            IOUtils.copy(inputStream, outputStream)
            IOUtils.closeQuietly(inputStream)
            IOUtils.closeQuietly(outputStream)
        }
        val db = DevOpenHelper(context, dbFile.absolutePath).writableDb
        daoSession = DaoMaster(db).newSession()
    }

    companion object {
        private const val IS_FIRST_RUN_KEY = "IS_FIRST_RUN_KEY"
        private const val USE_TOP_1000_WORDS_KEY = "USE_TOP_1000_WORDS_KEY"
        private const val USE_USER_WORDS_KEY = "USE_USER_WORDS_KEY"

        private var initializedInstance: SkyLockerManager? = null

        val instance: SkyLockerManager by lazy {
            initializedInstance ?: throw IllegalStateException("SkyLockerManager not initialized")
        }

        fun initInstance(context: Context) = synchronized(this) {
            if (initializedInstance == null) {
                initializedInstance = SkyLockerManager(context.applicationContext)
            }
        }
    }

    fun isFirstRun(): Boolean {
        val result = preferences.getBoolean(IS_FIRST_RUN_KEY, true)
        if (result) {
            preferences.edit().putBoolean(IS_FIRST_RUN_KEY, false).apply()
        }
        return result
    }

    fun refreshUserMeanings(email: String, token: String, callback: (User?, Throwable?) -> Unit) {
        SkyEngApi.userApi.userMeanings(email, token).enqueue(object: Callback<List<SkyEngUserMeaning>> {

            override fun onResponse(call: Call<List<SkyEngUserMeaning>>?, response: Response<List<SkyEngUserMeaning>>?) {
                if (response?.isSuccessful == true) {
                    val user = updateActiveUser(email, token)

                    val allUserMeanings = mutableListOf<Long>()
                    val userMeaningsToLoad = mutableListOf<Long>()

                    val meaningExistsQuery = daoSession.meaningDao.queryBuilder().where(MeaningDao.Properties.Id.eq(0)).buildCount()
                    response?.body()?.forEach {
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
                    val error = SkyEngApi.handleUserMeaningsError(response)
                    callback(null, error)
                }
            }

            override fun onFailure(call: Call<List<SkyEngUserMeaning>>?, t: Throwable?) {
                callback(null, t)
            }
        })
    }

    fun refreshUserMeaningsInBackground() = async(CommonPool) {
        val activeUser = activeUser()
        activeUser?.let {
            refreshUserMeanings(it.email, it.token) { user, error ->
                error?.let {
                    println("Error: ${error.localizedMessage}")
                }
            }
        }
    }

    fun randomMeaning(): Meaning? {
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

    fun answerWithAlternatives(meaning: Meaning): List<String> {
        val result = mutableListOf<String>()
        result.add(meaning.text)

        val alternatives = meaning.alternatives
        for (i in 0..Math.min(VIEW_ALTERNATIVES_COUNT - 1, alternatives.size - 1)) {
            result.add(alternatives[i].text)
        }
        Collections.shuffle(result)
        return result
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
        SkyEngApi.userApi.requestToken(email).enqueue(object: Callback<Unit> {

            override fun onResponse(call: Call<Unit>?, response: Response<Unit>?) {
                callback(null)
            }

            override fun onFailure(call: Call<Unit>?, error: Throwable?) {
                callback(error)
            }
        })
    }

    /**
     * This function used only on development step to fill default db with N most used english words
     */
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

    private fun loadUserMeanings(user: User?, toLoad: MutableList<Long>, callback: (Throwable?) -> Unit) {
        val ids = toLoad.joinToString()
        SkyEngApi.dictionaryApi.meanings(ids).enqueue(object : Callback<MutableList<SkyEngMeaning>> {

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
        val words = SkyEngApi.dictionaryApi.words(search).execute().body()
        val word = words?.filter { search.equals(it.text, true) }?.first()

        var meaning: SkyEngMeaning? = null
        word?.meanings?.first()?.let {
            meaning = SkyEngApi.dictionaryApi.meanings(it.id.toString()).execute().body()?.first()
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
                daoSession.insert(meaning)
                println("${meaning.id} ${meaning.text}")
            }

            if (inTransaction) {
                daoSession.runInTx(saveRunnable)
            } else {
                saveRunnable.run()
            }
        }
    }
}