package com.young.crccmap.ex

import android.content.Context
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.LatLng
import com.young.crccmap.model.MapPoint
import java.lang.ref.WeakReference

fun MapPoint.toLagLng(context: WeakReference<Context>): LatLng {
    return LatLng(this.lat.toDouble(), this.lng.toDouble()).toGDLatLng(context)
}

fun LatLng.toGDLatLng(context: WeakReference<Context>): LatLng {
    context.get()?.let {
        val converter = CoordinateConverter(it)
        converter.from(CoordinateConverter.CoordType.GPS)
        converter.coord(this)
        return converter.convert()
    }
    return this
}