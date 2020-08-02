package com.sunnyweather.android.logic.model

/**
 * 用于将 RealtimeResponse 和 DailyResponse 对象封装
 */
class Weather(val realtime: RealtimeResponse.Realtime, val daily: DailyResponse.Daily)