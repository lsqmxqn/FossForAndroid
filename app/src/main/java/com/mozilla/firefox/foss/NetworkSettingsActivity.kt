package com.mozilla.firefox.foss

import com.mozilla.firefox.foss.common.util.intent
import com.mozilla.firefox.foss.design.NetworkSettingsDesign
import com.mozilla.firefox.foss.service.store.ServiceStore
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select

class NetworkSettingsActivity : BaseActivity<NetworkSettingsDesign>() {
    override suspend fun main() {
        val design = NetworkSettingsDesign(
            this,
            uiStore,
            ServiceStore(this),
            fossRunning,
        )

        setContentDesign(design)

        while (isActive) {
            select<Unit> {
                events.onReceive {
                    when (it) {
                        Event.FossStart, Event.FossStop, Event.ServiceRecreated ->
                            recreate()
                        else -> Unit
                    }
                }
                design.requests.onReceive {
                    when (it) {
                        NetworkSettingsDesign.Request.StartAccessControlList ->
                            startActivity(AccessControlActivity::class.intent)
                    }
                }
            }
        }
    }

}
