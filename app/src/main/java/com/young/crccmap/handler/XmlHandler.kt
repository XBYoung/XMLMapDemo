package com.young.crccmap.handler

import android.content.Context
import android.util.Log
import com.young.crccmap.model.StyleImp
import com.young.crccmap.model.*
import com.young.crccmap.other.StyleBuilder
import com.young.crccmap.other.StyleFactory
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.lang.ref.WeakReference
import javax.xml.parsers.SAXParserFactory

object XmlHandler {
    fun parseXml(context: WeakReference<Context>, assetFileName: String): MapResult? {
        var mapResult: MapResult? = null
        val points = mutableListOf<MapPoint>()
        val lines = mutableListOf<MapLine>()
        val styles = mutableListOf<StyleImp>()
        try {
            var factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
            var parser: XmlPullParser = factory.newPullParser()

            context.get()?.let { context ->
                var input = context.assets.open(assetFileName)
                Log.d("XmlHandler", input.available().toString())
                parser.setInput(input, null)
                var eventType = parser.eventType
                var lastName = ""
                var point: MapPoint? = null
                var line: MapLine? = null
                var description = ""
                var styleUrl = ""
                val styleBuilder = StyleBuilder()
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    parser.depth
                    when (eventType) {
                        //文档开始事件
                        XmlPullParser.START_DOCUMENT -> {
                        }

                        XmlPullParser.START_TAG -> {
                            val name = parser.name
                            when (name) {
                                STYLE -> {
                                    parser.parseFirstAttribute {
                                        styleBuilder.id = it
                                    }
                                }
                                COLOR -> {
                                    parser.parseNoNullValue {
                                        styleBuilder.color = it
                                    }
                                }
                                WIDTH -> {
                                    parser.parseNoNullValue {
                                        styleBuilder.width = it.toInt()
                                    }
                                }
                                ICON -> {
                                    parser.parseNoNullValue {
                                        styleBuilder.icon = it
                                    }
                                }
                                STYLEURL -> {
                                    parser.parseNoNullValue {
                                        styleUrl = it
                                    }
                                }
                                COORDINATES -> {
                                    when (lastName) {
                                        LINESTRING -> {
                                            line = null
                                            parser.parseNoNullValue {
                                                val thisPoints = mutableListOf<MapPoint>()
                                                val values = it.split(" ").toMutableList()
                                                values.removeAll { it.isNullOrBlank() }
                                                values.forEach {
                                                    it.split(",")?.let {
                                                        if (it.size > 2) {
                                                            val point =
                                                                MapPoint(
                                                                    it[1],
                                                                    it[0]
                                                                )
                                                            thisPoints.add(point)
                                                        }
                                                    }
                                                }
                                                val mapLine =
                                                    MapLine(
                                                        thisPoints,
                                                        StyleFactory.instance()
                                                            .findStyle(styleUrl) as? LineStyle
                                                    )
                                                lines.add(mapLine)
                                            }
                                        }

                                        POINT -> {
                                            point = null
                                            val value = parser.nextText()
                                            val values = value.split(",")
                                            if (!values.isNullOrEmpty() && values.size >= 2) {
                                                point =
                                                    MapPoint(
                                                        values[1],
                                                        values[0],
                                                        StyleFactory.instance()
                                                            .findStyle(styleUrl) as? PointStyle
                                                    )
                                            }
                                        }
                                    }
                                }
                            }
                            lastName = name
                        }

                        //元素结束事件
                        XmlPullParser.END_TAG -> {
                            when (parser.name) {
                                POINT -> {
                                    point?.let {
                                        points.add(it)
                                    }
                                }
                                LINESTRING -> {
                                    line?.let {
                                        lines.add(it)
                                    }
                                }
                                LINESTYLE -> {
                                    styles.add(styleBuilder.buildLineStyle())
                                    StyleFactory.instance()
                                        .bind(styles)
                                }
                                ICONSTYLE -> {
                                    styles.add(styleBuilder.buildPointStyle())
                                    StyleFactory.instance()
                                        .bind(styles)
                                }
                            }
                        }
                        else -> {

                        }
                    }
                    eventType = parser.next()
                    if (eventType == XmlPullParser.END_DOCUMENT) {
                        mapResult =
                            MapResult(lines, points)
                        Log.d("PULL", "END_DOCUMENT")
                    }
                }
            }
            Log.d("PULL", styles.size.toString() + "          " + styles.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("PULL error", e.message)
        }
        return mapResult
    }


    fun parseBySax(context: WeakReference<Context>, assetFileName: String) {
        val newInstance = SAXParserFactory.newInstance()
        val saxParser = newInstance.newSAXParser()
        context.get()?.let { context ->
            var input = context.assets.open(assetFileName)
            saxParser.parse(input, SaxHandler())
        }


    }


    private fun XmlPullParser.parseNoNullValue(noNullAfter: (String) -> Unit) {
        this.nextText()?.let {
            if (it.contains("#")) {
                val info = it.substringAfter("#")
                noNullAfter(info)
                return
            }
            noNullAfter(it)
        }
    }

    private fun XmlPullParser.parseFirstAttribute(firstAttribute: (String) -> Unit) {
        this.getAttributeValue(0)?.let {
            if (it.contains("#")) {
                firstAttribute(it.substringAfter("#"))
            }
            firstAttribute(it)
        }
    }
}