package com.mozilla.firefox.foss.service.util

import com.mozilla.firefox.foss.service.data.ImportedDao
import com.mozilla.firefox.foss.service.data.PendingDao
import java.util.*

suspend fun generateProfileUUID(): UUID {
    var result = UUID.randomUUID()

    while (ImportedDao().exists(result) || PendingDao().exists(result)) {
        result = UUID.randomUUID()
    }

    return result
}
