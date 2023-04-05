package com.doyouhost.servercommander

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationHandler {
    companion object{
        fun createNotificationChannel(context: Context){
            val name = context.getString(R.string.appName)
            val descriptionText = "---"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =  NotificationChannel("SC", name, importance).apply {
                description = descriptionText
            }

            val notificationManager : NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        @SuppressLint("MissingPermission")
        fun showNotification(context: Context){
            val notificationBuilder = NotificationCompat.Builder(context, "SC")
                .setSmallIcon(R.drawable.network_outline)
                .setContentTitle(context.getString(R.string.notification_no_connection))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setSilent(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(0, notificationBuilder.build())
        }

        fun updateNotification(context: Context, sharedPref: SharedPreferences){
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)== PackageManager.PERMISSION_GRANTED) {
                val notificationBuilder = NotificationCompat.Builder(context, "SC")
                    .setSmallIcon(R.drawable.network_outline)
                    .setContentTitle(when(sharedPref.getBoolean("connectionTested", false)){ false -> context.getString(R.string.notification_no_connection) else -> "Connected to server"})
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOngoing(true)
                    .setSilent(true)

                with(NotificationManagerCompat.from(context)){
                    notify(0, notificationBuilder.build())
                }
            }
        }
    }
}