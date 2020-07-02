package com.young.crccmap.other

import com.young.crccmap.model.LineStyle
import com.young.crccmap.model.PointStyle

class StyleBuilder {
    var id: String = ""
    var color: String = ""
    var width: Int = 1
    var icon: String = ""

    fun buildLineStyle(): LineStyle {
        val lineStyle = LineStyle(id, color, width)
        clear()
        return lineStyle
    }

    fun buildPointStyle(): PointStyle {
        val pointStyle = PointStyle(id, icon)
        clear()
        return pointStyle
    }


    private fun clear(){
        id=""
        color=""
        width = 1
        icon = ""
    }

}