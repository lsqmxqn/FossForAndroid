package com.mozilla.firefox.foss.core.bridge

import androidx.annotation.Keep

@Keep
class FossException(msg: String) : IllegalArgumentException(msg)