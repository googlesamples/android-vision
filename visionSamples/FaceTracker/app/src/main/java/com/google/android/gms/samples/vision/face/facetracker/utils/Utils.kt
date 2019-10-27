package com.turn2cloud.paddemo.utils

import android.content.Context
import android.util.Base64
import android.media.ExifInterface
import android.net.Uri
import androidx.core.net.toUri
import android.graphics.*
import android.graphics.Bitmap
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import me.echodev.resizer.Resizer
import java.io.*

object Utils {

    fun convertBmpToBase64(bmpFile: File): String {
        val bmp = rotateImageIfRequired(BitmapFactory.decodeFile(bmpFile!!.absolutePath), bmpFile.toUri())
        val byteArrayOutputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)
    }

    @Throws(IOException::class)
    private fun rotateImageIfRequired(img: Bitmap, selectedImage: Uri): Bitmap {
        val ei = ExifInterface(selectedImage.getPath())
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val degree = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        return degree.run {
            if (this == 0f) {
                img
            } else {
                val matrix = Matrix()
                matrix.postRotate(degree)

                val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
                img.recycle()
                rotatedImg
            }
        }
    }

    fun saveResizedBitmapFile(
        ctx: Context,
        srcImgFile: File,
        targetFile: File,
        maxSize: Int
    ): File {
        // 轉向備份
        val exifSource = ExifInterface(srcImgFile.getAbsolutePath())
        // 壓縮寫入
        val resizedFile = Resizer(ctx)
            .setQuality(100)
            .setTargetLength(maxSize)
            .setOutputFormat(targetFile.extension)
            .setOutputFilename(targetFile.nameWithoutExtension)
            .setOutputDirPath(targetFile.parent)
            .setSourceImage(srcImgFile)
            .resizedFile
        // 還原轉向
        val newExif = ExifInterface(resizedFile.absolutePath)
        newExif.setAttribute(
            ExifInterface.TAG_ORIENTATION,
            exifSource.getAttribute(ExifInterface.TAG_ORIENTATION)
        )
        newExif.saveAttributes()

//        val inChannel = FileInputStream(resizedFile).getChannel()
//        val outChannel =
//            FileOutputStream(ctx.getExternalFilesDir(null)!!.absolutePath + File.separator + resizedFile.name).getChannel()
//        inChannel.transferTo(0, inChannel.size(), outChannel)
//        inChannel.close()
//        outChannel.close()

        return resizedFile
    }

//    fun saveBitmapFile(imgFile: File, targetFile: File, maxSize: Int): Unit {
//        val options = BitmapFactory.Options()
//        options.inScaled = false
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888
//        val bmp = BitmapFactory.decodeFile(imgFile.absolutePath, options)
//        var newBmp = rotateImageIfRequired(bmp, targetFile.toUri())
//
//        newBmp.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(targetFile))
//        newBmp.recycle()
//        bmp.recycle()
//    }

//    @Throws(IllegalArgumentException::class)
//    fun convertBase64ToBmp(base64Str: String): Bitmap {
//        val decodedBytes = Base64.decode(
//            base64Str.substring(base64Str.indexOf(",") + 1),
//            Base64.DEFAULT
//        )
//
//        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
//    }

//    fun getResizedBitmap(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
//        val resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
//
//        val scaleX = newWidth / bitmap.width.toFloat()
//        val scaleY = newHeight / bitmap.height.toFloat()
//        val pivotX = 0f
//        val pivotY = 0f
//
//        val scaleMatrix = Matrix()
//        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY)
//
//        val canvas = Canvas(resizedBitmap)
//        canvas.setMatrix(scaleMatrix)
//        canvas.drawBitmap(bitmap, 0f, 0f, Paint(FILTER_BITMAP_FLAG))
//        bitmap.recycle()
//
//        return resizedBitmap
//    }

    //    @Throws(IOException::class)
//    private fun getRotateImageExif(selectedImage: Uri): Float {
//        val ei = ExifInterface(selectedImage.getPath())
//        val orientation =
//            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
//
//        val degree = when (orientation) {
//            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
//            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
//            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
//            else -> 0f
//        }
//
//        return degree
//    }

    fun isConnectedToNetwork(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        var isConnected = false
        if (connectivityManager != null) {
            val activeNetwork = connectivityManager.activeNetworkInfo
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
        }

        return isConnected
    }
}