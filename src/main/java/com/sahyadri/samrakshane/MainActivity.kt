package com.sahyadri.samrakshane

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.sahyadri.samrakshane.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val repository by lazy { AlertRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playEntranceAnimation()
        loadDashboard()

        binding.btnReportAlert.setOnClickListener {
            launchWithSlide(Intent(this, ReportAlertActivity::class.java))
        }
        binding.btnMyReports.setOnClickListener {
            launchWithSlide(Intent(this, MyReportsActivity::class.java))
        }
        binding.btnEducation.setOnClickListener {
            launchWithSlide(Intent(this, EducationActivity::class.java))
        }
        binding.btnWildlifeMap.setOnClickListener {
            launchWithSlide(Intent(this, MapsActivity::class.java))
        }
        binding.helplineCard.setOnClickListener { dialForestHelpline() }
        binding.btnCallHelpline.setOnClickListener { dialForestHelpline() }
    }

    private fun dialForestHelpline() {
        val number = getString(R.string.forest_helpline_number)
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open dialer right now.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDashboard() {
        repository.loadStats(onSuccess = { total, active ->
            binding.tvTotalReports.text = total.toString()
            binding.tvActiveReports.text = active.toString()
        })
        repository.loadRecentAlerts(3, onSuccess = { alerts ->
            renderRecentAlerts(alerts)
        })
    }

    private fun renderRecentAlerts(alerts: List<AlertEntity>) {
        val cards = listOf(
            Triple(binding.alertCard1, binding.tvAlert1Type, binding.tvAlert1Status to binding.tvAlert1Time),
            Triple(binding.alertCard2, binding.tvAlert2Type, binding.tvAlert2Status to binding.tvAlert2Time),
            Triple(binding.alertCard3, binding.tvAlert3Type, binding.tvAlert3Status to binding.tvAlert3Time)
        )

        cards.forEach { it.first.visibility = View.GONE }

        if (alerts.isEmpty()) {
            binding.tvNoAlerts.visibility = View.VISIBLE
            return
        }

        binding.tvNoAlerts.visibility = View.GONE
        alerts.forEachIndexed { index, alert ->
            if (index < cards.size) {
                val (card, tvType, statusTime) = cards[index]
                val (tvStatus, tvTime) = statusTime
                card.visibility = View.VISIBLE
                card.alpha = 0f
                card.translationY = 24f
                card.animate().alpha(1f).translationY(0f).setDuration(240).setInterpolator(DecelerateInterpolator()).start()

                tvType.text = alert.alertType
                tvStatus.text = "Status: ${alert.status}"
                tvStatus.setTextColor(
                    when (alert.status) {
                        "Verified" -> 0xFF1565C0.toInt()
                        "Team Dispatched" -> 0xFF2E7D32.toInt()
                        "Resolved" -> 0xFF1B5E20.toInt()
                        else -> 0xFFFF6F00.toInt()
                    }
                )
                tvTime.text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(alert.timestamp))
            }
        }
    }

    private fun playEntranceAnimation() {
        binding.root.alpha = 0f
        binding.root.translationY = 18f
        binding.root.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(280)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
    }
}