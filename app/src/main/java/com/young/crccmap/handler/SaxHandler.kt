package com.young.crccmap.handler

import android.util.Log
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class SaxHandler : DefaultHandler() {
    override fun startDocument() {
        super.startDocument()
        Log.d("SAX","startDocument")
    }

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?
    ) {
        super.startElement(uri, localName, qName, attributes)
        Log.d("SAX", "startElement localName = $localName   qName = $qName")
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        super.endElement(uri, localName, qName)
        Log.d("SAX", "endElement localName = $localName   qName = $qName")
    }

    override fun endDocument() {
        super.endDocument()
        Log.d("SAX","endDocument")
    }
}