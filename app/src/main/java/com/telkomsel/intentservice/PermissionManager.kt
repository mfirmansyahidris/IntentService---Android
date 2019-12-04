package com.telkomsel.intentservice

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

/**
 ****************************************
created by -fi-
.::manca.fi@gmail.com ::.

27/11/2019, 03:07 PM
 ****************************************
 */

class PermissionManager(private val context: Activity) {

    fun requestLocationPermission(): Boolean {
        var allowed = false
        Dexter.withActivity(context)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        allowed = true
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog("wooow")
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(
                    context,
                    "Permission Error",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()

        return allowed
    }


    fun showSettingsDialog(message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("This is title")
        builder.setMessage(message)
        builder.setPositiveButton("Ok") { dialog, _ ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }


    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivityForResult(intent, 101)
    }
}