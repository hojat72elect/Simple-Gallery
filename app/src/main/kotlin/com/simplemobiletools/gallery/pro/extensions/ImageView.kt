package com.simplemobiletools.gallery.pro.extensions

import android.graphics.PorterDuff
import android.widget.ImageView

fun ImageView.applyColorFilter(color: Int) = setColorFilter(color, PorterDuff.Mode.SRC_IN)