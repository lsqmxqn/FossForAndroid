package com.mozilla.firefox.foss.remote

import android.content.Context
import android.net.Uri
import com.mozilla.firefox.foss.common.constants.Authorities
import com.mozilla.firefox.foss.common.log.Log
import com.mozilla.firefox.foss.service.StatusProvider

class StatusClient(private val context: Context) {
    private val uri: Uri
        get() {
            return Uri.Builder()
                .scheme("content")
                .authority(Authorities.STATUS_PROVIDER)
                .build()
        }

    fun currentProfile(): String? {
        return try {
            val result = context.contentResolver.call(
                uri,
                StatusProvider.METHOD_CURRENT_PROFILE,
                null,
                null
            )

            result?.getString("name")
        } catch (e: Exception) {
            Log.w("Query current profile: $e", e)

            null
        }
    }
}