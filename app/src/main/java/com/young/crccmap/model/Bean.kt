package com.young.crccmap.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class MapElement(open var styleId: String = ""):Parcelable

@Parcelize
data class LineStyle( val styleId: String, val color: String, val width: Int) :
    StyleImp {
    override fun getId():String {
        return styleId
    }
}

@Parcelize
data class PointStyle( val styleId: String, val icon: String) :
    StyleImp {
    override fun getId():String {
        return styleId
    }
}

/**
 * 点信息
 */
@Parcelize
data class MapPoint(val lat: String, val lng: String, val pointStyle: PointStyle? = null) :
    MapElementImp

/**
 * 线信息
 */
@Parcelize
data class MapLine(val points: List<MapPoint>, val lineStyle: LineStyle? = null) :
    MapElementImp

@Parcelize
data class MapResult(val lines:List<MapLine>, val points: List<MapPoint>):Parcelable



