package com.mozilla.firefox.foss

import com.mozilla.firefox.foss.common.util.intent
import com.mozilla.firefox.foss.core.Foss
import com.mozilla.firefox.foss.core.model.Proxy
import com.mozilla.firefox.foss.design.ProxyDesign
import com.mozilla.firefox.foss.design.model.ProxyState
import com.mozilla.firefox.foss.store.TipsStore
import com.mozilla.firefox.foss.util.withFoss
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.TimeUnit

class ProxyActivity : BaseActivity<ProxyDesign>() {
    override suspend fun main() {
        val mode = withFoss { queryOverride(Foss.OverrideSlot.Session).mode }
        val names = withFoss { queryProxyGroupNames(uiStore.proxyExcludeNotSelectable) }
        val states = List(names.size) { ProxyState("?") }
        val unorderedStates = names.indices.map { names[it] to states[it] }.toMap()
        val reloadLock = Semaphore(10)
        val tips = TipsStore(this)

        val design = ProxyDesign(
            this,
            mode,
            names,
            uiStore
        )

        setContentDesign(design)

        launch(Dispatchers.IO) {
            val pkg = packageManager.getPackageInfo(packageName, 0)
            val validate = System.currentTimeMillis() - pkg.firstInstallTime > TimeUnit.DAYS.toMillis(5)

            if (tips.requestDonate && validate) {
                tips.requestDonate = false

                design.requestDonate()
            }
        }

        design.requests.send(ProxyDesign.Request.ReloadAll)

        while (isActive) {
            select<Unit> {
                events.onReceive {
                    when (it) {
                        Event.ProfileLoaded -> {
                            val newNames = withFoss {
                                queryProxyGroupNames(uiStore.proxyExcludeNotSelectable)
                            }

                            if (newNames != names) {
                                startActivity(ProxyActivity::class.intent)

                                finish()
                            }
                        }
                        else -> Unit
                    }
                }
                design.requests.onReceive {
                    when (it) {
                        ProxyDesign.Request.ReLaunch -> {
                            startActivity(ProxyActivity::class.intent)

                            finish()
                        }
                        ProxyDesign.Request.ReloadAll -> {
                            names.indices.forEach { idx ->
                                design.requests.trySend(ProxyDesign.Request.Reload(idx))
                            }
                        }
                        is ProxyDesign.Request.Reload -> {
                            launch {
                                val group = reloadLock.withPermit {
                                    withFoss {
                                        queryProxyGroup(names[it.index], uiStore.proxySort)
                                    }
                                }
                                val state = states[it.index]

                                state.now = group.now

                                design.updateGroup(
                                    it.index,
                                    group.proxies,
                                    group.type == Proxy.Type.Selector,
                                    state,
                                    unorderedStates
                                )
                            }
                        }
                        is ProxyDesign.Request.Select -> {
                            withFoss {
                                patchSelector(names[it.index], it.name)

                                states[it.index].now = it.name
                            }

                            design.requestRedrawVisible()
                        }
                        is ProxyDesign.Request.UrlTest -> {
                            launch {
                                withFoss {
                                    healthCheck(names[it.index])
                                }

                                design.requests.send(ProxyDesign.Request.Reload(it.index))
                            }
                        }
                        is ProxyDesign.Request.PatchMode -> {
                            design.showModeSwitchTips()

                            withFoss {
                                val o = queryOverride(Foss.OverrideSlot.Session)

                                o.mode = it.mode

                                patchOverride(Foss.OverrideSlot.Session, o)
                            }
                        }
                    }
                }
            }
        }
    }
}