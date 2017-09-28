package com.shakuro.skylocker.ui

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.Element
import android.support.v8.renderscript.RenderScript
import android.support.v8.renderscript.ScriptIntrinsicBlur
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream

class BlurredImageLoader(private val appContext: Context, private val imageFile: File) {

    companion object {
        const val FILE_NAME = "blurred_bg.png"
    }

    fun genBlurredBgImageIfNotExistsAsync() = async(CommonPool) {
        if (!imageFile.exists()) {
            try {
                val image = genBlurredBgImage()
                saveImage(image, imageFile)
            } catch (e: Throwable) {
                println("Error: ${e.localizedMessage}")
            }
        }
    }

    fun getBlurredBgImage(): Bitmap? {
        var result: Bitmap? = null
        if (imageFile.exists()) {
            try {
                result = BitmapFactory.decodeFile(imageFile.absolutePath)
            } catch (e: Throwable) {
                println("Error: ${e.localizedMessage}")
            }
        }
        if (result == null) {
            try {
                result = genBlurredBgImage()
                saveImage(result, imageFile)
            } catch (e: Throwable) {
                println("Error: ${e.localizedMessage}")
            }
        }
        return result
    }

    private fun genBlurredBgImage(): Bitmap {
        // get desktop image
        val wallpaperManager = WallpaperManager.getInstance(appContext)
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

        val rs = RenderScript.create(appContext)
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
}