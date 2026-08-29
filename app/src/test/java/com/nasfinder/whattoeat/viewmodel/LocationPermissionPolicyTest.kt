package com.nasfinder.whattoeat.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.nasfinder.whattoeat.data.LocationFallbackPolicy
import com.nasfinder.whattoeat.data.LocationFailureReason
import com.nasfinder.whattoeat.model.AppPage
import com.nasfinder.whattoeat.model.LocationMode
import com.nasfinder.whattoeat.model.RecommendationPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocationPermissionPolicyTest {

    private lateinit var app: Application
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = MainViewModel(app)
    }

    @Test
    fun `fresh launch triggers runtime request when prompt is allowed`() {
        var requestTriggered = false
        viewModel.requestLocationPermission = {
            requestTriggered = true
        }

        viewModel.onForegroundResume(canShowRuntimePrompt = { true })

        assertTrue(requestTriggered)
        assertTrue(viewModel.store.hasRequestedLocationPermission)
        assertFalse(viewModel.showLocationDeniedAlert.value)
    }

    @Test
    fun `permanently denied state shows in-app alert without triggering request loop`() {
        viewModel.store.hasRequestedLocationPermission = true
        var requestTriggered = false
        viewModel.requestLocationPermission = {
            requestTriggered = true
        }

        viewModel.onForegroundResume(canShowRuntimePrompt = { false })

        assertFalse(requestTriggered)
        assertTrue(viewModel.showLocationDeniedAlert.value)
    }

    @Test
    fun `dismissing in-app alert hides dialog`() {
        viewModel.store.hasRequestedLocationPermission = true
        viewModel.onForegroundResume(canShowRuntimePrompt = { false })
        assertTrue(viewModel.showLocationDeniedAlert.value)

        viewModel.dismissLocationDeniedAlert()
        assertFalse(viewModel.showLocationDeniedAlert.value)
    }

    @Test
    fun `permission launcher callback updates request state and flags`() {
        viewModel.store.hasRequestedLocationPermission = true
        var requestTriggered = false
        viewModel.requestLocationPermission = {
            requestTriggered = true
        }

        // Launcher returns denied
        viewModel.onLocationPermissionLauncherResult()

        // Immediate next resume in the same cycle does not loop
        viewModel.onForegroundResume(canShowRuntimePrompt = { true })
        assertFalse(requestTriggered)
    }

    @Test
    fun `recovery message and status text align with permission denial`() {
        val recovery = LocationFallbackPolicy.resolveRecoveryMessage(LocationFailureReason.PERMISSION_DENIED)
        assertEquals("위치 권한을 켜거나, 지역을 직접 지정해 주세요.", recovery)

        val status = LocationFallbackPolicy.resolveStatusText(LocationFailureReason.PERMISSION_DENIED)
        assertEquals("위치 권한이 꺼져 있어요", status)
    }
}
