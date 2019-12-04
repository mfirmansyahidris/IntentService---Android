package com.telkomsel.intentservice

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.support.v4.os.ResultReceiver
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*


/**
 ****************************************
created by -fi-
.::manca.fi@gmail.com ::.

04/12/2019, 09:38 AM
 ****************************************
 */


class MyService : IntentService("MyService") {
    private val PACKAGE_NAME =
        "com.google.android.gms.location.sample.locationupdatesforegroundservice"
    val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"
    val EXTRA_LOCATION = "$PACKAGE_NAME.location"
    private val EXTRA_START_FROM_NOTIFICATION = "$PACKAGE_NAME.started_from_notification"
    private val NOTIFICATION_ID = 5353
    private val CHANNEL_ID = "channel_01"
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = UPDATE_INTERVAL_IN_MILLISECONDS / 2


    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private lateinit var mLocationCallback: LocationCallback

    private lateinit var mLocation: Location

    private lateinit var mNotificationManager: NotificationManager

    private lateinit var mLocationRequest: LocationRequest

    private lateinit var mServiceHandler: Handler

    private var mChangingConfiguration = false

    private val mBinder = LocalBinder()

    private val b = Bundle()

    override fun onCreate() {
        super.onCreate()
        Log.d("MyService", "creating...")

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                onNewLocation(p0?.lastLocation)
            }
        }

        createLocationRequest()
        getLastLocation()

        val handlerThread = HandlerThread("MyService")
        handlerThread.start()

        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel =
                NotificationChannel(CHANNEL_ID, "name", NotificationManager.IMPORTANCE_DEFAULT)
            mNotificationManager.createNotificationChannel(mChannel)
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyService", "starting...")

        val startFromNotification = intent?.getBooleanExtra(EXTRA_START_FROM_NOTIFICATION, false)

        if (startFromNotification!!) {
            removeLocationUpdate()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("MyService", "on binding...")
        stopForeground(true)
        mChangingConfiguration = false
        return mBinder
    }

    override fun onRebind(intent: Intent?) {
        Log.i("MyService", "rebinding...")
        stopForeground(true)
        mChangingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i("MyService", "unbinding...")
        if (!mChangingConfiguration && Utils().requestingLocationUpdates(this)) {
            Log.i("MyServices", "starting foreground services...")
            startForeground(NOTIFICATION_ID, getNotification())
        }
        return true
    }

    override fun onDestroy() {
        Log.i("MyServices", "destroying...")
        mServiceHandler.removeCallbacksAndMessages(null)
    }

    fun requestLocationUpdate() {
        Log.i("MyService", "requesting location updates")
        Utils().setRequestingLocationUpdates(this, true)
        startService(Intent(applicationContext, MyService::class.java))
        try {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.myLooper()
            )
        } catch (e: SecurityException) {
            Log.e("MyServices", "Lost location permission, could not request updates: $e")
            Utils().setRequestingLocationUpdates(this, false)
        }
    }

    override fun onHandleIntent(intent: Intent) {
        val rec: ResultReceiver = intent.getParcelableExtra("receiverTag")
        while (true) {
            mFusedLocationClient.lastLocation.addOnSuccessListener {
                Log.d("My Current location", "Lat : ${it?.latitude} Long : ${it?.longitude}")
                b.putString("ServiceTag", "")
                rec.send(0, b)
            }
            Thread.sleep(2000)
        }
    }

    private fun onNewLocation(location: Location?) {
        if (location != null) {
            mLocation = location
            val intent = Intent(ACTION_BROADCAST)
            intent.putExtra(EXTRA_LOCATION, mLocation)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

            if (serviceIsRunningInBackground(this)) {
                mNotificationManager.notify(NOTIFICATION_ID, getNotification())
            }
        }

    }


    private fun serviceIsRunningInBackground(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(
            Integer.MAX_VALUE
        )) {
            if (javaClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    private fun getNotification(): Notification {
        val intent = Intent(this, MyService::class.java)
        intent.putExtra(EXTRA_START_FROM_NOTIFICATION, true)

        val activityPendingIntent =
            PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)
        val servicePendingIntent =
            PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .addAction(0, "launch", activityPendingIntent)
            .addAction(0, "cancel", servicePendingIntent)
            .setContentText("ini text")
            .setContentTitle("ini title")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)
        //.setTicker()
        //.setWhen()
        return builder.build()
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient.lastLocation
                .addOnCompleteListener {
                    if (it.isSuccessful && it.result != null) {
                        mLocation = it.result!!
                    } else {
                        Log.w("MyService", "Failed to get Location")
                    }
                }
        } catch (e: SecurityException) {
            Log.e("MySercive", "lost location permission $e")
        }
    }

    fun removeLocationUpdate() {
        Log.i("MyService", "Removing location service...")
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)
            Utils().setRequestingLocationUpdates(this, false)
            stopSelf()
        } catch (e: SecurityException) {
            Utils().setRequestingLocationUpdates(this, true)
            Log.e("MyService", "lost location permission. could not remove update: $e")
        }
    }

    inner class LocalBinder : Binder() {
        internal val service: MyService
            get() = this@MyService
    }
}
