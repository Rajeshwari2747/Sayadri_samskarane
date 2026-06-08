package com.sahyadri.samrakshane

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlertAdapter(
    private val alerts: List<AlertEntity>,
    private val onItemClick: ((AlertEntity) -> Unit)? = null
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAlertType: TextView = itemView.findViewById(R.id.tvAlertType)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.tvAlertType.text = alert.alertType
        holder.tvStatus.text = "Status: ${alert.status}"
        holder.tvLocation.text = "Lat: ${String.format("%.4f", alert.latitude)}, Lng: ${String.format("%.4f", alert.longitude)}"
        val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(alert.timestamp))
        holder.tvTimestamp.text = date

        val statusColor = when (alert.status) {
            "Reported" -> 0xFFFF6F00.toInt()
            "Verified" -> 0xFF1565C0.toInt()
            "Team Dispatched" -> 0xFF6A1B9A.toInt()
            "Resolved" -> 0xFF2E7D32.toInt()
            else -> 0xFF757575.toInt()
        }
        holder.tvStatus.setTextColor(statusColor)

        onItemClick?.let { click ->
            holder.itemView.setOnClickListener { click(alert) }
        }
    }

    override fun getItemCount() = alerts.size
}