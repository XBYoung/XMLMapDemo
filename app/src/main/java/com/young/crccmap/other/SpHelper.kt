package com.young.crccmap.other

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.young.crccmap.model.MapResult
import java.lang.ref.WeakReference

class SpHelper {
    private lateinit var sp:SharedPreferences
    companion object{
        fun instance(): SpHelper {
            return Inner.sp
        }
    }

    private object Inner{
        val sp = SpHelper()
    }

    fun init(context: WeakReference<Context>){
        val rContext = context.get()
        rContext?.let {
            sp =   it.getSharedPreferences("MAP",Context.MODE_PRIVATE)
        }

    }

    fun insert(mapInfo: MapResult){
        val editor = sp.edit()
        editor.putString("mapkey",Gson().toJson(mapInfo))
        editor.commit()
    }

    fun getMapInfo(): MapResult {
        val info = sp.getString("mapkey","")
        return Gson().fromJson(info, MapResult::class.java)
    }
}