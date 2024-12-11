package com.mozilla.firefox.foss.core.bridge

import androidx.annotation.Keep

@Keep
interface TunInterface {
    fun markSocket(fd: Int)
    fun querySocketUid(protocol: Int, source: String, target: String): Int
}