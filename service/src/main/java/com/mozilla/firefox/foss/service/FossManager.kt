package com.mozilla.firefox.foss.service

import android.content.Context
import com.mozilla.firefox.foss.common.log.Log
import com.mozilla.firefox.foss.core.Foss
import com.mozilla.firefox.foss.core.model.*
import com.mozilla.firefox.foss.service.data.Selection
import com.mozilla.firefox.foss.service.data.SelectionDao
import com.mozilla.firefox.foss.service.remote.IFossManager
import com.mozilla.firefox.foss.service.remote.ILogObserver
import com.mozilla.firefox.foss.service.store.ServiceStore
import com.mozilla.firefox.foss.service.util.sendOverrideChanged
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel

class FossManager(private val context: Context) : IFossManager,
    CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val store = ServiceStore(context)
    private var logReceiver: ReceiveChannel<LogMessage>? = null

    override fun queryTunnelState(): TunnelState {
        return Foss.queryTunnelState()
    }

    override fun queryTrafficTotal(): Long {
        return Foss.queryTrafficTotal()
    }

    override fun queryProxyGroupNames(excludeNotSelectable: Boolean): List<String> {
        return Foss.queryGroupNames(excludeNotSelectable)
    }

    override fun queryProxyGroup(name: String, proxySort: ProxySort): ProxyGroup {
        return Foss.queryGroup(name, proxySort)
    }

    override fun queryConfiguration(): UiConfiguration {
        return Foss.queryConfiguration()
    }

    override fun queryProviders(): ProviderList {
        return ProviderList(Foss.queryProviders())
    }

    override fun queryOverride(slot: Foss.OverrideSlot): ConfigurationOverride {
        return Foss.queryOverride(slot)
    }

    override fun patchSelector(group: String, name: String): Boolean {
        return Foss.patchSelector(group, name).also {
            val current = store.activeProfile ?: return@also

            if (it) {
                SelectionDao().setSelected(Selection(current, group, name))
            } else {
                SelectionDao().removeSelected(current, group)
            }
        }
    }

    override fun patchOverride(slot: Foss.OverrideSlot, configuration: ConfigurationOverride) {
        Foss.patchOverride(slot, configuration)

        context.sendOverrideChanged()
    }

    override fun clearOverride(slot: Foss.OverrideSlot) {
        Foss.clearOverride(slot)
    }

    override suspend fun healthCheck(group: String) {
        return Foss.healthCheck(group).await()
    }

    override suspend fun updateProvider(type: Provider.Type, name: String) {
        return Foss.updateProvider(type, name).await()
    }

    override fun setLogObserver(observer: ILogObserver?) {
        synchronized(this) {
            logReceiver?.apply {
                cancel()

                Foss.forceGc()
            }

            if (observer != null) {
                logReceiver = Foss.subscribeLogcat().also { c ->
                    launch {
                        try {
                            while (isActive) {
                                observer.newItem(c.receive())
                            }
                        } catch (e: CancellationException) {
                            // intended behavior
                            // ignore
                        } catch (e: Exception) {
                            Log.w("UI crashed", e)
                        } finally {
                            withContext(NonCancellable) {
                                c.cancel()

                                Foss.forceGc()
                            }
                        }
                    }
                }
            }
        }
    }
}