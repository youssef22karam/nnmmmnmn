package com.azkari.wasalati

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_OPEN_TAB = "open_tab"
    }

    private val viewModel: FaithfulMainViewModel by viewModels()

    private var notificationPermissionGranted by mutableStateOf(false)
    private var exactAlarmPermissionGranted by mutableStateOf(false)
    private lateinit var connectivityManager: ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            syncConnectivityState()
        }

        override fun onLost(network: Network) {
            syncConnectivityState()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            syncConnectivityState()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        syncPermissionState()
        if (notificationPermissionGranted) {
            PrayerScheduler.scheduleAll(applicationContext)
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            requestCurrentLocation()
        } else {
            Toast.makeText(this, "اسمح بالموقع لاستخدام مدينتك الحالية.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectivityManager = getSystemService(ConnectivityManager::class.java)
        NotificationHelper.createChannels(this)
        syncPermissionState()
        syncConnectivityState()
        registerConnectivityCallback()

        setContent {
            AzkariFaithfulApp(
                viewModel = viewModel,
                notificationPermissionGranted = notificationPermissionGranted,
                exactAlarmPermissionGranted = exactAlarmPermissionGranted,
                onRequestNotifications = ::requestNotificationPermission,
                onRequestExactAlarms = ::openExactAlarmSettings,
                onUseCurrentLocation = ::promptForCurrentLocation,
            )
        }

        routeIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        routeIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        syncPermissionState()
        syncConnectivityState()
        viewModel.refreshTick()
        PrayerScheduler.scheduleAll(applicationContext)
    }

    override fun onDestroy() {
        runCatching {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
        super.onDestroy()
    }

    private fun routeIntent(intent: Intent?) {
        viewModel.handleOpenTab(intent?.getStringExtra(EXTRA_OPEN_TAB))
    }

    private fun syncPermissionState() {
        notificationPermissionGranted = NotificationHelper.canPostNotifications(this)
        exactAlarmPermissionGranted = PrayerScheduler.canScheduleExactAlarms(this)
    }

    private fun registerConnectivityCallback() {
        runCatching {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }
    }

    private fun syncConnectivityState() {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                )
        viewModel.onConnectivityChanged(isOffline = !isConnected)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            syncPermissionState()
            PrayerScheduler.scheduleAll(applicationContext)
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            syncPermissionState()
            PrayerScheduler.scheduleAll(applicationContext)
            return
        }

        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            syncPermissionState()
            return
        }

        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:$packageName")
        }

        runCatching { startActivity(intent) }
            .onFailure {
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:$packageName"),
                    ),
                )
            }
    }

    private fun promptForCurrentLocation() {
        if (hasLocationPermission()) {
            requestCurrentLocation()
            return
        }

        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
        )
    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation() {
        val locationManager = getSystemService(LocationManager::class.java)
        if (locationManager == null) {
            Toast.makeText(this, "تعذر الوصول إلى خدمة الموقع.", Toast.LENGTH_SHORT).show()
            return
        }

        val providers = locationManager.getProviders(true)
        val preferredProvider = when {
            providers.contains(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            providers.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            providers.isNotEmpty() -> providers.first()
            else -> null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && preferredProvider != null) {
            locationManager.getCurrentLocation(preferredProvider, null, mainExecutor) { location ->
                if (location != null) {
                    applyLocation(location)
                } else {
                    useLastKnownLocation(locationManager)
                }
            }
            return
        }

        if (preferredProvider != null) {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    applyLocation(location)
                }

                override fun onProviderEnabled(provider: String) = Unit

                override fun onProviderDisabled(provider: String) = Unit

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            }
            runCatching {
                locationManager.requestSingleUpdate(preferredProvider, listener, Looper.getMainLooper())
            }.onFailure {
                useLastKnownLocation(locationManager)
            }
            return
        }

        useLastKnownLocation(locationManager)
    }

    @SuppressLint("MissingPermission")
    private fun useLastKnownLocation(locationManager: LocationManager) {
        val bestLocation = locationManager.getProviders(true)
            .mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxByOrNull { it.time }

        if (bestLocation != null) {
            applyLocation(bestLocation)
        } else {
            Toast.makeText(this, "شغّل خدمات الموقع ثم حاول مرة أخرى.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyLocation(location: Location) {
        viewModel.setCurrentLocation(location.latitude, location.longitude)
        Toast.makeText(this, "تم تحديث الموقع الحالي.", Toast.LENGTH_SHORT).show()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
    }
}
