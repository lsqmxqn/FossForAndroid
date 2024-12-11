package com.mozilla.firefox.foss

import android.app.Application
import android.content.Context
import com.mozilla.firefox.foss.common.Global
import com.mozilla.firefox.foss.common.compat.currentProcessName
import com.mozilla.firefox.foss.common.log.Log
import com.mozilla.firefox.foss.remote.Remote
import com.mozilla.firefox.foss.service.util.sendServiceRecreated

@Suppress("unused")
class MainApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        Global.init(this)
    }

    override fun onCreate() {
        super.onCreate()

        val processName = currentProcessName

        Log.d("Process $processName started")

        if (processName == packageName) {
            Remote.launch()
        } else {
            sendServiceRecreated()
        }
    }

    fun finalize() {
        Global.destroy()
    }
}