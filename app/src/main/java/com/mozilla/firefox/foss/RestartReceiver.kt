package com.mozilla.firefox.foss

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mozilla.firefox.foss.service.StatusProvider
import com.mozilla.firefox.foss.util.startFossService

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                if (StatusProvider.shouldStartFossOnBoot)
                    context.startFossService()
            }
        }
    }
}