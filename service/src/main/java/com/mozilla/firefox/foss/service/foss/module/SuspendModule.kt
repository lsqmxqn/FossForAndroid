package com.mozilla.firefox.foss.service.foss.module

import android.app.Service
import android.content.Intent
import android.os.PowerManager
import androidx.core.content.getSystemService
import com.mozilla.firefox.foss.common.log.Log
import com.mozilla.firefox.foss.core.Foss
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext

class SuspendModule(service: Service) : Module<Unit>(service) {
    override suspend fun run() {
        val interactive = service.getSystemService<PowerManager>()?.isInteractive ?: true

        Foss.suspendCore(!interactive)

        val screenToggle = receiveBroadcast(false, Channel.CONFLATED) {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        try {
            while (true) {
                when (screenToggle.receive().action) {
                    Intent.ACTION_SCREEN_ON -> {
                        Foss.suspendCore(false)

                        Log.d("Foss resumed")
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        Foss.suspendCore(true)

                        Log.d("Foss suspended")
                    }
                    else -> {
                        // unreachable

                        Foss.healthCheckAll()
                    }
                }
            }
        } finally {
            withContext(NonCancellable) {
                Foss.suspendCore(false)
            }
        }
    }
}