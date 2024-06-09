package com.simplemobiletools.gallery.pro.extensions

import android.graphics.Color
import android.widget.RemoteViews

fun RemoteViews.applyColorFilter(id: Int, color: Int) {
    setInt(id, "setColorFilter", color)
    setInt(id, "setImageAlpha", Color.alpha(color))
}