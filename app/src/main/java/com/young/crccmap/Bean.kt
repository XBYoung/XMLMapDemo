package com.young.crccmap


open class MapElement(open var styleId: String = "")

data class LineStyle( val color: String, val width: Int) : MapElement()

data class PointStyle(val icon: String) : MapElement()

/**
 * 点信息
 */
data class MapPoint(val lat: Double, val lng: Double, val pointStyle: PointStyle? = null) : MapElement()

/**
 * 线信息
 */
data class MapLine(val points: List<MapPoint>, val lineStyle: LineStyle? = null) : MapElement()


data class MapResult(val lines:List<MapLine>,val points: List<MapPoint>)



