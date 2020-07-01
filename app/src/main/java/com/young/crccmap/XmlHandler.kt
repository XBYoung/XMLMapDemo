package com.young.crccmap

import android.content.Context
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.lang.ref.WeakReference
import javax.xml.parsers.SAXParserFactory

object XmlHandler {
    fun parseXml(context: WeakReference<Context>, assetFileName: String) {
        try {

            var factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
            var parser: XmlPullParser = factory.newPullParser()

            context.get()?.let { context ->
                var input = context.assets.open(assetFileName)
                Log.d("XmlHandler", input.available().toString())
                parser.setInput(input, null)
                var eventType = parser.eventType
                var lastName = ""
                var description = ""
                val styleBuilder = StyleBuilder()
                while (eventType != XmlPullParser.END_DOCUMENT) {

                    when (eventType) {
                        //文档开始事件
                        XmlPullParser.START_DOCUMENT -> {
                            Log.d("PULL start ", "START_DOCUMENT")
                        }

                        XmlPullParser.START_TAG -> {
                            val name = parser.name
                            when (name) {
                                STYLE -> {
                                    parser.toNoNullValue {
                                        styleBuilder.id = it
                                    }
                                }
                                COLOR -> {
                                    parser.toNoNullValue {
                                        styleBuilder.color = it
                                    }

                                }
                                WIDTH -> {
                                    parser.toNoNullValue {
                                        styleBuilder.width = it.toInt()
                                    }

                                }
                                ICON -> {
                                    parser.toNoNullValue {
                                        styleBuilder.icon = it
                                    }

                                }
                            }

                            if (name == "coordinates") {
                                val valueCount = parser.attributeCount
                                when (lastName) {
                                    "LineString" -> {
                                        val txt = parser.text
                                        for (index in 0 until valueCount) {
                                            val value = parser.getAttributeValue(index)
                                            Log.d("value LineString", value)
                                        }
                                    }

                                    "Point" -> {
                                        val value = parser.nextText()
                                        val values = value.split(",")
                                        if (!values.isNullOrEmpty() && values.size >= 2) {
                                            val point =
                                                MapPoint(values[1].toDouble(), value[0].toDouble())
                                        }
                                    }
                                }
                            }
                            lastName = name
                        }

                        //元素结束事件
                        XmlPullParser.END_TAG -> {

                           when(parser.name){
                               LINESTYLE -> {
                                   styleBuilder.buildLineStyle()
                               }
                               ICONSTYLE -> {
                                   styleBuilder.buildPointStyle()
                               }
                           }
                        }
                        else -> {

                        }
                    }
                    eventType = parser.next()
                    if (eventType == XmlPullParser.END_DOCUMENT) {
                        Log.d("PULL", "END_DOCUMENT")
                    }
                }
                Log.d("PULL start", "while end")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("PULL error", e.message)
        }

    }

    fun parseBySax(context: WeakReference<Context>, assetFileName: String) {
        val newInstance = SAXParserFactory.newInstance()
        val saxParser = newInstance.newSAXParser()
        context.get()?.let { context ->
            var input = context.assets.open(assetFileName)
            saxParser.parse(input, MyHandler())
        }


    }
    fun XmlPullParser.toNoNullValue(noNullAfter:(String)->Unit){
        this.nextText()?.let {
            noNullAfter(it)
        }
    }
}