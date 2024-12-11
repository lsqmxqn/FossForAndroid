package com.mozilla.firefox.foss.common.constants

import android.content.ComponentName
import com.mozilla.firefox.foss.common.util.packageName

object Components {
    private const val componentsPackageName = "com.mozilla.firefox.foss"

    val MAIN_ACTIVITY = ComponentName(packageName, "$componentsPackageName.MainActivity")
    val PROPERTIES_ACTIVITY = ComponentName(packageName, "$componentsPackageName.PropertiesActivity")
}