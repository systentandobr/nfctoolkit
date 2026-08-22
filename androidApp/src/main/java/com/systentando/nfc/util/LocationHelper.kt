package com.systentando.nfc.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

data class GeocodeResult(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): GeocodeResult? {
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                val address = reverseGeocode(it.latitude, it.longitude)
                GeocodeResult(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    address = address
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun geocodeAddress(address: String): GeocodeResult? {
        if (!Geocoder.isPresent()) {
            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(address, 1)

                if (addresses.isNullOrEmpty()) {
                    null
                } else {
                    val result = addresses[0]
                    GeocodeResult(
                        latitude = result.latitude,
                        longitude = result.longitude,
                        address = result.getAddressLine(0)
                    )
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun reverseGeocode(latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) {
            return null
        }

        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.firstOrNull()?.getAddressLine(0)
        } catch (e: Exception) {
            null
        }
    }
}
