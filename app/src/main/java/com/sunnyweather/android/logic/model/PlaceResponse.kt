package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

/**
 * 根据 JSON 格式数据，定义数据模型
 * ---------------------------
 * 由于 JSON 中一些字段的命名可能与 Kotlin 的命名规范不太一致，
 * 因此使用 @SerializedName 注解的方式，来让 JSON 字段和 Kotlin 字段之间建立映射关系。
 */

class PlaceResponse(val status: String, val places: List<Place>)

class Place(val name: String, val location: Location, @SerializedName("formatted_address") val address: String)

class Location(val lng: String, val lat: String)