package com.practice.qrdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat


fun Context.hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Activity.openSettings() {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        data = Uri.parse("package:$packageName")
    }.run(::startActivity)
}