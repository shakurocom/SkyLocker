package com.shakuro.skylocker.model

import android.content.Context
import android.util.Log
import android.webkit.ValueCallback
import com.shakuro.skylocker.model.entities.*
import com.shakuro.skylocker.model.entities.DaoMaster.DevOpenHelper
import com.shakuro.skylocker.model.skyeng.SkyEngApi
import com.shakuro.skylocker.model.skyeng.SkyEngMeaning
import com.shakuro.skylocker.model.skyeng.SkyEngWord
import kotlinx.coroutines.experimental.*
import java.io.File
import java.util.*

private const val MIN_ALTERNATIVES_COUNT = 3
private const val VIEW_ALTERNATIVES_COUNT = 6

class SkyLockerManager private constructor(context: Context) {
    private val daoSession: DaoSession

    init {
        val filesDir = context.getExternalFilesDir(null)
        val dbFile = File(filesDir, "skylocker-db")
        val db = DevOpenHelper(context, dbFile.absolutePath).writableDb
        daoSession = DaoMaster(db).newSession()
    }

    companion object {
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

    fun randomMeaning(): Meaning? {
        if (daoSession.meaningDao.count() > 0) {
            val query = daoSession.meaningDao.queryBuilder().orderRaw("RANDOM()").limit(1).build()
            return query.list()?.first()
        } else {
            return null
        }
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
                        println("done: " + it)
                    }
                } catch (e: Exception) {
                    Log.e("error", "Failed: $it $e")
                }
            }
        }
        callback()
    }

    private data class SearchWordResult(val skyEngWord: SkyEngWord?, val skyEngMeaning: SkyEngMeaning?)

    private fun searchWord(search: String): SearchWordResult {
        val words = SkyEngApi.dictionaryApi.words(search).execute().body()
        val word = words?.filter { search.equals(it.text, true) }?.first()

        val meanings = word?.meanings
        meanings?.sortBy { it.id }
        var meaning: SkyEngMeaning? = null
        meanings?.first()?.let {
            meaning = SkyEngApi.dictionaryApi.meanings(it.id.toString()).execute().body()?.first()
        }
        return SearchWordResult(word, meaning)
    }

    private fun saveWord(skyEngWord: SkyEngWord, skyEngMeaning: SkyEngMeaning) {
        val alternatives = skyEngMeaning.alternativeTranslations
        if (alternatives != null && alternatives.size >= MIN_ALTERNATIVES_COUNT) {
            daoSession.runInTx {
                alternatives.forEach {
                    if (it.text != null && it.translation?.text != null) {
                        val alternative = Alternative(null, it.text, it.translation.text, skyEngMeaning.id)
                        daoSession.insert(alternative)
                    }
                }
                val meaning = Meaning()
                with(meaning) {
                    id = skyEngMeaning.id
                    wordId = skyEngWord.id
                    text = skyEngMeaning.text
                    translation = skyEngMeaning.translation?.text
                    definition = skyEngMeaning.definition?.text
                }
                daoSession.insert(meaning)
            }
        }
    }
}