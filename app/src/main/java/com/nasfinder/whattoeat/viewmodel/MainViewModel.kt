package com.nasfinder.whattoeat.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nasfinder.whattoeat.data.ApiClient
import com.nasfinder.whattoeat.data.ApiException
import com.nasfinder.whattoeat.data.ChoiceStore
import com.nasfinder.whattoeat.data.LocationResult
import com.nasfinder.whattoeat.data.LocationService
import com.nasfinder.whattoeat.data.MapProviderHelper
import com.nasfinder.whattoeat.data.MenuPolicy
import com.nasfinder.whattoeat.data.NearbyRegion
import com.nasfinder.whattoeat.data.NotificationHelper
import com.nasfinder.whattoeat.data.PhotoRefreshPolicy
import com.nasfinder.whattoeat.BuildConfig
import com.nasfinder.whattoeat.model.AppPage
import com.nasfinder.whattoeat.model.ChoiceRecord
import com.nasfinder.whattoeat.model.Decision
import com.nasfinder.whattoeat.model.FavoriteRecord
import com.nasfinder.whattoeat.model.LocationMode
import com.nasfinder.whattoeat.model.MapProvider
import com.nasfinder.whattoeat.model.PhotoInformation
import com.nasfinder.whattoeat.model.RecommendationPhase
import com.nasfinder.whattoeat.model.RegionUsage
import com.nasfinder.whattoeat.model.ReminderLeadTime
import com.nasfinder.whattoeat.model.Restaurant
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

internal fun shouldFreezeMatchupLoadingAnimation(state: String?, isDebug: Boolean): Boolean =
    isDebug && state == "loading"

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication()
    val store = ChoiceStore(context)
    val locationService = LocationService(context)

    // Navigation state
    private val _currentPage = MutableStateFlow(AppPage.HOME)
    val currentPage: StateFlow<AppPage> = _currentPage.asStateFlow()

    private var pageBeforeSettings: AppPage = AppPage.HOME

    private val _matchupRegionSearchFocused = MutableStateFlow(false)
    val matchupRegionSearchFocused: StateFlow<Boolean> = _matchupRegionSearchFocused.asStateFlow()
    var freezeMatchupLoadingAnimation: Boolean = false
        private set

    val bottomBarPage: AppPage
        get() = when (_currentPage.value) {
            AppPage.DECISION -> AppPage.RESULT
            AppPage.PROFILE -> if (pageBeforeSettings == AppPage.DECISION) AppPage.RESULT else pageBeforeSettings
            else -> _currentPage.value
        }

    // Location mode & values
    private val _locationMode = MutableStateFlow(LocationMode.AUTO)
    val locationMode: StateFlow<LocationMode> = _locationMode.asStateFlow()

    private val _currentRegionName = MutableStateFlow("")
    val currentRegionName: StateFlow<String> = _currentRegionName.asStateFlow()

    private val _currentLatitude = MutableStateFlow<Double?>(null)
    val currentLatitude: StateFlow<Double?> = _currentLatitude.asStateFlow()

    private val _currentLongitude = MutableStateFlow<Double?>(null)
    val currentLongitude: StateFlow<Double?> = _currentLongitude.asStateFlow()

    // Region screen state
    private val _regionStatusText = MutableStateFlow("현 위치를 다시 확인할 수 있어요")
    val regionStatusText: StateFlow<String> = _regionStatusText.asStateFlow()

    private val _isResolvingLocation = MutableStateFlow(false)
    val isResolvingLocation: StateFlow<Boolean> = _isResolvingLocation.asStateFlow()

    private val _nearbyRegions = MutableStateFlow<List<NearbyRegion>>(emptyList())
    val nearbyRegions: StateFlow<List<NearbyRegion>> = _nearbyRegions.asStateFlow()

    private val _frequentRegions = MutableStateFlow<List<RegionUsage>>(emptyList())
    val frequentRegions: StateFlow<List<RegionUsage>> = _frequentRegions.asStateFlow()

    // Recommendation state
    private val _recommendationPhase = MutableStateFlow(RecommendationPhase.IDLE)
    val recommendationPhase: StateFlow<RecommendationPhase> = _recommendationPhase.asStateFlow()

    private val _mainRestaurant = MutableStateFlow<Restaurant?>(null)
    val mainRestaurant: StateFlow<Restaurant?> = _mainRestaurant.asStateFlow()

    private val _carouselRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val carouselRestaurants: StateFlow<List<Restaurant>> = _carouselRestaurants.asStateFlow()

    private val _recommendationError = MutableStateFlow<String?>(null)
    val recommendationError: StateFlow<String?> = _recommendationError.asStateFlow()

    private val _loadingSeconds = MutableStateFlow(0)
    val loadingSeconds: StateFlow<Int> = _loadingSeconds.asStateFlow()

    private var activeRequestToken: Long = 0L
    private var loadingTimerJob: Job? = null

    // Hooks wired by MainActivity to OS permission launchers
    var requestLocationPermission: (() -> Unit)? = null
    var requestNotificationPermission: (() -> Unit)? = null
    private var pendingAutoRecommendationAfterPermission = false
    private var pendingNotifyEnableContext: Context? = null

    // Decision state
    private val _currentDecision = MutableStateFlow<Decision?>(null)
    val currentDecision: StateFlow<Decision?> = _currentDecision.asStateFlow()

    private val _isCurrentDecisionRecorded = MutableStateFlow(false)
    val isCurrentDecisionRecorded: StateFlow<Boolean> = _isCurrentDecisionRecorded.asStateFlow()

    // History & Favorites state
    private val _choiceRecords = MutableStateFlow<List<ChoiceRecord>>(emptyList())
    val choiceRecords: StateFlow<List<ChoiceRecord>> = _choiceRecords.asStateFlow()

    private val _favoriteRecords = MutableStateFlow<List<FavoriteRecord>>(emptyList())
    val favoriteRecords: StateFlow<List<FavoriteRecord>> = _favoriteRecords.asStateFlow()

    // Modals / Sheets / Alerts
    private val _selectedPhotoInformation = MutableStateFlow<PhotoInformation?>(null)
    val selectedPhotoInformation: StateFlow<PhotoInformation?> = _selectedPhotoInformation.asStateFlow()

    private val _showPhotoSheet = MutableStateFlow(false)
    val showPhotoSheet: StateFlow<Boolean> = _showPhotoSheet.asStateFlow()

    private val _showBusinessInfoAlert = MutableStateFlow(false)
    val showBusinessInfoAlert: StateFlow<Boolean> = _showBusinessInfoAlert.asStateFlow()

    private val _showMissingMapAlert = MutableStateFlow(false)
    val showMissingMapAlert: StateFlow<Boolean> = _showMissingMapAlert.asStateFlow()

    private val _missingMapProvider = MutableStateFlow<MapProvider?>(null)
    val missingMapProvider: StateFlow<MapProvider?> = _missingMapProvider.asStateFlow()

    private val _showOtherMapPicker = MutableStateFlow(false)
    val showOtherMapPicker: StateFlow<Boolean> = _showOtherMapPicker.asStateFlow()

    // Settings state
    private val _locationPermissionStatus = MutableStateFlow("사용 중")
    val locationPermissionStatus: StateFlow<String> = _locationPermissionStatus.asStateFlow()

    private val _selectedMapProvider = MutableStateFlow(MapProvider.NAVER)
    val selectedMapProvider: StateFlow<MapProvider> = _selectedMapProvider.asStateFlow()

    private val _lunchNotifyEnabled = MutableStateFlow(false)
    val lunchNotifyEnabled: StateFlow<Boolean> = _lunchNotifyEnabled.asStateFlow()

    private val _lunchHour = MutableStateFlow(12)
    val lunchHour: StateFlow<Int> = _lunchHour.asStateFlow()

    private val _lunchMinute = MutableStateFlow(0)
    val lunchMinute: StateFlow<Int> = _lunchMinute.asStateFlow()

    private val _lunchLeadTime = MutableStateFlow(ReminderLeadTime.MIN_5)
    val lunchLeadTime: StateFlow<ReminderLeadTime> = _lunchLeadTime.asStateFlow()

    private val _isCopyrightExpanded = MutableStateFlow(false)
    val isCopyrightExpanded: StateFlow<Boolean> = _isCopyrightExpanded.asStateFlow()

    private val _showNotificationDeniedAlert = MutableStateFlow(false)
    val showNotificationDeniedAlert: StateFlow<Boolean> = _showNotificationDeniedAlert.asStateFlow()

    init {
        // Initial load from store
        _choiceRecords.value = store.getChoiceRecords()
        _favoriteRecords.value = store.getFavoriteRecords()
        _frequentRegions.value = store.getTopFrequentRegions(3)
        _selectedMapProvider.value = store.mapProvider
        _lunchNotifyEnabled.value = store.lunchNotifyEnabled
        _lunchHour.value = store.lunchHour
        _lunchMinute.value = store.lunchMinute
        _lunchLeadTime.value = ReminderLeadTime.fromMinutes(store.lunchLeadMinutes)

        // Manual location defaults if available
        if (store.manualResolvedName.isNotEmpty()) {
            _currentRegionName.value = store.manualResolvedName
            _currentLatitude.value = store.manualLatitude
            _currentLongitude.value = store.manualLongitude
        }

        updatePermissionStatus()
    }

    /** Debug APK 전용 Matchup 결정론적 시작 상태. 일반 실행에는 영향이 없다. */
    fun applyMatchupState(state: String?) {
        freezeMatchupLoadingAnimation = shouldFreezeMatchupLoadingAnimation(state, BuildConfig.DEBUG)
        if (!BuildConfig.DEBUG || state.isNullOrBlank()) return
        _matchupRegionSearchFocused.value = state == "region-search"

        val restaurants = listOf(
            matchupRestaurant("matchup-1", "바람난김밥카페", "음식점 > 분식", 320, "김밥"),
            matchupRestaurant("matchup-2", "설월식당", "음식점 > 한식", 480, "한식"),
            matchupRestaurant("matchup-3", "동백벌", "음식점 > 한식", 640, "불고기"),
            matchupRestaurant("matchup-4", "다미정", "음식점 > 냉면", 790, "냉면"),
            matchupRestaurant("matchup-5", "백암토종순대국", "음식점 > 순대", 910, "순대")
        )
        val main = restaurants.first()
        val decision = Decision("김밥", main)
        val fixtureDate = ZonedDateTime.of(2026, 8, 24, 13, 54, 0, 0, ZoneId.of("Asia/Seoul"))
            .toInstant()
            .toEpochMilli()
        val record = ChoiceRecord(
            menu = decision.menu,
            restaurantName = main.name,
            date = fixtureDate,
            region = "경기도 광주시 초월읍",
            category = main.category,
            restaurantId = main.id,
            lat = main.lat,
            lng = main.lng,
            address = main.address
        )
        val favorite = FavoriteRecord(
            restaurantId = main.id,
            restaurantName = main.name,
            category = main.category,
            region = "경기도 광주시 초월읍",
            date = fixtureDate,
            lat = main.lat,
            lng = main.lng,
            address = main.address,
            curatedMenus = main.curatedMenus
        )

        _currentRegionName.value = "경기도 광주시 초월읍"
        _currentLatitude.value = main.lat
        _currentLongitude.value = main.lng
        _regionStatusText.value = "현 위치를 확인했어요"
        _nearbyRegions.value = listOf(
            NearbyRegion("초월읍", 37.4, 127.3),
            NearbyRegion("곤지암읍", 37.35, 127.33),
            NearbyRegion("경안동", 37.41, 127.25)
        )
        _frequentRegions.value = emptyList()

        when (state) {
            "home" -> _currentPage.value = AppPage.HOME
            "region", "region-search" -> _currentPage.value = AppPage.REGION
            "loading" -> {
                _currentPage.value = AppPage.RESULT
                _recommendationPhase.value = RecommendationPhase.LOADING
            }
            "results" -> {
                _currentPage.value = AppPage.RESULT
                _mainRestaurant.value = main
                _carouselRestaurants.value = restaurants.drop(1)
                _recommendationPhase.value = RecommendationPhase.SUCCESS
            }
            "decision", "decision-recorded" -> {
                _currentPage.value = AppPage.DECISION
                _currentDecision.value = decision
                _isCurrentDecisionRecorded.value = state == "decision-recorded"
            }
            "history-empty" -> {
                _currentPage.value = AppPage.HISTORY
                _choiceRecords.value = emptyList()
            }
            "history-populated" -> {
                _currentPage.value = AppPage.HISTORY
                _choiceRecords.value = listOf(record)
            }
            "favorites-empty" -> {
                _currentPage.value = AppPage.FAVORITES
                _favoriteRecords.value = emptyList()
            }
            "favorites-populated" -> {
                _currentPage.value = AppPage.FAVORITES
                _favoriteRecords.value = listOf(favorite)
            }
            "settings" -> {
                pageBeforeSettings = AppPage.HOME
                _currentPage.value = AppPage.PROFILE
            }
        }
    }

    private fun matchupRestaurant(
        id: String,
        name: String,
        category: String,
        distance: Int,
        menu: String
    ) = Restaurant(
        id = id,
        name = name,
        category = category,
        lat = 37.4,
        lng = 127.3,
        distanceMeters = distance,
        address = "경기도 광주시 초월읍",
        curatedMenus = listOf(menu)
    )

    fun updatePermissionStatus() {
        _locationPermissionStatus.value = if (locationService.hasLocationPermission()) {
            "사용 중"
        } else {
            "꺼짐"
        }
    }

    fun onLocationPermissionResult() {
        if (pendingAutoRecommendationAfterPermission) {
            pendingAutoRecommendationAfterPermission = false
            if (locationService.hasLocationPermission()) {
                executeAutoRecommendation()
            } else {
                _recommendationPhase.value = RecommendationPhase.LOCATION_DENIED
            }
        }
    }

    fun onNotificationPermissionResult(granted: Boolean, context: Context) {
        val target = pendingNotifyEnableContext ?: context
        pendingNotifyEnableContext = null
        if (granted) {
            store.lunchNotifyEnabled = true
            _lunchNotifyEnabled.value = true
            NotificationHelper.scheduleDailyAlarm(target)
        } else {
            store.lunchNotifyEnabled = false
            _lunchNotifyEnabled.value = false
            _showNotificationDeniedAlert.value = true
        }
    }

    // --- Navigation ---

    fun navigateTo(page: AppPage) {
        if (page == AppPage.RESULT) {
            _currentDecision.value = null
            _isCurrentDecisionRecorded.value = false
            startRecommendationForCurrentMode()
        }
        _currentPage.value = page
    }

    fun openSettings() {
        if (_currentPage.value != AppPage.PROFILE) {
            pageBeforeSettings = _currentPage.value
            _currentPage.value = AppPage.PROFILE
        }
    }

    fun closeSettings() {
        _currentPage.value = pageBeforeSettings
    }

    fun openDecision(restaurant: Restaurant) {
        val menu = MenuPolicy.resolveMenu(restaurant)
        _currentDecision.value = Decision(menu, restaurant)
        _isCurrentDecisionRecorded.value = false
        _currentPage.value = AppPage.DECISION
    }

    fun closeDecision() {
        _currentPage.value = AppPage.RESULT
    }

    fun handleSystemBack(): Boolean {
        return when (_currentPage.value) {
            AppPage.PROFILE -> {
                closeSettings()
                true
            }
            AppPage.DECISION -> {
                closeDecision()
                true
            }
            AppPage.REGION -> {
                _currentPage.value = AppPage.HOME
                true
            }
            AppPage.RESULT -> {
                _currentPage.value = AppPage.HOME
                true
            }
            AppPage.HISTORY, AppPage.FAVORITES -> {
                _currentPage.value = AppPage.HOME
                true
            }
            AppPage.HOME -> false
        }
    }

    // --- Location & Recommendation Flows ---

    fun setLocationMode(mode: LocationMode) {
        _locationMode.value = mode
    }

    fun startAutoRecommendation() {
        _locationMode.value = LocationMode.AUTO
        _currentPage.value = AppPage.RESULT
        executeAutoRecommendation()
    }

    fun startManualRecommendation(lat: Double, lng: Double, regionName: String) {
        _locationMode.value = LocationMode.MANUAL
        _currentLatitude.value = lat
        _currentLongitude.value = lng
        _currentRegionName.value = regionName
        store.manualLatitude = lat
        store.manualLongitude = lng
        store.manualResolvedName = regionName
        _currentPage.value = AppPage.RESULT
        executeFetchRestaurants(lat, lng, regionName)
    }

    fun startRecommendationForCurrentMode() {
        if (_locationMode.value == LocationMode.AUTO) {
            executeAutoRecommendation()
        } else {
            val lat = _currentLatitude.value
            val lng = _currentLongitude.value
            val name = _currentRegionName.value
            if (lat != null && lng != null) {
                executeFetchRestaurants(lat, lng, name)
            } else {
                executeAutoRecommendation()
            }
        }
    }

    private fun executeAutoRecommendation() {
        val requestToken = System.currentTimeMillis()
        activeRequestToken = requestToken
        _recommendationPhase.value = RecommendationPhase.LOADING
        _currentRegionName.value = "현 위치"
        startLoadingTimer()

        viewModelScope.launch {
            if (!locationService.hasLocationPermission()) {
                if (requestLocationPermission != null) {
                    pendingAutoRecommendationAfterPermission = true
                    stopLoadingTimer()
                    requestLocationPermission?.invoke()
                } else if (activeRequestToken == requestToken) {
                    _recommendationPhase.value = RecommendationPhase.LOCATION_DENIED
                    stopLoadingTimer()
                }
                return@launch
            }

            val loc = locationService.getCurrentLocation()
            if (loc == null) {
                if (activeRequestToken == requestToken) {
                    _recommendationPhase.value = RecommendationPhase.ERROR
                    _recommendationError.value = "현재 위치를 확인하지 못했습니다. 잠시 후 다시 시도해 주세요."
                    stopLoadingTimer()
                }
                return@launch
            }

            _currentLatitude.value = loc.lat
            _currentLongitude.value = loc.lng
            _currentRegionName.value = loc.resolvedName
            executeFetchRestaurants(loc.lat, loc.lng, loc.resolvedName, requestToken)
        }
    }

    private fun executeFetchRestaurants(
        lat: Double,
        lng: Double,
        regionName: String,
        existingToken: Long? = null
    ) {
        val requestToken = existingToken ?: System.currentTimeMillis().also {
            activeRequestToken = it
            _recommendationPhase.value = RecommendationPhase.LOADING
            startLoadingTimer()
        }

        viewModelScope.launch {
            try {
                val response = ApiClient.fetchRestaurants(lat, lng)
                if (activeRequestToken != requestToken) return@launch

                // Filter out isOpenNow == false (keep true and null), pool up to 13
                val pooled = com.nasfinder.whattoeat.data.RecommendationPool.buildPool(response.restaurants)

                if (pooled.isEmpty()) {
                    _recommendationPhase.value = RecommendationPhase.EMPTY
                    stopLoadingTimer()
                    return@launch
                }

                val pool = pooled.shuffled()
                val main = pool.first()
                val remaining = if (pool.size > 1) pool.subList(1, pool.size) else emptyList()

                _mainRestaurant.value = main
                _carouselRestaurants.value = remaining
                _recommendationPhase.value = RecommendationPhase.SUCCESS
                stopLoadingTimer()

                if (pool.any { it.photoUrl == null }) {
                    refreshRestaurantPhotos(lat, lng, requestToken)
                }

                // Save usage history
                val topMenu = MenuPolicy.resolveMenu(main)
                store.lastTopMenu = topMenu
                store.recordRegionUsage(regionName, lat, lng)
                _frequentRegions.value = store.getTopFrequentRegions(3)

            } catch (e: ApiException) {
                if (activeRequestToken == requestToken) {
                    _recommendationPhase.value = RecommendationPhase.ERROR
                    _recommendationError.value = e.message
                    stopLoadingTimer()
                }
            } catch (e: Exception) {
                if (activeRequestToken == requestToken) {
                    _recommendationPhase.value = RecommendationPhase.ERROR
                    _recommendationError.value = "서버에 연결하지 못했습니다. 네트워크 상태를 확인해 주세요."
                    stopLoadingTimer()
                }
            }
        }
    }

    private suspend fun refreshRestaurantPhotos(lat: Double, lng: Double, requestToken: Long) {
        for (waitMillis in PhotoRefreshPolicy.retryDelaysMillis) {
            delay(waitMillis)
            if (activeRequestToken != requestToken) return
            val current = listOfNotNull(_mainRestaurant.value) + _carouselRestaurants.value
            if (current.none { it.photoUrl == null }) return

            val response = try {
                ApiClient.fetchRestaurants(lat, lng)
            } catch (_: Exception) {
                continue
            }
            if (activeRequestToken != requestToken) return
            val refreshedById = response.restaurants.associateBy { it.id }
            val refreshed = PhotoRefreshPolicy.mergePreservingOrder(current, response.restaurants)
            if (refreshed == current) continue
            _mainRestaurant.value = refreshed.firstOrNull()
            _carouselRestaurants.value = refreshed.drop(1)

            val currentDecision = _currentDecision.value
            val latestDecisionRestaurant = currentDecision?.restaurant?.id?.let(refreshedById::get)
            if (currentDecision != null && latestDecisionRestaurant?.photoUrl != null) {
                _currentDecision.value = Decision(currentDecision.menu, latestDecisionRestaurant)
            }
        }
    }

    private fun startLoadingTimer() {
        loadingTimerJob?.cancel()
        _loadingSeconds.value = 0
        loadingTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _loadingSeconds.value += 1
            }
        }
    }

    private fun stopLoadingTimer() {
        loadingTimerJob?.cancel()
        loadingTimerJob = null
        _loadingSeconds.value = 0
    }

    fun retryRecommendation() {
        startRecommendationForCurrentMode()
    }

    // --- Region Screen Actions ---

    fun refreshLocationInRegionScreen() {
        viewModelScope.launch {
            _isResolvingLocation.value = true
            _regionStatusText.value = "현 위치 확인 중…"
            if (!locationService.hasLocationPermission()) {
                _regionStatusText.value = "위치 권한이 꺼져 있어요"
                _isResolvingLocation.value = false
                return@launch
            }

            val loc = locationService.getCurrentLocation()
            if (loc != null) {
                _regionStatusText.value = if (loc.resolvedName.isNotEmpty() && loc.resolvedName != "현 위치") {
                    "현 위치: ${loc.resolvedName}"
                } else {
                    "현 위치를 확인했어요"
                }
                val nearby = locationService.resolveNearbyRegions(loc.lat, loc.lng)
                _nearbyRegions.value = nearby
            } else {
                _regionStatusText.value = "위치 권한이 꺼져 있어요"
            }
            _isResolvingLocation.value = false
        }
    }

    fun searchRegion(query: String, onComplete: (success: Boolean, errorMsg: String?) -> Unit) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        _locationMode.value = LocationMode.MANUAL
        _currentPage.value = AppPage.RESULT
        _recommendationPhase.value = RecommendationPhase.LOADING

        viewModelScope.launch {
            try {
                val result = locationService.searchLocation(trimmed)
                if (result != null) {
                    startManualRecommendation(result.lat, result.lng, result.resolvedName)
                    onComplete(true, null)
                } else {
                    _recommendationPhase.value = RecommendationPhase.ERROR
                    _recommendationError.value = "‘$trimmed’ 지역을 찾지 못했어요. ‘서울 강남’처럼 시·구 단위로 입력해 보세요."
                    onComplete(
                        false,
                        "‘$trimmed’ 지역을 찾지 못했어요. ‘서울 강남’처럼 시·구 단위로 입력해 보세요."
                    )
                }
            } catch (e: Exception) {
                _recommendationPhase.value = RecommendationPhase.ERROR
                _recommendationError.value = "지역 검색에 실패했어요. 네트워크 상태를 확인한 뒤 다시 시도해 주세요."
                onComplete(
                    false,
                    "지역 검색에 실패했어요. 네트워크 상태를 확인한 뒤 다시 시도해 주세요."
                )
            }
        }
    }

    // --- Decision & Recording ---

    fun recordCurrentDecision(context: Context) {
        val decision = _currentDecision.value ?: return
        val restaurant = decision.restaurant

        val record = ChoiceRecord(
            menu = decision.menu,
            restaurantName = restaurant.name,
            date = System.currentTimeMillis(),
            region = _currentRegionName.value.ifEmpty { restaurant.address },
            imageUrl = restaurant.photoUrl,
            category = restaurant.category,
            photoKind = restaurant.photoKind,
            photoProvider = restaurant.photoProvider,
            photoSourceUrl = restaurant.photoSourceUrl,
            photoAttribution = restaurant.photoAttribution,
            photoCreator = restaurant.photoCreator,
            photoCreatorUrl = restaurant.photoCreatorUrl,
            photoLicense = restaurant.photoLicense,
            photoLicenseUrl = restaurant.photoLicenseUrl,
            photoTitle = restaurant.photoTitle,
            restaurantId = restaurant.id,
            lat = restaurant.lat,
            lng = restaurant.lng,
            address = restaurant.address ?: restaurant.roadAddress
        )
        store.addChoiceRecord(record)
        _choiceRecords.value = store.getChoiceRecords()
        _isCurrentDecisionRecorded.value = true

        openMapForRestaurant(context, restaurant)
    }

    fun openMapForRestaurant(context: Context, restaurant: Restaurant) {
        val provider = _selectedMapProvider.value
        val success = MapProviderHelper.openMap(context, provider, restaurant)
        if (!success) {
            _missingMapProvider.value = provider
            _showMissingMapAlert.value = true
        }
    }

    fun dismissMissingMapAlert() {
        _showMissingMapAlert.value = false
        _missingMapProvider.value = null
    }

    fun openOtherMapPicker() {
        _showMissingMapAlert.value = false
        _showOtherMapPicker.value = true
    }

    fun dismissOtherMapPicker() {
        _showOtherMapPicker.value = false
    }

    fun installMissingMap(context: Context) {
        val provider = _missingMapProvider.value ?: return
        MapProviderHelper.openPlayStore(context, provider)
        dismissMissingMapAlert()
    }

    fun selectOtherMapProviderAndOpen(context: Context, newProvider: MapProvider) {
        _showMissingMapAlert.value = false
        _showOtherMapPicker.value = false
        setMapProvider(newProvider)
        _currentDecision.value?.restaurant?.let {
            MapProviderHelper.openMap(context, newProvider, it)
        }
    }

    // --- Favorites ---

    fun toggleFavorite(restaurant: Restaurant): Boolean {
        val favRecord = FavoriteRecord(
            restaurantId = restaurant.id,
            restaurantName = restaurant.name,
            category = restaurant.category,
            region = _currentRegionName.value.ifEmpty { restaurant.address },
            imageUrl = restaurant.photoUrl,
            photoKind = restaurant.photoKind,
            photoProvider = restaurant.photoProvider,
            photoSourceUrl = restaurant.photoSourceUrl,
            photoAttribution = restaurant.photoAttribution,
            photoCreator = restaurant.photoCreator,
            photoCreatorUrl = restaurant.photoCreatorUrl,
            photoLicense = restaurant.photoLicense,
            photoLicenseUrl = restaurant.photoLicenseUrl,
            photoTitle = restaurant.photoTitle,
            date = System.currentTimeMillis(),
            lat = restaurant.lat,
            lng = restaurant.lng,
            address = restaurant.address ?: restaurant.roadAddress,
            curatedMenus = restaurant.curatedMenus,
            phone = restaurant.phone,
            placeUrl = restaurant.placeUrl
        )
        val isFav = store.toggleFavorite(favRecord)
        _favoriteRecords.value = store.getFavoriteRecords()
        return isFav
    }

    fun isFavorite(restaurantId: String): Boolean {
        return store.isFavorite(restaurantId)
    }

    fun deleteChoiceRecord(record: ChoiceRecord) {
        store.deleteChoiceRecord(record)
        _choiceRecords.value = store.getChoiceRecords()
    }

    fun deleteFavorite(restaurantId: String) {
        store.deleteFavorite(restaurantId)
        _favoriteRecords.value = store.getFavoriteRecords()
    }

    // --- Photo Sheet & Alerts ---

    fun showPhotoInformation(information: PhotoInformation) {
        _selectedPhotoInformation.value = information
        _showPhotoSheet.value = true
    }

    fun dismissPhotoInformation() {
        _showPhotoSheet.value = false
        _selectedPhotoInformation.value = null
    }

    fun showBusinessInfo() {
        _showBusinessInfoAlert.value = true
    }

    fun dismissBusinessInfo() {
        _showBusinessInfoAlert.value = false
    }

    // --- Settings Updates ---

    fun setMapProvider(provider: MapProvider) {
        store.mapProvider = provider
        _selectedMapProvider.value = provider
    }

    fun setLunchNotifyEnabled(enabled: Boolean, context: Context) {
        if (enabled) {
            pendingNotifyEnableContext = context
            if (requestNotificationPermission != null) {
                requestNotificationPermission?.invoke()
            } else {
                store.lunchNotifyEnabled = true
                _lunchNotifyEnabled.value = true
                NotificationHelper.scheduleDailyAlarm(context)
            }
        } else {
            store.lunchNotifyEnabled = false
            _lunchNotifyEnabled.value = false
            NotificationHelper.cancelDailyAlarm(context)
        }
    }

    fun setLunchTime(hour: Int, minute: Int, context: Context) {
        store.lunchHour = hour
        store.lunchMinute = minute
        _lunchHour.value = hour
        _lunchMinute.value = minute
        if (_lunchNotifyEnabled.value) {
            NotificationHelper.scheduleDailyAlarm(context)
        }
    }

    fun setLunchLeadTime(leadTime: ReminderLeadTime, context: Context) {
        store.lunchLeadMinutes = leadTime.leadMinutes
        _lunchLeadTime.value = leadTime
        if (_lunchNotifyEnabled.value) {
            NotificationHelper.scheduleDailyAlarm(context)
        }
    }

    fun toggleCopyrightExpanded() {
        _isCopyrightExpanded.value = !_isCopyrightExpanded.value
    }

    fun dismissNotificationDeniedAlert() {
        _showNotificationDeniedAlert.value = false
    }

    // --- Debug Fixtures Loader (for Acceptance Criteria #9 & Preview Testing) ---
    fun loadDebugFixture(
        page: AppPage,
        phase: RecommendationPhase = RecommendationPhase.SUCCESS,
        mainRest: Restaurant? = null,
        carouselList: List<Restaurant> = emptyList(),
        choices: List<ChoiceRecord> = emptyList(),
        favorites: List<FavoriteRecord> = emptyList()
    ) {
        _currentPage.value = page
        _recommendationPhase.value = phase
        if (mainRest != null) _mainRestaurant.value = mainRest
        if (carouselList.isNotEmpty()) _carouselRestaurants.value = carouselList
        if (choices.isNotEmpty()) _choiceRecords.value = choices
        if (favorites.isNotEmpty()) _favoriteRecords.value = favorites
    }
}
