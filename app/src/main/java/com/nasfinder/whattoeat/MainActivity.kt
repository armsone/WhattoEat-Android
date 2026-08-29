package com.nasfinder.whattoeat

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.nasfinder.whattoeat.data.NotificationHelper
import com.nasfinder.whattoeat.theme.WhattoEatTheme
import com.nasfinder.whattoeat.ui.RootApp
import com.nasfinder.whattoeat.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    val viewModel: MainViewModel by viewModels()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.onLocationPermissionLauncherResult()
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onNotificationPermissionResult(granted, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!viewModel.handleSystemBack()) {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )

        viewModel.requestLocationPermission = {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        viewModel.requestNotificationPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.onNotificationPermissionResult(true, this)
            }
        }

        NotificationHelper.createNotificationChannel(this)

        viewModel.applyMatchupState(intent.getStringExtra("matchup_state"))

        setContent {
            WhattoEatTheme {
                RootApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onForegroundResume(
            canShowRuntimePrompt = { permissions ->
                if (!viewModel.store.hasRequestedLocationPermission) {
                    true
                } else {
                    permissions.any {
                        ActivityCompat.shouldShowRequestPermissionRationale(this, it)
                    }
                }
            }
        )
    }
}
