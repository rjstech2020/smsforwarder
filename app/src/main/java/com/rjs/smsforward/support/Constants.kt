package com.rjs.smsforward.support

object Constants {

    const val SMS_READ_PERMISSION_REQUEST_CODE = 1
    const val MAX_LOG_ENTRIES = 10

    enum class PostItems(val value: String) {
        SMS("SMS")
    }

    enum class Storage {
        ruleKeyword,
        ruleNegKeyword,
        postType,
        postForwardTo,
        stopProcessRule
    }

    enum class RLActionType {
        RULE_ADD,
        RULE_EDIT,
        RULE_DELETE
    }
}
