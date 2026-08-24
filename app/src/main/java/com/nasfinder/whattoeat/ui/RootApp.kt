package com.nasfinder.whattoeat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nasfinder.whattoeat.model.AppPage
import com.nasfinder.whattoeat.ui.components.AppBottomBar
import com.nasfinder.whattoeat.ui.components.AppleMapFailureAlertDialog
import com.nasfinder.whattoeat.ui.components.BusinessInfoAlertDialog
import com.nasfinder.whattoeat.ui.components.MissingMapAlertDialog
import com.nasfinder.whattoeat.ui.components.NotificationDeniedAlertDialog
import com.nasfinder.whattoeat.ui.components.OtherMapPickerDialog
import com.nasfinder.whattoeat.ui.components.PhotoInfoSheet
import com.nasfinder.whattoeat.data.MapProviderHelper
import com.nasfinder.whattoeat.model.MapProvider
import com.nasfinder.whattoeat.theme.MintBase
import com.nasfinder.whattoeat.ui.screens.DecisionScreen
import com.nasfinder.whattoeat.ui.screens.FavoritesScreen
import com.nasfinder.whattoeat.ui.screens.HistoryScreen
import com.nasfinder.whattoeat.ui.screens.HomeScreen
import com.nasfinder.whattoeat.ui.screens.RegionScreen
import com.nasfinder.whattoeat.ui.screens.ResultScreen
import com.nasfinder.whattoeat.ui.screens.SettingsScreen
import com.nasfinder.whattoeat.viewmodel.MainViewModel

@Composable
fun RootApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentPage by viewModel.currentPage.collectAsState()
    val showMissingMapAlert by viewModel.showMissingMapAlert.collectAsState()
    val missingMapProvider by viewModel.missingMapProvider.collectAsState()
    val showOtherMapPicker by viewModel.showOtherMapPicker.collectAsState()
    val showBusinessInfoAlert by viewModel.showBusinessInfoAlert.collectAsState()
    val showNotificationDeniedAlert by viewModel.showNotificationDeniedAlert.collectAsState()
    val showPhotoSheet by viewModel.showPhotoSheet.collectAsState()
    val selectedPhotoInformation by viewModel.selectedPhotoInformation.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MintBase)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 154.dp)) {
                        when (currentPage) {
                            AppPage.HOME -> HomeScreen(viewModel)
                            AppPage.REGION -> RegionScreen(viewModel)
                            AppPage.RESULT -> ResultScreen(viewModel)
                            AppPage.DECISION -> DecisionScreen(viewModel)
                            AppPage.HISTORY -> HistoryScreen(viewModel)
                            AppPage.FAVORITES -> FavoritesScreen(viewModel)
                            AppPage.PROFILE -> SettingsScreen(viewModel)
                        }
                    }

                    AppBottomBar(
                        currentPage = viewModel.bottomBarPage,
                        onTabSelected = { viewModel.navigateTo(it) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    )
                }
            }
        }

        if (showMissingMapAlert && missingMapProvider != null) {
            val provider = missingMapProvider!!
            if (provider == MapProvider.APPLE) {
                AppleMapFailureAlertDialog(onDismiss = { viewModel.dismissMissingMapAlert() })
            } else {
                MissingMapAlertDialog(
                    provider = provider,
                    onInstall = { viewModel.installMissingMap(context) },
                    onChooseOther = { viewModel.openOtherMapPicker() },
                    onCancel = { viewModel.dismissMissingMapAlert() }
                )
            }
        }

        if (showOtherMapPicker) {
            val installed = MapProviderHelper.getInstalledProviders(context)
            OtherMapPickerDialog(
                installedProviders = installed,
                onSelect = { viewModel.selectOtherMapProviderAndOpen(context, it) },
                onCancel = { viewModel.dismissOtherMapPicker() }
            )
        }

        if (showBusinessInfoAlert) {
            BusinessInfoAlertDialog(onDismiss = { viewModel.dismissBusinessInfo() })
        }

        if (showNotificationDeniedAlert) {
            NotificationDeniedAlertDialog(onDismiss = { viewModel.dismissNotificationDeniedAlert() })
        }

        if (showPhotoSheet && selectedPhotoInformation != null) {
            PhotoInfoSheet(
                information = selectedPhotoInformation!!,
                onOpenUrl = { url ->
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // No browser available; ignore per no-crash policy
                    }
                },
                onDismiss = { viewModel.dismissPhotoInformation() }
            )
        }
    }
}
