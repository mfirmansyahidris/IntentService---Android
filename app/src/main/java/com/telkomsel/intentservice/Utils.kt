package com.telkomsel.intentservice

import android.content.Context
import android.preference.PreferenceManager

/**
 ****************************************
created by -fi-
.::manca.fi@gmail.com ::.

04/12/2019, 03:35 PM
 ****************************************
 */

class Utils {

    val KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates"

    fun setRequestingLocationUpdates(context: Context, requestingLocationUpdates: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
            .apply()
    }

    fun requestingLocationUpdates(context: Context): Boolean{
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
    }



}