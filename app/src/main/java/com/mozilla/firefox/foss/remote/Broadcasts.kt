package com.mozilla.firefox.foss.remote

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.mozilla.firefox.foss.common.constants.Intents
import com.mozilla.firefox.foss.common.log.Log

class Broadcasts(private val context: Application) {
    interface Observer {
        fun onServiceRecreated()
        fun onStarted()
        fun onStopped(cause: String?)
        fun onProfileChanged()
        fun onProfileLoaded()
    }

    var fossRunning: Boolean = false

    private var registered = false
    private val receivers = mutableListOf<Observer>()
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.`package` != context?.packageName)
                return

            when (intent?.action) {
                Intents.ACTION_SERVICE_RECREATED -> {
                    fossRunning = false

                    receivers.forEach {
                        it.onServiceRecreated()
                    }
                }
                Intents.ACTION_FOSS_STARTED -> {
                    fossRunning = true

                    receivers.forEach {
                        it.onStarted()
                    }
                }
                Intents.ACTION_FOSS_STOPPED -> {
                    fossRunning = false

                    receivers.forEach {
                        it.onStopped(intent.getStringExtra(Intents.EXTRA_STOP_REASON))
                    }
                }
                Intents.ACTION_PROFILE_CHANGED ->
                    receivers.forEach {
                        it.onProfileChanged()
                    }
                Intents.ACTION_PROFILE_LOADED -> {
                    receivers.forEach {
                        it.onProfileLoaded()
                    }
                }
            }
        }
    }

    fun addObserver(observer: Observer) {
        receivers.add(observer)
    }

    fun removeObserver(observer: Observer) {
        receivers.remove(observer)
    }

    fun register() {
        if (registered)
            return

        try {
            context.registerReceiver(broadcastReceiver, IntentFilter().apply {
                addAction(Intents.ACTION_SERVICE_RECREATED)
                addAction(Intents.ACTION_FOSS_STARTED)
                addAction(Intents.ACTION_FOSS_STOPPED)
                addAction(Intents.ACTION_PROFILE_CHANGED)
                addAction(Intents.ACTION_PROFILE_LOADED)
            })

            fossRunning = StatusClient(context).currentProfile() != null
        } catch (e: Exception) {
            Log.w("Register global receiver: $e", e)
        }
    }

    fun unregister() {
        if (!registered)
            return

        try {
            context.unregisterReceiver(broadcastReceiver)

            fossRunning = false
        } catch (e: Exception) {
            Log.w("Unregister global receiver: $e", e)
        }
    }
}