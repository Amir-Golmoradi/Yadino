package com.rahim.ui.main

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.rahim.BuildConfig
import com.rahim.data.modle.dialog.StateOpenDialog
import com.rahim.ui.navigation.NavGraph
import com.rahim.ui.navigation.YadinoApp
import com.rahim.ui.theme.BalticSea
import com.rahim.ui.theme.YadinoTheme
import com.rahim.ui.theme.Zircon
import com.rahim.utils.base.view.requestPermissionNotification
import com.rahim.utils.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()


    private val screenItems = listOf(
        Screen.Home,
        Screen.Routine,
        Screen.Note,
//        Screen.Calender
    )

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel
        Timber.tag("packege").d(this.packageName)
        setContent {
            val context = LocalContext.current
            (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()
            var openDialog by rememberSaveable { mutableStateOf(StateOpenDialog(true, "")) }
            val notificationPermissionState =
                rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
            val navController = rememberNavController()
            val currentDestination = navController.currentBackStackEntry?.destination?.route

            DisposableEffect(systemUiController, useDarkIcons) {
                systemUiController.setSystemBarsColor(
                    color = if (useDarkIcons) Zircon else BalticSea,
                    darkIcons = useDarkIcons
                ) {
                    Color.Black
                }
                onDispose {}
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                YadinoTheme {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    ) {
                        Scaffold(
                            bottomBar = {
                                YadinoApp(navController, screenItems) {
                                    openDialog = it
                                }
                            }, containerColor = MaterialTheme.colorScheme.background
                        ) { innerPadding ->
                            NavGraph(
                                navController,
                                innerPadding = innerPadding,
                                isClickButtonAdd = openDialog, isOpenDialog = {
                                    openDialog = it
                                }
                            )
                            if (currentDestination == Screen.Home.route)
                                requestPermissionNotification(
                                    notificationPermission = notificationPermissionState,
                                    isGranted = {},
                                    permissionState = {
                                        it.launchPermissionRequest()
                                    })
                        }
                    }
                }
            }
        }
        getTokenFirebase()
    }

    private fun getTokenFirebase() {
        if (BuildConfig.DEBUG)
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                val token = task.result
                Timber.tag("token").d(token)
            }
    }
}
