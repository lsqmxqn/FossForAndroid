package com.mozilla.firefox.foss.service.remote

import com.mozilla.firefox.foss.core.model.LogMessage
import com.github.kr328.kaidl.BinderInterface

@BinderInterface
interface ILogObserver {
    fun newItem(log: LogMessage)
}