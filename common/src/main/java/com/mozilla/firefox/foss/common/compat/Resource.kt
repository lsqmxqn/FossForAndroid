@file:Suppress("DEPRECATION")

package com.mozilla.firefox.foss.common.compat

import android.content.res.Configuration
import android.os.Build
import java.util.*

val Configuration.preferredLocale: Locale
    get() {
        return if (Build.VERSION.SDK_INT >= 24) {
            locales[0]
        } else {
            locale
        }
    }