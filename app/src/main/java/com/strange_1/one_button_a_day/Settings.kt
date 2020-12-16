package com.strange_1.one_button_a_day

import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.random.nextULong

class Settings(data: String?) {
    var id: String = ""
    var isFirstRun: Boolean = true
        get() = field
        set(value) {
            field = value
        }
    var firstTime: Long = 0
    var dailyTime: Long = 0
    var highScore: Int = 0
    var pushList: ArrayList<PushSet> = ArrayList()

    class PushSet(var date: Long, var count: Int)

    init {
        if (data == null) {
            id = Random.nextULong().toString()
        } else {
            readData(data)
        }
    }

    private fun readData(data: String) {
        val jObjects = JSONObject(data)
        id = jObjects.getString("id")
        isFirstRun = jObjects.getBoolean("FirstRun")
        firstTime = jObjects.getLong("Time")
        dailyTime = firstTime % (1000L*60*60*24)
        highScore = jObjects.getInt("HighScore")
        val pushObject = jObjects.getJSONArray("PushList")
        for (i in 1 until pushObject.length()) {
            pushList.add(PushSet(
                    pushObject.getJSONObject(i).getLong("Date"),
                    pushObject.getJSONObject(i).getInt("Count")
            ))
        }
    }

    fun getData(): String {
        val pushObject = JSONArray()
        var pushData: JSONObject
        for (i in pushList) {
            pushData = JSONObject()
            pushData.put("Date", i.date)
            pushData.put("Count", i.count)
            pushObject.put(pushData)
        }
        val jObject = JSONObject()
        jObject.put("id", id)
        jObject.put("FirstRun", isFirstRun)
        jObject.put("Time", firstTime)
        jObject.put("HighScore", highScore)
        jObject.put("PushList", pushObject)

        return jObject.toString()
    }
}