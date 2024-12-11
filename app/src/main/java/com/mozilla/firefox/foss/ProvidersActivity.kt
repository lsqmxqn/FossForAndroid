package com.mozilla.firefox.foss

import com.mozilla.firefox.foss.common.util.intent
import com.mozilla.firefox.foss.common.util.ticker
import com.mozilla.firefox.foss.design.ProvidersDesign
import com.mozilla.firefox.foss.design.util.showExceptionToast
import com.mozilla.firefox.foss.util.withFoss
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.concurrent.TimeUnit

class ProvidersActivity : BaseActivity<ProvidersDesign>() {
    override suspend fun main() {
        val providers = withFoss { queryProviders().sorted() }
        val design = ProvidersDesign(this, providers)

        setContentDesign(design)

        val ticker = ticker(TimeUnit.MINUTES.toMillis(1))

        while (isActive) {
            select<Unit> {
                events.onReceive {
                    when (it) {
                        Event.ProfileLoaded -> {
                            val newList = withFoss { queryProviders().sorted() }

                            if (newList != providers) {
                                startActivity(ProvidersActivity::class.intent)

                                finish()
                            }
                        }
                        else -> Unit
                    }
                }
                design.requests.onReceive {
                    when (it) {
                        is ProvidersDesign.Request.Update -> {
                            launch {
                                try {
                                    withFoss {
                                        updateProvider(it.provider.type, it.provider.name)
                                    }

                                    design.notifyChanged(it.index)
                                } catch (e: Exception) {
                                    design.showExceptionToast(
                                        getString(
                                            R.string.format_update_provider_failure,
                                            it.provider.name,
                                            e.message
                                        )
                                    )

                                    design.notifyUpdated(it.index)
                                }
                            }
                        }
                    }
                }
                if (activityStarted) {
                    ticker.onReceive {
                        design.updateElapsed()
                    }
                }
            }
        }
    }
}