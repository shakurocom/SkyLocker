package com.shakuro.skylocker.model.quiz

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.Element
import android.support.v8.renderscript.RenderScript
import android.support.v8.renderscript.ScriptIntrinsicBlur
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class QuizBgImageLoader @Inject constructor(private val context: Context) {
    private val FILE_NAME = "blurred_bg.png"
    private val imageFile: File = File(context.filesDir, FILE_NAME)

    fun createBgImage(): Completable {
        return Completable.create { emitter ->
            if (!imageFile.exists()) {
                try {
                    renderBlurredBgImage(imageFile)
                    emitter.onComplete()
                } catch (e: Throwable) {
                    emitter.onError(e)
                }
            } else {
                emitter.onComplete()
            }
        }.subscribeOn(Schedulers.io())
    }

    fun loadBgImage(): Single<Bitmap> {
        return createBgImage().andThen(Single.create<Bitmap> { emitter ->
            try {
                val image = BitmapFactory.decodeFile(imageFile.absolutePath)
                emitter.onSuccess(image)
            } catch (e: Throwable) {
                emitter.onError(e)
            }
        }).subscribeOn(Schedulers.io())
    }

    private fun renderBlurredBgImage(imageFile: File): Bitmap {
        // get desktop image
        val wallpaperManager = WallpaperManager.getInstance(context)
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

        val rs = RenderScript.create(context)
        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val inputAllocation = Allocation.createFromBitmap(rs, bitmap)
        val outputAllocation = Allocation.createFromBitmap(rs, blurredBitmap)
        blurScript.setRadius(12.0f)
        blurScript.setInput(inputAllocation)
        blurScript.forEach(outputAllocation)
        outputAllocation.copyTo(blurredBitmap)
        inputAllocation.destroy()
        outputAllocation.destroy()

        saveImage(blurredBitmap, imageFile)

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
}