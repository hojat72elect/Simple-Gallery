package com.simplemobiletools.gallery.pro.extensions

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Handler
import android.os.Looper
import com.simplemobiletools.commons.extensions.otgPath
import com.simplemobiletools.commons.extensions.sdCardPath

fun Context.isPathOnOTG(path: String) = otgPath.isNotEmpty() && path.startsWith(otgPath)

fun Context.isPathOnSD(path: String) = sdCardPath.isNotEmpty() && path.startsWith(sdCardPath)

fun Context.rescanAndDeletePath(path: String, callback: () -> Unit) {
    val SCAN_FILE_MAX_DURATION = 1000L
    val scanFileHandler = Handler(Looper.getMainLooper())
    scanFileHandler.postDelayed({
        callback()
    }, SCAN_FILE_MAX_DURATION)

    MediaScannerConnection.scanFile(applicationContext, arrayOf(path), null) { path, uri ->
        scanFileHandler.removeCallbacksAndMessages(null)
        try {
            applicationContext.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
        }
        callback()
    }
}