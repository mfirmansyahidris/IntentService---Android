package com.telkomsel.intentservice

import android.os.Bundle
import android.os.Handler
import android.support.v4.os.ResultReceiver


/**
 ****************************************
created by -fi-
.::manca.fi@gmail.com ::.

04/12/2019, 11:08 AM
 ****************************************
 */

class MyResultReceiver(handler: Handler)
    : ResultReceiver(handler) {

    private var mReceiver: Receiver? = null

    interface Receiver {
        fun onReceiveResult(resultCode: Int, resultData: Bundle)

    }

    fun setReceiver(receiver: Receiver) {
        mReceiver = receiver
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {

        if (mReceiver != null) {
            mReceiver!!.onReceiveResult(resultCode, resultData)
        }
    }

}