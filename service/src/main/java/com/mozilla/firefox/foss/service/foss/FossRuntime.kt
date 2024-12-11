package com.mozilla.firefox.foss.service.foss

import com.mozilla.firefox.foss.common.log.Log
import com.mozilla.firefox.foss.core.Foss
import com.mozilla.firefox.foss.service.foss.module.Module
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val globalLock = Mutex()

interface FossRuntimeScope {
    fun <E, T : Module<E>> install(module: T): T
}

interface FossRuntime {
    fun launch()
    fun requestGc()
}

fun CoroutineScope.fossRuntime(block: suspend FossRuntimeScope.() -> Unit): FossRuntime {
    return object : FossRuntime {
        override fun launch() {
            launch(Dispatchers.IO) {
                globalLock.withLock {
                    Log.d("FossRuntime: initialize")

                    try {
                        val modules = mutableListOf<Module<*>>()

                        Foss.reset()
                        Foss.clearOverride(Foss.OverrideSlot.Session)

                        val scope = object : FossRuntimeScope {
                            override fun <E, T : Module<E>> install(module: T): T {
                                launch {
                                    modules.add(module)

                                    module.execute()
                                }

                                return module
                            }
                        }

                        scope.block()

                        cancel()
                    } finally {
                        withContext(NonCancellable) {
                            Foss.reset()
                            Foss.clearOverride(Foss.OverrideSlot.Session)

                            Log.d("FossRuntime: destroyed")
                        }
                    }
                }
            }
        }

        override fun requestGc() {
            Foss.forceGc()
        }
    }
}