package com.mozilla.firefox.foss

import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mozilla.firefox.foss.common.compat.getColorCompat
import com.mozilla.firefox.foss.common.compat.pendingIntentFlags
import com.mozilla.firefox.foss.common.log.Log
import com.mozilla.firefox.foss.common.util.intent
import com.mozilla.firefox.foss.core.model.LogMessage
import com.mozilla.firefox.foss.log.LogcatCache
import com.mozilla.firefox.foss.log.LogcatWriter
import com.mozilla.firefox.foss.service.RemoteService
import com.mozilla.firefox.foss.service.remote.ILogObserver
import com.mozilla.firefox.foss.service.remote.IRemoteService
import com.mozilla.firefox.foss.service.remote.unwrap
import com.mozilla.firefox.foss.util.logsDir
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.IOException
import java.util.*

class LogcatService : Service(), CoroutineScope by CoroutineScope(Dispatchers.Default), IInterface {
    private val cache = LogcatCache()

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            stopSelf()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            startObserver(service ?: return stopSelf())
        }
    }

    override fun onCreate() {
        super.onCreate()

        running = true

        createNotificationChannel()

        showNotification()

        bindService(RemoteService::class.intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        cancel()

        unbindService(connection)

        stopForeground(true)

        running = false

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        return this.asBinder()
    }

    override fun asBinder(): IBinder {
        return object : Binder() {
            override fun queryLocalInterface(descriptor: String): IInterface {
                return this@LogcatService
            }
        }
    }

    suspend fun snapshot(full: Boolean): LogcatCache.Snapshot? {
        return cache.snapshot(full)
    }

    private fun startObserver(binder: IBinder) {
        if (!binder.isBinderAlive)
            return stopSelf()

        launch(Dispatchers.IO) {
            val service = binder.unwrap(IRemoteService::class).foss()
            val channel = Channel<LogMessage>(CACHE_CAPACITY)

            try {
                logsDir.mkdirs()

                LogcatWriter(this@LogcatService).use {
                    val observer = object : ILogObserver {
                        override fun newItem(log: LogMessage) {
                            channel.trySend(log)
                        }
                    }

                    service.setLogObserver(observer)

                    while (isActive) {
                        val msg = channel.receive()

                        it.appendMessage(msg)

                        cache.append(msg)
                    }
                }
            } catch (e: IOException) {
                Log.e("Write log file: $e", e)
            } finally {
                withContext(NonCancellable) {
                    if (binder.isBinderAlive) {
                        service.setLogObserver(null)
                    }

                    stopSelf()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        NotificationManagerCompat.from(this)
            .createNotificationChannel(
                NotificationChannelCompat.Builder(
                    CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT
                ).setName(getString(R.string.foss_logcat)).build()
            )
    }

    private fun showNotification() {
        val notification = NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_service)
            .setColor(getColorCompat(R.color.color_foss_light))
            .setContentTitle(getString(R.string.foss_logcat))
            .setContentText(getString(R.string.running))
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    R.id.nf_logcat_status,
                    LogcatActivity::class.intent
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                    pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)
                )
            )
            .build()

        startForeground(R.id.nf_logcat_status, notification)
    }

    companion object {
        private const val CHANNEL_ID = "foss_logcat_channel"
        private const val CACHE_CAPACITY = 128

        var running: Boolean = false
    }
}