package com.mozilla.firefox.foss.service.foss.module

import android.app.Service
import com.mozilla.firefox.foss.common.constants.Intents
import com.mozilla.firefox.foss.common.log.Log

class CloseModule(service: Service) : Module<CloseModule.RequestClose>(service) {
    object RequestClose

    override suspend fun run() {
        val broadcasts = receiveBroadcast {
            addAction(Intents.ACTION_FOSS_REQUEST_STOP)
        }

        broadcasts.receive()

        Log.d("User request close")

        return enqueueEvent(RequestClose)
    }
}