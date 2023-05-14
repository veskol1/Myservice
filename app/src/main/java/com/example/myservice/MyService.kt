package com.example.myservice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.myservice.constants.Constants.CHANNEL_ID
import com.example.myservice.constants.Constants.CHANNEL_NAME
import com.example.myservice.constants.Constants.NOTIFICATION_ID
import com.example.myservice.constants.Constants.SERVICE_STARTED
import com.example.myservice.constants.Constants.SERVICE_STOPPED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.seconds

class MyService: LifecycleService() {

    var serviceIsRunning = false

    private val timeInMillis = MutableLiveData<Long>()
    private val isTracking = MutableLiveData<Boolean>()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let { it ->
            when (it.action) {
                SERVICE_STARTED -> {
                    initTimer()
                    if (!serviceIsRunning) {
                        serviceIsRunning = true
                        isTracking.postValue(true)
                        startTimer()
                        startForegroundService()
                        Log.d("haha", "service started")

                        timeInMillis.observe(this, Observer {
                            Log.d("haha","millis= "+it)
                            Log.d("haha","sec= "+it.milliseconds)
                            val builder = getNotificationBuilder().setContentText(
                                it.milliseconds.toString()
                            )

                            val notificationManager: NotificationManager =
                                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                            notificationManager.notify(NOTIFICATION_ID, builder.build())
                        })
                    } else {
                        Log.d("haha", "service IS ALREADY started")
                    }

                }
                SERVICE_STOPPED -> {
                    isTracking.value = false
                    Log.d("haha","service stopped")
                    //initTimer()
                    serviceIsRunning = false
                    stopForeground(STOP_FOREGROUND_REMOVE)


                }
                else -> {}
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer() {
        val timeStarted = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                Log.d("haha","serviceIsRunning")
                val lap = System.currentTimeMillis() - timeStarted
                timeInMillis.postValue(lap)
                delay(1000L)
            }
        }
    }

    private fun initTimer() {
        timeInMillis.value = 0L
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(createNotificationChannel)

        startForeground(NOTIFICATION_ID, getNotificationBuilder().build())
    }

    private fun getNotificationBuilder() =
         NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_add_reaction_24)
            .setContentTitle("textTitle")
            .setContentText("00:00:00")
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(getPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)



    @RequiresApi(Build.VERSION_CODES.O)
    private val createNotificationChannel =
        NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE_LOW).apply {
            description = "descriptionText"
        }

    private fun getPendingIntent() = PendingIntent.getActivity(
        this, 0, Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = SERVICE_STARTED
        }, PendingIntent.FLAG_IMMUTABLE
    )

}

