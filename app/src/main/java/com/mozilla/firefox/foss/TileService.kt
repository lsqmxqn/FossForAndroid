package com.mozilla.firefox.foss

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.mozilla.firefox.foss.common.constants.Intents
import com.mozilla.firefox.foss.common.constants.Permissions
import com.mozilla.firefox.foss.remote.StatusClient
import com.mozilla.firefox.foss.util.startFossService
import com.mozilla.firefox.foss.util.stopFossService

@RequiresApi(Build.VERSION_CODES.N)
class TileService : TileService() {
    private var currentProfile = ""
    private var fossRunning = false

    override fun onClick() {
        val tile = qsTile ?: return

        when (tile.state) {
            Tile.STATE_INACTIVE -> {
                startFossService()
            }
            Tile.STATE_ACTIVE -> {
                stopFossService()
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()

        registerReceiver(
            receiver,
            IntentFilter().apply {
                addAction(Intents.ACTION_FOSS_STARTED)
                addAction(Intents.ACTION_FOSS_STOPPED)
                addAction(Intents.ACTION_PROFILE_LOADED)
                addAction(Intents.ACTION_SERVICE_RECREATED)
            },
            Permissions.RECEIVE_SELF_BROADCASTS,
            null
        )

        val name = StatusClient(this).currentProfile()

        fossRunning = name != null
        currentProfile = name ?: ""

        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()

        unregisterReceiver(receiver)
    }

    private fun updateTile() {
        val tile = qsTile ?: return

        tile.state = if (fossRunning)
            Tile.STATE_ACTIVE
        else
            Tile.STATE_INACTIVE

        tile.label = if (currentProfile.isEmpty())
            getText(R.string.launch_name)
        else
            currentProfile

        tile.icon = Icon.createWithResource(this, R.drawable.ic_logo_service)

        tile.updateTile()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intents.ACTION_FOSS_STARTED -> {
                    fossRunning = true

                    currentProfile = ""
                }
                Intents.ACTION_FOSS_STOPPED, Intents.ACTION_SERVICE_RECREATED -> {
                    fossRunning = false

                    currentProfile = ""
                }
                Intents.ACTION_PROFILE_LOADED -> {
                    currentProfile = StatusClient(this@TileService).currentProfile() ?: ""
                }
            }

            updateTile()
        }
    }
}