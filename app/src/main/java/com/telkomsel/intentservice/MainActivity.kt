package com.telkomsel.intentservice

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.location.Location
import android.location.LocationListener
import android.os.Handler
import android.util.Log


class MainActivity : AppCompatActivity(), MyResultReceiver.Receiver {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mReceiver = MyResultReceiver(Handler())
        mReceiver.setReceiver(this)

        if(PermissionManager(this).requestLocationPermission()){
            val intent = Intent(this, MyService::class.java)
            intent.putExtra("data", "awesome service")
            intent.putExtra("receiverTag", mReceiver);
            startService(intent)
        }
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        Log.d("from service", resultData["ServiceTag"].toString())
    }
}
