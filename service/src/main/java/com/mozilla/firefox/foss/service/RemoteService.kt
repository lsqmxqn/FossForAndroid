package com.mozilla.firefox.foss.service

import android.content.Intent
import android.os.IBinder
import com.mozilla.firefox.foss.service.remote.IFossManager
import com.mozilla.firefox.foss.service.remote.IRemoteService
import com.mozilla.firefox.foss.service.remote.IProfileManager
import com.mozilla.firefox.foss.service.remote.wrap
import com.mozilla.firefox.foss.service.util.cancelAndJoinBlocking

class RemoteService : BaseService(), IRemoteService {
    private val binder = this.wrap()

    private var foss: FossManager? = null
    private var profile: ProfileManager? = null
    private var fossBinder: IFossManager? = null
    private var profileBinder: IProfileManager? = null

    override fun onCreate() {
        super.onCreate()

        foss = FossManager(this)
        profile = ProfileManager(this)
        fossBinder = foss?.wrap() as IFossManager?
        profileBinder = profile?.wrap() as IProfileManager?
    }

    override fun onDestroy() {
        super.onDestroy()

        foss?.cancelAndJoinBlocking()
        profile?.cancelAndJoinBlocking()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun foss(): IFossManager {
        return fossBinder!!
    }

    override fun profile(): IProfileManager {
        return profileBinder!!
    }
}