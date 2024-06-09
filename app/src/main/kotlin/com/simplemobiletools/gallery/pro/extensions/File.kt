package com.simplemobiletools.gallery.pro.extensions

import java.io.File

fun File.isSvg() = absolutePath.isSvg()
fun File.isGif() = absolutePath.endsWith(".gif", true)