package com.shakuro.skylocker.model

import android.content.Context
import com.shakuro.skylocker.model.entities.*
import com.shakuro.skylocker.model.entities.DaoMaster.DevOpenHelper
import com.shakuro.skylocker.model.skyeng.SkyEngApi
import com.shakuro.skylocker.model.skyeng.SkyEngMeaning
import com.shakuro.skylocker.model.skyeng.SkyEngWord
import kotlinx.coroutines.experimental.*
import java.io.File

private const val REQUIRED_ALTERNATIVES_COUNT = 3

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
                initializedInstance = SkyLockerManager(context)
            }
        }
    }

    /**
     * This function used only on development step to fill default db with N most used english words
     */
    fun fillWithWords(words: List<String>) = async(CommonPool) {
        words.forEach {
            val exists = daoSession.meaningDao
                    .queryBuilder()
                    .where(MeaningDao.Properties.Text.like(it))
                    .count() > 0

            if (!exists) {
                val (skyEngWord, skyEngMeaning) = searchWord(it)
                if (skyEngWord != null && skyEngMeaning != null) {
                    saveWord(skyEngWord, skyEngMeaning)
                }
            }
        }
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
        if (skyEngMeaning.alternativeTranslations.size >= REQUIRED_ALTERNATIVES_COUNT) {
            daoSession.runInTx {
                skyEngMeaning.alternativeTranslations.forEach {
                    val alternative = Alternative(null, it.text, it.translation.text, skyEngMeaning.id)
                    daoSession.insert(alternative)
                }
                val meaning = Meaning()
                with(meaning) {
                    id = skyEngMeaning.id
                    wordId = skyEngWord.id
                    text = skyEngMeaning.text
                    translation = skyEngMeaning.translation?.text
                }
                daoSession.insert(meaning)
            }
        }
    }
}