package com.example.myservice

import android.Manifest
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.myservice.constants.Constants.SERVICE_STARTED
import com.example.myservice.constants.Constants.SERVICE_STOPPED
import com.example.myservice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.startButton.setOnClickListener {
            checkNotificationPermissionAllowed()
        }

        binding.stopButton.setOnClickListener {
            sendCommandToService(SERVICE_STOPPED)
        }
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                sendCommandToService(SERVICE_STARTED)
            } else {
                //SHOW ERROR
            }
        }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermissionAllowed() {
        if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            sendCommandToService(SERVICE_STARTED)
        } else {
            requestPermissionLauncher.launch(POST_NOTIFICATIONS)
        }
    }

    private fun sendCommandToService(action: String) {
        Intent(this, MyService::class.java).also {
            it.action = action
            startService(it)
        }
    }
}
