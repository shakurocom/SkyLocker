package com.shakuro.skylocker.model

import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.Element
import android.support.v8.renderscript.RenderScript
import android.support.v8.renderscript.ScriptIntrinsicBlur
import com.shakuro.skylocker.R
import com.shakuro.skylocker.model.entities.*
import com.shakuro.skylocker.model.entities.DaoMaster.DevOpenHelper
import com.shakuro.skylocker.model.skyeng.*
import kotlinx.coroutines.experimental.*
import org.apache.commons.io.IOUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.TimeUnit

private const val MIN_ALTERNATIVES_COUNT = 3
private const val VIEW_ALTERNATIVES_COUNT = 3

class SkyLockerManager private constructor(context: Context) {
    private val preferences: SharedPreferences
    private val daoSession: DaoSession

    var lockingEnabled: Boolean
        get() = preferences.getBoolean(LOCKING_ENABLED, true)
        set(value) = preferences.edit().putBoolean(LOCKING_ENABLED, value).apply()

    var useTop1000Words: Boolean
        get() = preferences.getBoolean(USE_TOP_1000_WORDS_KEY, true)
        set(value) = preferences.edit().putBoolean(USE_TOP_1000_WORDS_KEY, value).apply()

    var useUserWords: Boolean
        get() = preferences.getBoolean(USE_USER_WORDS_KEY, true)
        set(value) = preferences.edit().putBoolean(USE_USER_WORDS_KEY, value).apply()

    private var lastLockTime: Long
        get() = preferences.getLong(LAST_LOCK_TIME_KEY, 0)
        set(value) = preferences.edit().putLong(LAST_LOCK_TIME_KEY, value).apply()

    var locksCount: Long
        get() = preferences.getLong(LOCKS_COUNT_KEY, 0)
        set(value) = preferences.edit().putLong(LOCKS_COUNT_KEY, value).apply()

    val shouldRefreshUserMeanings: Boolean
        get() = activeUser() != null && locksCount > 0 && locksCount % LOCKS_COUNT_TO_MEANINGS_REFRESH == 0L

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
        private const val LOCKING_ENABLED = "LOCKING_ENABLED"
        private const val USE_TOP_1000_WORDS_KEY = "USE_TOP_1000_WORDS_KEY"
        private const val USE_USER_WORDS_KEY = "USE_USER_WORDS_KEY"
        private const val LOCKS_COUNT_KEY = "LOCKS_COUNT_KEY"
        private const val LAST_LOCK_TIME_KEY = "LAST_LOCK_TIME_KEY"
        private const val ENOUGH_SECONDS_TO_LOCK_AGAIN = 10L
        private const val LOCKS_COUNT_TO_MEANINGS_REFRESH = 5

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

    fun enoughTimePassedToLockAgain(): Boolean {
        synchronized(this) {
            val currentTime = System.currentTimeMillis()
            val passed = TimeUnit.MILLISECONDS.toSeconds(Math.abs(lastLockTime - currentTime))
            val enoughTimePassed = passed > ENOUGH_SECONDS_TO_LOCK_AGAIN
            if (enoughTimePassed) {
                lastLockTime = currentTime
            }
            return enoughTimePassed
        }
    }

    fun refreshUserMeanings(email: String, token: String, callback: (User?, Throwable?) -> Unit) {
        SkyEngApi.userApi.userMeanings(email, token).enqueue(object: Callback<List<SkyEngUserMeaning>> {

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
                        val error = SkyEngApi.handleUserMeaningsError(response)
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
        locksCount = 0
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

    fun genBlurredBgImageIfNotExistsAsync(context: Context) = async(CommonPool) {
        val imageFile = blurredBgImageFile(context)
        if (!imageFile.exists()) {
            try {
                val image = genBlurredBgImage(context = context)
                saveImage(image, imageFile)
            } catch (e: Throwable) {
                println("Error: ${e.localizedMessage}")
            }
        }
    }

    fun getBlurredBgImage(context: Context): Bitmap? {
        var result: Bitmap? = null
        val imageFile = blurredBgImageFile(context)
        if (imageFile.exists()) {
            try {
                result = BitmapFactory.decodeFile(imageFile.absolutePath)
            } catch (e: Throwable) {
                println("Error: ${e.localizedMessage}")
            }
        }
        if (result == null) {
            try {
                result = genBlurredBgImage(context = context)
                saveImage(result, imageFile)
            } catch (e: Throwable) {
                println("Error: ${e.localizedMessage}")
            }
        }
        return result
    }

    private fun genBlurredBgImage(context: Context): Bitmap {
        // get desktop image
        val wallpaperManager = WallpaperManager.getInstance(context.applicationContext)
        val drawable = wallpaperManager.drawable
        val inWidth = drawable.intrinsicWidth
        val inHeight = drawable.intrinsicHeight

        // set max size of blurred image to 320 pixels
        val scale = 320.0f / Math.max(inWidth, inHeight)
        val outWidth = (scale * inWidth).toInt()
        val outHeight = (scale * inHeight).toInt()

        val bitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        val blurredBitmap = Bitmap.createBitmap(bitmap)

        val rs = RenderScript.create(context.applicationContext)
        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val inputAllocation = Allocation.createFromBitmap(rs, bitmap)
        val outputAllocation = Allocation.createFromBitmap(rs, blurredBitmap)
        blurScript.setRadius(12.0f)
        blurScript.setInput(inputAllocation)
        blurScript.forEach(outputAllocation)
        outputAllocation.copyTo(blurredBitmap)
        inputAllocation.destroy()
        outputAllocation.destroy()

        return blurredBitmap
    }

    private fun saveImage(image: Bitmap, file: File) {
        val out: FileOutputStream = FileOutputStream(file)
        try {
            image.compress(Bitmap.CompressFormat.PNG, 100, out)
        } catch (e: Throwable) {
            println("Error: ${e.localizedMessage}")
        } finally {
            IOUtils.closeQuietly(out)
        }
    }

    private fun blurredBgImageFile(context: Context): File = File(context.filesDir, "blurred_bg.png")

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
}