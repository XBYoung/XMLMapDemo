package com.young.crccmap.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.young.crccmap.*
import com.young.crccmap.R
import com.young.crccmap.handler.XmlHandler
import com.young.crccmap.model.MapResult
import com.young.crccmap.other.SpHelper
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.*
import permissions.dispatcher.*
import java.lang.ref.WeakReference

@RuntimePermissions
class SplashActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private var mapResult: MapResult? = null

    /**
     * 第一次申请权限并且通过
     */
    @NeedsPermission(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE
    )
    fun necessaryPermission() {
        toNextPage()
    }


    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }


    /**
     * 第一次拒绝并没有勾选不在询问之后请求权限
     */
    @OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)
    fun requestPermissionAgain(request: PermissionRequest) {
        Toast.makeText(this, "使用地图需要相关权限", Toast.LENGTH_LONG).show()
        request.proceed()
    }

    /**
     * 申请权限被拒绝
     */
    @OnPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)
    fun alertAfterDenied() {
        Toast.makeText(this, "使用地图需要相关权限", Toast.LENGTH_LONG).show()

    }

    /**
     * 申请权限被拒绝，并且勾选不在询问
     */
    @OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)
    fun alertAfterNeverAsk() {
        Toast.makeText(this, "定位或存储权限缺失将部分无法使用", Toast.LENGTH_LONG).show()
    }


    private fun toNextPage() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun startParse() {
        val loading =
            MaterialDialog.Builder(this)
                .autoDismiss(false)
                .neutralColor(resources.getColor(R.color.gray_trans) )
                .title("数据解析")
                .content("Loading...")
                .progress(true, 0)
                .show()
        kotlin.runCatching {
            launch(Dispatchers.IO) {
                kotlin.runCatching {
                    val start = System.currentTimeMillis()
                    mapResult = XmlHandler.parseXml(
                        WeakReference(application.applicationContext),
                        "cmcc.kml"
                    )
                    mapResult?.let {
                        SpHelper.instance().insert(it)
                    }
                    Log.d("mapResult", mapResult.toString())
                    val end = System.currentTimeMillis()
                    val duration = end - start
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@SplashActivity, duration.toString(), Toast.LENGTH_SHORT)
                            .show()
                        loading.dismiss()
                    }

                }.onFailure {
                    it.printStackTrace()
                    launch(Dispatchers.Main) {
                        loading.dismiss()
                    }
                }
            }
        }.onFailure {
            it.printStackTrace()
            Toast.makeText(this@SplashActivity, it.message + " out", Toast.LENGTH_SHORT).show()
            loading.dismiss()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        startParse()
        mapBtn.setOnClickListener {
            mapResult?.let {
               necessaryPermissionWithPermissionCheck()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}
