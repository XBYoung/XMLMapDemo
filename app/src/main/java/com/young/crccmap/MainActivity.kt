package com.young.crccmap

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity(),CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
     //   startParse()
    }

    override fun onResume() {
        super.onResume()
        startParse()
    }
    private fun startParse(){
        kotlin.runCatching {

            launch (Dispatchers.IO){
                kotlin.runCatching {
                    val start = System.currentTimeMillis()

                    XmlHandler.parseXml(WeakReference(application.applicationContext), "cmcc.kml")
                    //XmlHandler.parseBySax(WeakReference(application.applicationContext), "cmcc.kml")
                    val end = System.currentTimeMillis()
                    val duration = end - start
                    Log.d("duration", duration.toString())
                    launch (Dispatchers.Main){
                        Toast.makeText(this@MainActivity,duration.toString(),Toast.LENGTH_SHORT).show()
                    }

                }.onFailure {
                    it.printStackTrace()
                    Log.d("duration inner",it.message)
                    launch (Dispatchers.Main){
                        Toast.makeText(this@MainActivity,it.message+" in",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.onFailure {
            it.printStackTrace()
            Log.d("duration outer",it.message)
            Toast.makeText(this@MainActivity,it.message+" out",Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}
