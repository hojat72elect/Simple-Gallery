package com.simplemobiletools.gallery.pro.new_architecture.shared.extensions

import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.TYPE_GIFS
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.TYPE_IMAGES
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.TYPE_PORTRAITS
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.TYPE_RAWS
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.TYPE_SVGS
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.TYPE_VIDEOS
import com.simplemobiletools.gallery.pro.models.Medium

fun ArrayList<Medium>.getDirMediaTypes(): Int {
    var types = 0
    if (any { it.isImage() }) {
        types += TYPE_IMAGES
    }

    if (any { it.isVideo() }) {
        types += TYPE_VIDEOS
    }

    if (any { it.isGIF() }) {
        types += TYPE_GIFS
    }

    if (any { it.isRaw() }) {
        types += TYPE_RAWS
    }

    if (any { it.isSVG() }) {
        types += TYPE_SVGS
    }

    if (any { it.isPortrait() }) {
        types += TYPE_PORTRAITS
    }

    return types
}
