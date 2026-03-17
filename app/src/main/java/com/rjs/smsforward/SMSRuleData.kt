package com.rjs.smsforward

import com.rjs.smsforward.support.Constants
import org.json.JSONObject

class SMSRuleData(private val ruleData: JSONObject) {

    val keyword: String
        get() = ruleData.optString(Constants.Storage.ruleKeyword.name)

    val negKeyword: String
        get() = ruleData.optString(Constants.Storage.ruleNegKeyword.name)

    val phoneNumber: String
        get() = ruleData.optString(Constants.Storage.postForwardTo.name)

    val stopProcessRule: Boolean
        get() = ruleData.optBoolean(Constants.Storage.stopProcessRule.name)
}
