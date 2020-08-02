package com.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place
import retrofit2.http.Query

class PlaceViewModel :ViewModel() {

    private val searchLiveData = MutableLiveData<String>()

    /**
     * 对界面上显示的城市数据进行缓存，
     * 因为原则上与界面相关的数据都应该放到 ViewModel 中，以保证它们在手机屏幕发生旋转时不会丢失。
     */
    val placeList = ArrayList<Place>()

    /**
     * 调用 Transformations 的 switchMap() 观察 searchLiveData 对象，
     * 否则仓库层返回的 LiveData 对象将无法观察。
     */
    val placeLiveData = Transformations.switchMap(searchLiveData){ query ->
        Repository.serachPlaces(query)
    }

    /**
     * 将传入的搜索参数赋值给了一个 searchLiveData 对象
     * -------------------------------------------------
     * 每当此函数被调用时，switchMap() 所对应的转换函数就会执行，
     * 然后在转换函数中，只需要调用仓库层中定义的 serachPlaces() 就可以发起网络请求，
     * 同时将仓库层返回的 LiveData 对象转换成一个可供 Activity 观察的 LiveData 对象。
     */
    fun searchPlaces(query: String){
        searchLiveData.value = query
    }

    /**
     * 因为仓库层中这几个接口的内部没有开启线程，
     * 因此也不必借助 LiveData 对象来观察数据变化，直接调用仓库层中相应的接口并返回即可。
     */
    fun savePlace(place: Place) = Repository.savePlace(place)

    fun getSavedPlace() = Repository.getSavedPlace()

    fun isPlaceSaved() = Repository.isPlaceSaved()

}