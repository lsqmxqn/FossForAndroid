package com.mozilla.firefox.foss.remote

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import com.mozilla.firefox.foss.common.log.Log
import com.mozilla.firefox.foss.common.util.intent
import com.mozilla.firefox.foss.service.RemoteService
import com.mozilla.firefox.foss.service.remote.IRemoteService
import com.mozilla.firefox.foss.service.remote.unwrap
import com.mozilla.firefox.foss.util.unbindServiceSilent
import java.util.concurrent.TimeUnit

class Service(private val context: Application, val crashed: () -> Unit) {
    val remote = Resource<IRemoteService>()

    private val connection = object : ServiceConnection {
        private var lastCrashed: Long = -1

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            remote.set(service.unwrap(IRemoteService::class))
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            remote.set(null)

            if (System.currentTimeMillis() - lastCrashed < TOGGLE_CRASHED_INTERVAL) {
                unbind()

                crashed()
            }

            lastCrashed = System.currentTimeMillis()

            Log.w("RemoteManager crashed")
        }
    }

    fun bind() {
        try {
            context.bindService(RemoteService::class.intent, connection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            unbind()

            crashed()
        }
    }

    fun unbind() {
        context.unbindServiceSilent(connection)

        remote.set(null)
    }

    companion object {
        private val TOGGLE_CRASHED_INTERVAL = TimeUnit.SECONDS.toMillis(10)
    }
}