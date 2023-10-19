package com.rahim.data.sharedPreferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.rahim.utils.Constants.IS_FORCE
import com.rahim.utils.Constants.NAME_SHARED_PREFERENCE
import com.rahim.utils.Constants.SAMPLE_NOTE
import com.rahim.utils.Constants.SAMPLE_ROUTINE
import com.rahim.utils.Constants.UPDATE_VERSION
import com.rahim.utils.Constants.VERSION
import com.rahim.utils.Constants.WELCOME_SHARED
import javax.inject.Inject


class SharedPreferencesCustom @Inject constructor(private val context: Context) {
    private var sharedPreferences =
        context.getSharedPreferences(NAME_SHARED_PREFERENCE, MODE_PRIVATE)
    private var edit = sharedPreferences.edit()

    fun saveWelcomePage(isShow: Boolean) {
        edit.putBoolean(WELCOME_SHARED, isShow)
        edit.apply()
    }

    fun isShowWelcome() = sharedPreferences.getBoolean(WELCOME_SHARED, false)

    fun showSampleRoutine(isShow: Boolean) {
        edit.putBoolean(SAMPLE_ROUTINE, isShow)
        edit.apply()
    }

    fun isShowSampleRoutine() = sharedPreferences.getBoolean(SAMPLE_ROUTINE, false)
    fun showSampleNote(isShow: Boolean) {
        edit.putBoolean(SAMPLE_NOTE, isShow)
        edit.apply()
    }

    fun isSampleNote() = sharedPreferences.getBoolean(SAMPLE_NOTE, false)

}