package com.young.crccmap.ui

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.UiSettings
import com.amap.api.maps.model.*
import com.young.crccmap.R
import com.young.crccmap.ex.toLagLng
import com.young.crccmap.model.MapLine
import com.young.crccmap.model.MapPoint
import com.young.crccmap.model.MapResult
import com.young.crccmap.other.SpHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.math.ceil


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope(),
    AMap.OnMultiPointClickListener,
    RadioGroup.OnCheckedChangeListener,
    AMap.OnCameraChangeListener,
    AMap.OnMapTouchListener {
    private var aMap: AMap? = null
    private var mUiSettings: UiSettings? = null
    private var drawLineJob: Job? = null
    private var defaultMapLines = mutableListOf<MapLine>()
    private val multiPointOverlay by lazy {
        aMap?.addMultiPointOverlay(MultiPointOverlayOptions().apply {
            icon(BitmapDescriptorFactory.fromResource(R.mipmap.pin))
        })
    }
    private var mapResult: MapResult? = null
    private val loading by lazy {
        MaterialDialog.Builder(this)
            .autoDismiss(false)
            .neutralColor(resources.getColor(R.color.gray_trans))
            .title("数据加载")
            .content("Loading...")
            .progress(true, 0)
            .build()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView.onCreate(savedInstanceState)
        if (aMap == null) {
            aMap = mapView.map
        }
        mapView.onLowMemory()
        initMap()
        initUiSettings()
        injectEvent()
        loading.show()
        launch(Dispatchers.IO) {
            mapResult = SpHelper.instance().getMapInfo()
            mapResult?.let {
                launch(Dispatchers.Main) {
                    loading.dismiss()
                    pointCount.text = "点数:" + it.points.size
                    defaultMapLines = it.lines as MutableList<MapLine>
                    sortByNomal()
                    lineCount.text = "线路数:" + defaultMapLines.size
                    addPoint(it)
                    addLineDefault()
                }
            } ?: loading.dismiss()

        }
    }

    private fun initMap() {
        aMap?.mapType = AMap.MAP_TYPE_NORMAL
        aMap?.setOnMultiPointClickListener(this)
    }

    private fun addPoint(mapResult: MapResult) {
        launch(Dispatchers.IO) {
            val points = mapResult.points
            val boundBuilder = LatLngBounds.Builder()
            val pointItems = async {
                val pointItems = mutableListOf<MultiPointItem>()
                points.forEach {
                    val latlng = it.toLagLng()
                    boundBuilder.include(latlng)
                    val multiPointItem = MultiPointItem(latlng).apply {
                        title = it.name
                        snippet = it.description
                    }
                    pointItems.add(multiPointItem)
                }
                pointItems
            }
            val items = pointItems.await()
            multiPointOverlay?.setItems(items)
            move(boundBuilder.build())
            aMap?.setOnCameraChangeListener(this@MainActivity)
            aMap?.setOnMapTouchListener(this@MainActivity)
        }

    }

    private fun sortByNomal() {
        defaultMapLines.sortByDescending { it.points.size }
    }


    /**
     * 根据到屏幕中心最近距离排序
     */
    private fun sortByFocus() {
        getScreenCenterPoint()
    }

    private fun MapLine.getMiddlePoint(): MapPoint {
        return this.points[this.points.size / 2]

    }


    private fun getScreenCenterPoint() {
        Log.d("MainActivity", "屏幕排序开始")
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(outMetrics)
        val widthPixel = outMetrics.widthPixels
        val heightPixel = outMetrics.heightPixels
        aMap?.let {
            val center = it.projection.fromScreenLocation(Point(widthPixel / 2, heightPixel / 2))
            Log.d("sort", "center = ${center.latitude}   ${center.longitude}")
            defaultMapLines.removeAll { it.points.isEmpty() }
            val x = defaultMapLines[0].points[0].toLagLng()
            Log.d("center", "centerx 钱= ${x.latitude}   ${x.longitude}")
            defaultMapLines.sortBy {
                val middlePoint = it.getMiddlePoint()
                abs(middlePoint.lng.toDouble() - center.longitude)
            }

        }


        Log.d("MainActivity", "屏幕排序完成")
    }

    private fun splitLines(orgLines: MutableList<MapLine>): Array<List<MapLine>?> {
        val max = 5000

        var size = ceil(orgLines.size.toDouble() / max.toDouble()).toInt()

        if (orgLines.size % max != 0) {
            size++
        }
        val arry = arrayOfNulls<List<MapLine>>(size)
        for (position in 0 until size) {
            val endPosition = if (position + max <= orgLines.size) position + max else orgLines.size
            arry[position] = orgLines.subList(position, endPosition)
        }
        return arry

    }


    private fun addLineDefault() {
        Log.d("MainActivity", "开始添加   size = " + defaultMapLines.size)
        if (defaultMapLines.isNullOrEmpty()) {
            return
        }
        drawLineJob = launch(Dispatchers.IO) {
            defaultMapLines.forEachIndexed { index, mapLine ->
                if (drawLineJob?.isCancelled == true) {
                    return@forEachIndexed
                }
                drawLine(mapLine)
                mapLine.isAdd = true
                Log.d("MainActivity", "index = " + index)
            }
        }
    }

    private fun removeAddedLines() {
        try {
            Log.d("MainActivity", "删除添加点 开始" + defaultMapLines.size)
            defaultMapLines.removeAll { it.isAdd }
            Log.d("MainActivity", "删除添加点 完成" + defaultMapLines.size)
        } catch (e: Exception) {
            Log.e("MainActivity", e.message)
            e.printStackTrace()
        }
    }


    @Synchronized
    private fun drawLine(line: MapLine) {
        line.lineStyle?.let {
            aMap?.addPolyline(it)
        }
    }

    private fun move(bound: LatLngBounds) {
        aMap?.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bound,
                0
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
        mapsRoot.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        aMap?.mapType = when (checkedId) {
            R.id.sateMap -> AMap.MAP_TYPE_SATELLITE
            R.id.normalMap -> AMap.MAP_TYPE_NORMAL
            R.id.nightMap -> AMap.MAP_TYPE_NIGHT
            else -> AMap.MAP_TYPE_NORMAL
        }
    }


    override fun onPointClick(p0: MultiPointItem): Boolean {
        markContent.text = "name:${p0.title}    description:${p0.snippet}"
        return false
    }

    override fun onTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touch = true
            }
            MotionEvent.ACTION_MOVE -> {
                moving = true
            }
            MotionEvent.ACTION_UP -> {
                if (!moving) {
                    touch = false
                }
                moving = false
            }

        }
    }

    private var touch = false
    private var moving = false
    override fun onCameraChangeFinish(location: CameraPosition?) {

        if (touch) {
            location?.let {
                kotlin.runCatching {
                    launch {
                        drawLineJob?.cancel()
                        delay(200)
                        removeAddedLines()
                        sortByFocus()
                        addLineDefault()
                    }
                }.onFailure {
                    Log.e("MainActivity", it.message)
                    it.printStackTrace()
                }


            }
            touch = false
        }


    }

    override fun onCameraChange(p0: CameraPosition?) {

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
