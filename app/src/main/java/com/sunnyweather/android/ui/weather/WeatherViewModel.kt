package com.sunnyweather.android.ui.weather

import androidx.lifecycle.*
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Location

class WeatherViewModel : ViewModel() {

    private val locationLiveData = MutableLiveData<Location>()

    /**
     * 和界面相关的数据，放在 ViewModel 中可以保证它们在手机屏幕发生旋转时不会丢失。
     */
    var locationLng = ""
    var locationLat = ""
    var placeName = ""

    /**
     * 调用 Transformations 的 switchMap() 观察 locationLiveData 对象
     */
    val weatherLiveData = Transformations.switchMap(locationLiveData) { location ->
        // 在 switchMap() 的转换函数中调用仓库层中定义的 refreshWeather()，
        // 这样，仓库层返回的 LiveData 对象就可以转换成一个可供 Activity 观察的 LiveData 对象了。
        Repository.refreshWeather(location.lng, location.lat, placeName)
    }

    /**
     * 刷新天气信息
     */
    fun refreshWeather(lng: String, lat: String) {
        // 将传入的经纬度参数封装成一个 Location 对象后赋值给 locationLiveData 对象
        locationLiveData.value = Location(lng, lat)
    }

}