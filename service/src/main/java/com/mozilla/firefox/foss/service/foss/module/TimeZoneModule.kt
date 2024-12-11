package com.mozilla.firefox.foss.service.foss.module

import android.app.Service
import android.content.Intent
import com.mozilla.firefox.foss.core.Foss
import java.util.*

class TimeZoneModule(service: Service) : Module<Unit>(service) {
    override suspend fun run() {
        val timeZones = receiveBroadcast {
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }

        while (true) {
            val timeZone = TimeZone.getDefault()

            Foss.notifyTimeZoneChanged(timeZone.id, timeZone.rawOffset)

            timeZones.receive()
        }
    }
}