package com.young.crccmap.other

import android.util.Log
import com.young.crccmap.model.StyleImp

class StyleFactory {
    companion object{
        fun instance(): StyleFactory {
            return Inner.inner
        }
    }
    private  object Inner{
       open val inner = StyleFactory()

    }
    private lateinit var styles : MutableList<StyleImp>
    fun bind(binds:MutableList<StyleImp>){
        this.styles = binds
    }

    fun findStyle(id:String): StyleImp?{
        val style = styles.find { it.getId() == id }
        Log.d("style","id = "+id+"   "+style.toString())
       return style
    }
}