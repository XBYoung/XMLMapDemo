package com.young.crccmap

class StyleBuilder {
    var id: String = ""
    var color: String = ""
    var width: Int = 1
    var icon: String = ""

    fun buildLineStyle():LineStyle{
        val lineStyle =  LineStyle(color,width).apply { styleId = id }
        clear()
        return lineStyle
    }

    fun buildPointStyle():PointStyle{
        val pointStyle = PointStyle(icon).apply { styleId = id }
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