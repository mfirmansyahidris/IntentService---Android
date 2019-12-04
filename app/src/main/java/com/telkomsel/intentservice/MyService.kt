package com.telkomsel.intentservice

import android.app.IntentService
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v4.os.ResultReceiver
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices


/**
 ****************************************
created by -fi-
.::manca.fi@gmail.com ::.

04/12/2019, 09:38 AM
 ****************************************
 */


class MyService : IntentService("MyService"){

    private lateinit var mFusedLocation: FusedLocationProviderClient
    private val b = Bundle()

    override fun onCreate() {
        super.onCreate()
        Log.d("MyService", "starting..")
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onHandleIntent(intent: Intent) {
        val rec: ResultReceiver = intent.getParcelableExtra("receiverTag")
        while (true){
            mFusedLocation.lastLocation.addOnSuccessListener {
                Log.d("My Current location", "Lat : ${it?.latitude} Long : ${it?.longitude}")
                b.putString("ServiceTag", "")
                rec.send(0, b)
            }
            Thread.sleep(2000)
        }
    }
}
