package com.simplemobiletools.gallery.pro

import android.app.Application
import com.github.ajalt.reprint.core.Reprint
import com.simplemobiletools.gallery.pro.extensions.baseConfig
import com.simplemobiletools.gallery.pro.helpers.isNougatPlus
import com.squareup.picasso.Downloader
import com.squareup.picasso.Picasso
import java.util.Locale
import okhttp3.Request
import okhttp3.Response

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
        Reprint.initialize(this)
        Picasso.setSingletonInstance(Picasso.Builder(this).downloader(object : Downloader {
            override fun load(request: Request) = Response.Builder().build()

            override fun shutdown() {}
        }).build())
    }

    private fun checkUseEnglish() {

        if (baseConfig.useEnglish && !isNougatPlus()) {
            val conf = resources.configuration
            conf.locale = Locale.ENGLISH
            resources.updateConfiguration(conf, resources.displayMetrics)
        }
    }
}
