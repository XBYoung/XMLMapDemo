package com.young.crccmap.model

import android.os.Parcelable
import com.amap.api.maps.model.PolylineOptions
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
data class MapPoint(val lat: String, val lng: String,val name:String = "", val description :String= "",val pointStyle: PointStyle? = null) :
    MapElementImp

/**
 * 线信息
 */
@Parcelize
data class MapLine(val points: List<MapPoint>,var isAdd:Boolean = false, val lineStyle: PolylineOptions? = null) :
    MapElementImp

@Parcelize
data class MapResult(val lines:List<MapLine>, val points: List<MapPoint>):Parcelable



