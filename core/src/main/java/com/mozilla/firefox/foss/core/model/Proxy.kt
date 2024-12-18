package com.mozilla.firefox.foss.core.model

import android.os.Parcel
import android.os.Parcelable
import com.mozilla.firefox.foss.core.util.Parcelizer
import kotlinx.serialization.Serializable

@Serializable
data class Proxy(
    val name: String,
    val title: String,
    val subtitle: String,
    val type: Type,
    val delay: Int,
) : Parcelable {
    @Suppress("unused")
    enum class Type(val group: Boolean) {
        Direct(false),
        Reject(false),

        Shadowsocks(false),
        ShadowsocksR(false),
        Snell(false),
        Socks5(false),
        Http(false),
        Vmess(false),
        Trojan(false),

        Relay(true),
        Selector(true),
        Fallback(true),
        URLTest(true),
        LoadBalance(true),

        Unknown(false);
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        Parcelizer.encodeToParcel(serializer(), parcel, this)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Proxy> {
        override fun createFromParcel(parcel: Parcel): Proxy {
            return Parcelizer.decodeFromParcel(serializer(), parcel)
        }

        override fun newArray(size: Int): Array<Proxy?> {
            return arrayOfNulls(size)
        }
    }
}
