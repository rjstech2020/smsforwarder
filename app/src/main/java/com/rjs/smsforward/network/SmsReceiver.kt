package com.rjs.smsforward.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SmsMessage

import com.google.firebase.analytics.FirebaseAnalytics
import com.rjs.smsforward.R
import com.rjs.smsforward.SMSRuleData
import com.rjs.smsforward.support.SharedPrefStorage

import org.json.JSONArray

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null || intent.action == null) {
            return
        }
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }
        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (message in smsMessages) {
            applySMSRule(context, message)
        }
    }

    private fun applySMSRule(context: Context, message: SmsMessage) {
        Thread {
            try {
                val smsRuleData = SharedPrefStorage.getSMSRuleData(context, "[]")
                val smsRuleArr = JSONArray(smsRuleData)
                val msgBody = message.messageBody
                var matched = false

                for (i in 0 until smsRuleArr.length()) {
                    val smsRule = SMSRuleData(smsRuleArr.optJSONObject(i))
                    val keywords = smsRule.keyword.lowercase()
                    val negKeywords = smsRule.negKeyword.lowercase()

                    if (keywords.isNotEmpty() && msgBody.lowercase().contains(keywords)) {
                        val shouldBlock = negKeywords.isNotEmpty() && msgBody.lowercase().contains(negKeywords)

                        if (shouldBlock) {
                            SharedPrefStorage.addLogEntry(
                                context, msgBody,
                                "Blocked: matched keyword \"${smsRule.keyword}\" but also matched negative keyword \"${smsRule.negKeyword}\"",
                                false
                            )
                            logAnalyticsEvent(context, "sms_blocked")
                            matched = true
                        } else {
                            fwdMsgToPhone(context, smsRule.phoneNumber, msgBody)
                            Thread.sleep(1000)
                            SharedPrefStorage.addLogEntry(
                                context, msgBody,
                                "Forwarded to ${smsRule.phoneNumber} (keyword: \"${smsRule.keyword}\")",
                                true
                            )
                            logAnalyticsEvent(context, "sms_forwarded")
                            matched = true
                            if (smsRule.stopProcessRule) {
                                break
                            }
                        }
                    }
                }

                if (!matched) {
                    SharedPrefStorage.addLogEntry(
                        context, msgBody,
                        "No matching rule found",
                        false
                    )
                    logAnalyticsEvent(context, "sms_no_match")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun logAnalyticsEvent(context: Context, event: String) {
        try {
            FirebaseAnalytics.getInstance(context).logEvent(event, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fwdMsgToPhone(context: Context, to: String?, message: String?) {
        try {
            val msg = "FWD: " + message
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parts = smsManager.divideMessage(msg)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(to, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(to, null, msg, null, null)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
