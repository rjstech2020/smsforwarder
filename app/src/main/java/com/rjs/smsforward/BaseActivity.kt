package com.rjs.smsforward

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rjs.smsforward.support.ApplicationStorage
import com.rjs.smsforward.support.Constants

open class BaseActivity : AppCompatActivity() {

    private var ruleAddEditDialog: RuleAddEditDialog? = null

    companion object {
        fun recordItemEvent(eventName: String, source: String) {
            try {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, source)
                ApplicationStorage.getInstance().getTracker().logEvent(eventName, bundle)
            } catch (e: Exception) {
                handleException(e)
            }
        }

        fun handleException(e: Throwable) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun recordScreenView(screenName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        ApplicationStorage.getInstance().getTracker().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun showBottomSheetDialog(update: Boolean, position: Int) {
        if (ruleAddEditDialog == null) {
            ruleAddEditDialog = RuleAddEditDialog(this)
        }
        ruleAddEditDialog?.showDialog(update, position)
    }

    open fun ruleListUpdate(rlActionType: Constants.RLActionType, position: Int, smsRule: SMSRuleData?) {}

    fun hideSoftKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
