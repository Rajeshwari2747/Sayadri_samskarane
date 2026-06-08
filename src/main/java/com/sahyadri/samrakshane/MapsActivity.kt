package com.sahyadri.samrakshane

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sahyadri.samrakshane.databinding.ActivityMapsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var googleMap: GoogleMap
    private val repository by lazy { AlertRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.map_title)

        binding.root.alpha = 0f
        binding.root.translationY = 18f
        binding.root.animate().alpha(1f).translationY(0f).setDuration(280).start()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnRefreshMap.setOnClickListener {
            refreshMarkers()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finishWithSlide()
        return true
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(15.0, 75.5), 6.2f))
        refreshMarkers()
    }

    private fun refreshMarkers() {
        if (!::googleMap.isInitialized) return

        googleMap.clear()
        repository.loadAllAlerts(onSuccess = { alerts ->
            if (alerts.isEmpty()) {
                binding.tvMapEmpty.visibility = View.VISIBLE
                return@loadAllAlerts
            }

            binding.tvMapEmpty.visibility = View.GONE
            val builder = com.google.android.gms.maps.model.LatLngBounds.Builder()
            alerts.forEach { alert ->
                val position = LatLng(alert.latitude, alert.longitude)
                val markerColor = when (alert.status) {
                    "Verified" -> BitmapDescriptorFactory.HUE_BLUE
                    "Team Dispatched" -> BitmapDescriptorFactory.HUE_GREEN
                    "Resolved" -> BitmapDescriptorFactory.HUE_AZURE
                    else -> BitmapDescriptorFactory.HUE_ORANGE
                }
                val time = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(alert.timestamp))
                googleMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(alert.alertType)
                        .snippet("${alert.status} • $time")
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                )
                builder.include(position)
            }

            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 120))
            } catch (_: Exception) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(15.0, 75.5), 6.2f))
            }
        })
    }
}

