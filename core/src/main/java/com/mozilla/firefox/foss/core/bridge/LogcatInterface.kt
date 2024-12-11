package com.mozilla.firefox.foss.core.bridge

import androidx.annotation.Keep

@Keep
interface LogcatInterface {
    fun received(jsonPayload: String)
}