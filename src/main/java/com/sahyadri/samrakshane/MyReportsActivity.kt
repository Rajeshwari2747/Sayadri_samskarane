package com.sahyadri.samrakshane

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sahyadri.samrakshane.databinding.ActivityMyReportsBinding

class MyReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyReportsBinding
    private val alertList = mutableListOf<AlertEntity>()
    private lateinit var adapter: AlertAdapter
    private val repository by lazy { AlertRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Reports"

        adapter = AlertAdapter(alertList) { alert ->
            showStatusDialog(alert)
        }
        binding.rvReports.layoutManager = LinearLayoutManager(this)
        binding.rvReports.adapter = adapter

        binding.root.alpha = 0f
        binding.root.translationY = 18f
        binding.root.animate().alpha(1f).translationY(0f).setDuration(280).setInterpolator(DecelerateInterpolator()).start()

        loadReports()
    }

    override fun onSupportNavigateUp(): Boolean {
        finishWithSlide()
        return true
    }

    private fun showStatusDialog(alert: AlertEntity) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_status_tracker)
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        val statuses = listOf("Reported", "Verified", "Team Dispatched", "Resolved")
        val currentIndex = statuses.indexOf(alert.status)

        val stepViews = listOf(
            dialog.findViewById<TextView>(R.id.tvStep1),
            dialog.findViewById<TextView>(R.id.tvStep2),
            dialog.findViewById<TextView>(R.id.tvStep3),
            dialog.findViewById<TextView>(R.id.tvStep4)
        )

        stepViews.forEachIndexed { index, tv ->
            if (index <= currentIndex) {
                tv.setBackgroundColor(0xFF2D6A4F.toInt())
                tv.setTextColor(0xFFFFFFFF.toInt())
            } else {
                tv.setBackgroundColor(0xFFE0E0E0.toInt())
                tv.setTextColor(0xFF757575.toInt())
            }
            // No click listener — view only for users
        }

        dialog.findViewById<TextView>(R.id.tvDialogTitle).text = "📍 ${alert.alertType}"

        // Show note that only admin can update
        dialog.findViewById<TextView>(R.id.tvDismiss).text = "Status is updated by Forest Department"

        dialog.findViewById<TextView>(R.id.tvDismiss).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadReports() {
        repository.loadAllAlerts(onSuccess = { alerts ->
            alertList.clear()
            alertList.addAll(alerts)
            adapter.notifyDataSetChanged()

            if (alertList.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvReports.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvReports.visibility = View.VISIBLE
            }
        })
    }
}