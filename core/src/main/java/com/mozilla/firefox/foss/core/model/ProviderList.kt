package com.mozilla.firefox.foss.core.model

import android.os.Parcel
import android.os.Parcelable
import com.mozilla.firefox.foss.common.util.createListFromParcelSlice
import com.mozilla.firefox.foss.common.util.writeToParcelSlice

class ProviderList(data: List<Provider>) : List<Provider> by data, Parcelable {
    constructor(parcel: Parcel) : this(Provider.createListFromParcelSlice(parcel, 0, 20))

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        return writeToParcelSlice(parcel, flags)
    }

    companion object CREATOR : Parcelable.Creator<ProviderList> {
        override fun createFromParcel(parcel: Parcel): ProviderList {
            return ProviderList(parcel)
        }

        override fun newArray(size: Int): Array<ProviderList?> {
            return arrayOfNulls(size)
        }
    }
}