package com.sunnyweather.android.logic


import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

/**
 * 仓库层的统一封装入口
 * 仓库层有点像是一个数据获取与缓存的中间层
 * 这里会直接发起网络请求，而没有做本地缓存。
 */
object Repository {

    /**
     * 这里是非标准的写法，只是为了代码简单一些。
     * 因为即使 SharedPreferences 的执行速度很快，也应该开启线程来执行，然后通过 LiveData 对象进行数据返回。
     */
    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()

    /**
     * 刷新天气信息
     * 统一封装获取实时天气信息和未来天气信息
     * ---------------------------------
     * 为了提升程序的运行效率，可并发执行获取实时天气信息和未来天气信息的方法。
     * 但是需要同时得到它们的响应结果才能进一步执行操作，因此，可通过协程的 async 函数实现。
     * 因为 async 需要在协程作用域内才能调用，因此使用 coroutineScope 函数创建了一个协程作用域。
     */
    fun refreshWeather(lng: String, lat: String, placeName: String) = fire(Dispatchers.IO) {
        coroutineScope {
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                // 将 Realtime 和 Daily 对象取出并封装到一个 Weather 对象中
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                // 包装 Weather 对象
                Result.success(weather)
            } else {
                // 包装一个异常信息
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }

    /**
     * 为了将异步获取的数据以响应式编程的方式通知给上一层，通常会返回一个 LiveData 对象。
     * 这里的 liveData 函数是 lifecycle-livedata-ktx 库提供的一个非常强大且好用的功能，
     * 它可以自动构建并返回一个 LiveData 对象，然后在它的代码块中提供一个挂起函数的上下文。
     */
//    fun serachPlaces(query:String) = liveData(Dispatchers.IO) {
//        val result = try {
//            // 搜索城市数据
//            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
//            if (placeResponse.status == "ok"){
//                val places = placeResponse.places
//                // 使用 Kotlin 内置的 Result.success() 包装获取的城市数据列表
//                Result.success(places)
//            }else {
//                // 包装一个异常信息
//                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
//            }
//        }catch (e:Exception){
//            Result.failure<List<Place>>(e)
//        }
//        // 使用 emit() 将包装的结果发射出去，
//        // emit() 类似于调用 LiveData 的 setValue() 来通知数据变化，
//        // 只不过这里无法直接取得返回的 LiveData 对象，所以 lifecycle-livedata-ktx 库提供了这样一个替代方法。
//        emit(result)
//    }
    fun serachPlaces(query:String) = fire(Dispatchers.IO){
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok"){
                val places = placeResponse.places
                // 使用 Kotlin 内置的 Result.success() 包装获取的城市数据列表
                Result.success(places)
            }else {
                // 包装一个异常信息
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
    }

    /**
     * 统一对 try catch 进行处理
     * suspend 关键字，表示所有传入的 Lambda 表达式中的代码也是拥有挂起函数上下文的。
     */
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                // 调用传入的 Lambda 表达式中的代码
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            // 最终获取 Lambda 表达式的执行结果，
            // 并将结果发射出去
            emit(result)
        }
}