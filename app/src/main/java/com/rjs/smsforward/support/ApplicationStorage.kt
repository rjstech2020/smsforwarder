package com.rjs.smsforward.support

import androidx.multidex.MultiDexApplication
import com.google.firebase.analytics.FirebaseAnalytics

class ApplicationStorage : MultiDexApplication() {

    companion object {
        @Volatile
        private var mInstance: ApplicationStorage? = null

        fun getInstance(): ApplicationStorage {
            return mInstance ?: synchronized(this) {
                mInstance ?: ApplicationStorage().also { mInstance = it }
            }
        }
    }

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    fun getTracker(): FirebaseAnalytics {
        return mFirebaseAnalytics ?: synchronized(this) {
            mFirebaseAnalytics ?: FirebaseAnalytics.getInstance(this).also { mFirebaseAnalytics = it }
        }
    }
}
