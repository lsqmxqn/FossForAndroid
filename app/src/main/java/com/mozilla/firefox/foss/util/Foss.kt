package com.mozilla.firefox.foss.util

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.mozilla.firefox.foss.common.compat.startForegroundServiceCompat
import com.mozilla.firefox.foss.common.constants.Intents
import com.mozilla.firefox.foss.common.util.intent
import com.mozilla.firefox.foss.design.store.UiStore
import com.mozilla.firefox.foss.service.FossService
import com.mozilla.firefox.foss.service.TunService
import com.mozilla.firefox.foss.service.util.sendBroadcastSelf

fun Context.startFossService(): Intent? {
    val startTun = UiStore(this).enableVpn

    if (startTun) {
        val vpnRequest = VpnService.prepare(this)
        if (vpnRequest != null)
            return vpnRequest

        startForegroundServiceCompat(TunService::class.intent)
    } else {
        startForegroundServiceCompat(FossService::class.intent)
    }

    return null
}

fun Context.stopFossService() {
    sendBroadcastSelf(Intent(Intents.ACTION_FOSS_REQUEST_STOP))
}