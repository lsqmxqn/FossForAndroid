package com.mozilla.firefox.foss

import android.content.pm.PackageManager
import com.mozilla.firefox.foss.common.compat.getDrawableCompat
import com.mozilla.firefox.foss.common.constants.Metadata
import com.mozilla.firefox.foss.core.Foss
import com.mozilla.firefox.foss.design.OverrideSettingsDesign
import com.mozilla.firefox.foss.design.model.AppInfo
import com.mozilla.firefox.foss.design.util.toAppInfo
import com.mozilla.firefox.foss.service.store.ServiceStore
import com.mozilla.firefox.foss.util.withFoss
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

class OverrideSettingsActivity : BaseActivity<OverrideSettingsDesign>() {
    override suspend fun main() {
        val configuration = withFoss { queryOverride(Foss.OverrideSlot.Persist) }
        val service = ServiceStore(this)

        defer {
            withFoss {
                patchOverride(Foss.OverrideSlot.Persist, configuration)
            }
        }

        val design = OverrideSettingsDesign(
            this,
            configuration
        )

        setContentDesign(design)

        while (isActive) {
            select<Unit> {
                events.onReceive {

                }
                design.requests.onReceive {
                    when (it) {
                        OverrideSettingsDesign.Request.ResetOverride -> {
                            if (design.requestResetConfirm()) {
                                defer {
                                    withFoss {
                                        clearOverride(Foss.OverrideSlot.Persist)
                                    }

                                    service.sideloadGeoip = ""
                                }

                                finish()
                            }
                        }
                        OverrideSettingsDesign.Request.EditSideloadGeoip -> {
                            withContext(Dispatchers.IO) {
                                val list = querySideloadProviders()
                                val initial = service.sideloadGeoip
                                val exist = list.any { info -> info.packageName == initial }

                                service.sideloadGeoip =
                                    design.requestSelectSideload(if (exist) initial else "", list)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun querySideloadProviders(): List<AppInfo> {
        val apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            .filter {
                it.applicationInfo.metaData?.containsKey(Metadata.GEOIP_FILE_NAME)
                    ?: false
            }
            .map { it.toAppInfo(packageManager) }

        return listOf(
            AppInfo(
                packageName = "",
                label = getString(R.string.use_built_in),
                icon = getDrawableCompat(R.drawable.ic_baseline_work)!!,
                installTime = 0,
                updateDate = 0,
            )
        ) + apps
    }
}