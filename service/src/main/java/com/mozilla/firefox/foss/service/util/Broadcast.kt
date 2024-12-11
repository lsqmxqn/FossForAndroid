package com.mozilla.firefox.foss.service.util

import android.content.Context
import android.content.Intent
import com.mozilla.firefox.foss.common.constants.Intents
import com.mozilla.firefox.foss.common.constants.Permissions
import java.util.*

fun Context.sendBroadcastSelf(intent: Intent) {
    sendBroadcast(
        intent.setPackage(this.packageName),
        Permissions.RECEIVE_SELF_BROADCASTS
    )
}

fun Context.sendProfileChanged(uuid: UUID) {
    val intent = Intent(Intents.ACTION_PROFILE_CHANGED)
        .putExtra(Intents.EXTRA_UUID, uuid.toString())

    sendBroadcastSelf(intent)
}

fun Context.sendProfileLoaded(uuid: UUID) {
    val intent = Intent(Intents.ACTION_PROFILE_LOADED)
        .putExtra(Intents.EXTRA_UUID, uuid.toString())

    sendBroadcastSelf(intent)
}

fun Context.sendOverrideChanged() {
    val intent = Intent(Intents.ACTION_OVERRIDE_CHANGED)

    sendBroadcastSelf(intent)
}

fun Context.sendServiceRecreated() {
    sendBroadcastSelf(Intent(Intents.ACTION_SERVICE_RECREATED))
}

fun Context.sendFossStarted() {
    sendBroadcastSelf(Intent(Intents.ACTION_FOSS_STARTED))
}

fun Context.sendFossStopped(reason: String?) {
    sendBroadcastSelf(
        Intent(Intents.ACTION_FOSS_STOPPED).putExtra(
            Intents.EXTRA_STOP_REASON,
            reason
        )
    )
}
