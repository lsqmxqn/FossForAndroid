package com.mozilla.firefox.foss.util

import android.os.DeadObjectException
import com.mozilla.firefox.foss.common.log.Log
import com.mozilla.firefox.foss.remote.Remote
import com.mozilla.firefox.foss.service.remote.IFossManager
import com.mozilla.firefox.foss.service.remote.IProfileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

suspend fun <T> withFoss(
    context: CoroutineContext = Dispatchers.IO,
    block: suspend IFossManager.() -> T
): T {
    while (true) {
        val remote = Remote.service.remote.get()
        val client = remote.foss()

        try {
            return withContext(context) { client.block() }
        } catch (e: DeadObjectException) {
            Log.w("Remote services panic")

            Remote.service.remote.reset(remote)
        }
    }
}

suspend fun <T> withProfile(
    context: CoroutineContext = Dispatchers.IO,
    block: suspend IProfileManager.() -> T
): T {
    while (true) {
        val remote = Remote.service.remote.get()
        val client = remote.profile()

        try {
            return withContext(context) { client.block() }
        } catch (e: DeadObjectException) {
            Log.w("Remote services panic")

            Remote.service.remote.reset(remote)
        }
    }
}
