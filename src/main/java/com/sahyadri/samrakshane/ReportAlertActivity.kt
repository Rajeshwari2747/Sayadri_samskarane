package com.sahyadri.samrakshane

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import android.view.animation.DecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sahyadri.samrakshane.databinding.ActivityReportAlertBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportAlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportAlertBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedAlertType: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var photoUri: Uri? = null
    private val repository by lazy { AlertRepository(this) }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
            binding.imgPreview.setImageURI(uri)
            Toast.makeText(this, "Photo selected! ✅", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            binding.imgPreview.setImageURI(photoUri)
            Toast.makeText(this, "Photo captured! ✅", Toast.LENGTH_SHORT).show()
        } else {
            pickImageLauncher.launch("image/*")
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else pickImageLauncher.launch("image/*")
    }

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) getLocation()
        else Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Report Alert"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.root.alpha = 0f
        binding.root.translationY = 20f
        binding.root.animate().alpha(1f).translationY(0f).setDuration(280).setInterpolator(DecelerateInterpolator()).start()

        binding.btnFire.setOnClickListener {
            selectedAlertType = "Forest Fire"
            binding.tvSelectedType.text = "Selected: 🔥 Forest Fire"
            binding.etOtherIssue.visibility = View.GONE
            binding.btnSubmitReport.visibility = View.VISIBLE
        }

        binding.btnLandslide.setOnClickListener {
            selectedAlertType = "Landslide"
            binding.tvSelectedType.text = "Selected: ⛰ Landslide"
            binding.etOtherIssue.visibility = View.GONE
            binding.btnSubmitReport.visibility = View.VISIBLE
        }

        binding.btnLogging.setOnClickListener {
            selectedAlertType = "Illegal Logging"
            binding.tvSelectedType.text = "Selected: 🪓 Illegal Logging"
            binding.etOtherIssue.visibility = View.GONE
            binding.btnSubmitReport.visibility = View.VISIBLE
        }

        binding.btnWildlife.setOnClickListener {
            selectedAlertType = "Wildlife Sighting"
            binding.tvSelectedType.text = "Selected: 🐯 Wildlife Sighting"
            binding.etOtherIssue.visibility = View.GONE
            binding.btnSubmitReport.visibility = View.VISIBLE
        }

        binding.btnOther.setOnClickListener {
            selectedAlertType = "Other"
            binding.tvSelectedType.text = "Selected: ⚠️ Other Issue"
            binding.etOtherIssue.visibility = View.VISIBLE
            binding.etOtherIssue.requestFocus()
            binding.btnSubmitReport.visibility = View.VISIBLE
        }

        getLocation()

        binding.btnTakePhoto.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnSubmitReport.setOnClickListener {
            if (selectedAlertType.isEmpty()) {
                Toast.makeText(this, "Please select an alert type!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedAlertType == "Other") {
                val otherText = binding.etOtherIssue.text.toString().trim()
                if (otherText.isEmpty()) {
                    Toast.makeText(this, "Please describe the issue!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                selectedAlertType = "Other: $otherText"
            }
            submitReport()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finishWithSlide()
        return true
    }

    private fun submitReport() {
        val finalAlertType = if (selectedAlertType == "Other") {
            val otherText = binding.etOtherIssue.text.toString().trim()
            "Other: $otherText"
        } else {
            selectedAlertType
        }

        binding.btnSubmitReport.isEnabled = false
        repository.saveReport(
            alertType = finalAlertType,
            latitude = latitude,
            longitude = longitude,
            onSuccess = { reportId ->
                Toast.makeText(this, "Report saved locally! ✅ ID: ${reportId.takeLast(6)}", Toast.LENGTH_LONG).show()
                finishWithSlide()
            },
            onError = { error ->
                binding.btnSubmitReport.isEnabled = true
                Toast.makeText(this, "Failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun launchCamera() {
        try {
            val photoFile = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "alert_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
            )
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
            photoUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                binding.tvGpsCoordinates.text =
                    "Lat: ${String.format("%.6f", latitude)}\nLng: ${String.format("%.6f", longitude)}"
            } else {
                binding.tvGpsCoordinates.text = "Location not available.\nEnable GPS and try again."
            }
        }
    }
}