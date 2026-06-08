package com.sahyadri.samrakshane

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AlertRepository(context: Context) {

    private val appContext = context.applicationContext
    private val database = AppDatabase.getInstance(appContext)
    private val dao = database.alertDao()
    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun saveReport(
        alertType: String,
        latitude: Double,
        longitude: Double,
        status: String = "Reported",
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        ioExecutor.execute {
            try {
                val id = UUID.randomUUID().toString()
                val alert = AlertEntity(id, alertType, latitude, longitude, System.currentTimeMillis(), status)
                dao.upsert(alert)
                mainHandler.post { onSuccess(id) }
            } catch (t: Throwable) {
                mainHandler.post { onError(t) }
            }
        }
    }

    fun loadStats(onSuccess: (total: Int, active: Int) -> Unit, onError: (Throwable) -> Unit = {}) {
        ioExecutor.execute {
            try {
                val total = dao.totalCount
                val active = dao.getActiveCount("Resolved")
                mainHandler.post { onSuccess(total, active) }
            } catch (t: Throwable) {
                mainHandler.post { onError(t) }
            }
        }
    }

    fun loadRecentAlerts(limit: Int, onSuccess: (List<AlertEntity>) -> Unit, onError: (Throwable) -> Unit = {}) {
        ioExecutor.execute {
            try {
                val alerts = dao.getLatestAlerts(limit)
                mainHandler.post { onSuccess(alerts) }
            } catch (t: Throwable) {
                mainHandler.post { onError(t) }
            }
        }
    }

    fun loadAllAlerts(onSuccess: (List<AlertEntity>) -> Unit, onError: (Throwable) -> Unit = {}) {
        ioExecutor.execute {
            try {
                val alerts = dao.allAlerts
                mainHandler.post { onSuccess(alerts) }
            } catch (t: Throwable) {
                mainHandler.post { onError(t) }
            }
        }
    }
}

