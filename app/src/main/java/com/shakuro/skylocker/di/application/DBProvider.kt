package com.shakuro.skylocker.di.application

import android.content.Context
import com.shakuro.skylocker.R
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import com.shakuro.skylocker.model.skyeng.models.db.DaoMaster
import com.shakuro.skylocker.model.skyeng.models.db.DaoSession
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Provider

class DBProvider @Inject constructor(private val context: Context) : Provider<DaoSession> {

    override fun get(): DaoSession {
        val dbFile = File(context.filesDir, SkyEngRepository.DB_FILE_NAME)
        if (!dbFile.exists()) {
            val inputStream = context.resources.openRawResource(R.raw.skylockerdb)
            val outputStream = FileOutputStream(dbFile)
            IOUtils.copy(inputStream, outputStream)
            IOUtils.closeQuietly(inputStream)
            IOUtils.closeQuietly(outputStream)
        }
        val db = DaoMaster.DevOpenHelper(context, dbFile.absolutePath).writableDb
        return DaoMaster(db).newSession()
    }
}