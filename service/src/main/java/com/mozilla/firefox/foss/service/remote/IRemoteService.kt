package com.mozilla.firefox.foss.service.remote

import com.github.kr328.kaidl.BinderInterface

@BinderInterface
interface IRemoteService {
    fun foss(): IFossManager
    fun profile(): IProfileManager
}