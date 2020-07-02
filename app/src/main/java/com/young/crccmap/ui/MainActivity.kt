package com.young.crccmap.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.UiSettings
import com.amap.api.maps.model.*
import com.young.crccmap.R
import com.young.crccmap.other.SpHelper
import com.young.crccmap.ex.toLagLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private var aMap: AMap? = null
    private var mUiSettings: UiSettings? = null
    private val multiPointOverlay by lazy {
        aMap?.addMultiPointOverlay(MultiPointOverlayOptions().apply {
            icon(BitmapDescriptorFactory.fromResource(R.mipmap.pin))
        })
    }


    private val mapResult by lazy {
        SpHelper.instance().getMapInfo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (aMap == null) {
            aMap = mapView.map
        }
        mapView.onCreate(savedInstanceState)
        initUiSettings()
        aMap?.mapType = AMap.MAP_TYPE_NORMAL
        addPoint()
        addLine()
        injectEvent()
    }

    private fun addPoint() {
        val points = mapResult.points
        pointCount.text = "点数:"+points.size
        launch(Dispatchers.IO) {
            val boundBuilder = LatLngBounds.Builder()
            val pointItems = async {
                val pointItems = mutableListOf<MultiPointItem>()
                points.forEach {
                    val latlng = it.toLagLng(WeakReference(this@MainActivity))
                    boundBuilder.include(latlng)
                    val multiPointItem = MultiPointItem(latlng)
                    pointItems.add(multiPointItem)
                }
                pointItems
            }
            val items = pointItems.await()
            multiPointOverlay?.setItems(items)
            move(boundBuilder.build())
        }

    }


    private fun addLine() {
        val lines = mapResult.lines
        lineCount.text = "线路数:"+lines.size
        launch(Dispatchers.IO) {

            for (line in lines) {
                val latLngs: MutableList<LatLng> = ArrayList()
                line.points.forEach {
                    val latLng = it.toLagLng(WeakReference(this@MainActivity))
                    latLngs.add(latLng)
                }
                val color = "#${line.lineStyle?.color?:"80FC0404"}"
                val width = line.lineStyle?.width?.toFloat()?:1f
              //  Log.d("Style", "Color = $color   width = $width")
               aMap?.addPolyline(
                    PolylineOptions()
                        .addAll(latLngs)
                        .color(Color.parseColor(color))
                        .width(width)
                )
            }
        }
    }

    private fun move(bound: LatLngBounds) {
        aMap?.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bound,
                50
            )
        )
    }


    private fun initUiSettings() {
        mUiSettings = aMap?.uiSettings
        mUiSettings?.apply {
            isScaleControlsEnabled = false
            logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_CENTER
            setLogoBottomMargin(-200)
            isZoomControlsEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }
    }


    private fun injectEvent() {
        pointCb.setOnCheckedChangeListener { buttonView, isChecked ->
            buttonView.setTextColor(
                if (isChecked) resources.getColor(R.color.colorAccent) else resources.getColor(
                   android.R.color.black
                )
            )
            multiPointOverlay?.setEnable(isChecked)

        }

        lineCb.setOnCheckedChangeListener { buttonView, isChecked ->
            buttonView.setTextColor(
                if (isChecked) resources.getColor(R.color.colorAccent) else resources.getColor(
                    android.R.color.black
                )
            )
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
        mapView.onDestroy()
    }
}
