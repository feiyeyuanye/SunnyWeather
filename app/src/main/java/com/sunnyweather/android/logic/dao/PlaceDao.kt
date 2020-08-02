package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.MyApplication
import com.sunnyweather.android.logic.model.Place

object PlaceDao {

    fun savePlace(place: Place) {
        sharedPreferences().edit {
//            通过 GSON 将 Place 对象转换成一个 JSON 字符串，然后以字符串存储的方式来保存数据。
            putString("place", Gson().toJson(place))
        }
    }

    fun getSavedPlace(): Place {
//        先将 JSON 字符串取出，然后通过 GSON 解析成 Place 对象并返回。
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    /**
     * 判断是否有数据已被存储
     */
    fun isPlaceSaved() = sharedPreferences().contains("place")


    private fun sharedPreferences() =
        MyApplication.context.getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)

}