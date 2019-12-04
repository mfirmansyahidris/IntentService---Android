package com.telkomsel.intentservice

import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var myReceiver: MyReceiver

    private var mBound = false

    private var mService: MyService? = null

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MyService.LocalBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            mBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myReceiver = MyReceiver()

        if (Utils().requestingLocationUpdates(this)) {
            if(PermissionManager(this).requestLocationPermission()){
                mService?.requestLocationUpdate();
            }
        }
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        mService?.requestLocationUpdate()
        //mService?.removeLocationUpdate()
        bindService(Intent(this, MyService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            myReceiver,
            IntentFilter(MyService().ACTION_BROADCAST)
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver)
        super.onPause()
    }

    override fun onStop() {
        if (mBound) {
            unbindService(mServiceConnection)
            mBound = false
        }
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key.equals(Utils().KEY_REQUESTING_LOCATION_UPDATES)){
            Log.i("MainActivity", "location update changes")
        }
    }

    private class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val location: Location? = intent?.getParcelableExtra(MyService().EXTRA_LOCATION)
            if (location != null) {
                Log.i("MainActivity", "lat: ${location.latitude}, lng: ${location.longitude}")
            }
        }
    }
}
