package com.mozilla.firefox.foss.service.remote

import com.mozilla.firefox.foss.core.model.FetchStatus
import com.github.kr328.kaidl.BinderInterface

@BinderInterface
fun interface IFetchObserver {
    fun updateStatus(status: FetchStatus)
}