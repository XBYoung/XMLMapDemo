package com.young.crccmap

import android.app.Application
import com.young.crccmap.other.SpHelper
import java.lang.ref.WeakReference

class MapApp:Application() {
    override fun onCreate() {
        super.onCreate()
        SpHelper.instance().init(WeakReference(this))
    }
}