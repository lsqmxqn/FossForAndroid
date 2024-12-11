package com.mozilla.firefox.foss.service.remote

import com.mozilla.firefox.foss.core.Foss
import com.mozilla.firefox.foss.core.model.*
import com.github.kr328.kaidl.BinderInterface

@BinderInterface
interface IFossManager {
    fun queryTunnelState(): TunnelState
    fun queryTrafficTotal(): Long
    fun queryProxyGroupNames(excludeNotSelectable: Boolean): List<String>
    fun queryProxyGroup(name: String, proxySort: ProxySort): ProxyGroup
    fun queryConfiguration(): UiConfiguration
    fun queryProviders(): ProviderList

    fun patchSelector(group: String, name: String): Boolean

    suspend fun healthCheck(group: String)
    suspend fun updateProvider(type: Provider.Type, name: String)

    fun queryOverride(slot: Foss.OverrideSlot): ConfigurationOverride
    fun patchOverride(slot: Foss.OverrideSlot, configuration: ConfigurationOverride)
    fun clearOverride(slot: Foss.OverrideSlot)

    fun setLogObserver(observer: ILogObserver?)
}