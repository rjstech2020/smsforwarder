package com.rjs.smsforward.support

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SharedPrefStorage {
    const val SHARED_PREF_NAME = "rjs_forward_sms"
    const val PREF_SMS_POST_RULE = "sms_post_rule"
    const val PREF_SMS_LOGS = "sms_logs"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getSMSRuleData(context: Context, defValue: String?): String? {
        return getPreferences(context).getString(PREF_SMS_POST_RULE, defValue)
    }

    fun saveSMSRuleData(context: Context, value: String) {
        getPreferences(context).edit().putString(PREF_SMS_POST_RULE, value).apply()
    }

    fun addLogEntry(context: Context, message: String, result: String, forwarded: Boolean) {
        val logs = JSONArray(getPreferences(context).getString(PREF_SMS_LOGS, "[]"))
        val entry = JSONObject()
        val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        entry.put("time", dateFormat.format(Date()))
        entry.put("message", if (message.length > 50) message.substring(0, 50) + "..." else message)
        entry.put("result", result)
        entry.put("forwarded", forwarded)

        // Add to front, keep max 10
        val newLogs = JSONArray()
        newLogs.put(entry)
        for (i in 0 until minOf(logs.length(), Constants.MAX_LOG_ENTRIES - 1)) {
            newLogs.put(logs.getJSONObject(i))
        }
        getPreferences(context).edit().putString(PREF_SMS_LOGS, newLogs.toString()).apply()
    }

    fun getLogs(context: Context): JSONArray {
        return JSONArray(getPreferences(context).getString(PREF_SMS_LOGS, "[]"))
    }
}
