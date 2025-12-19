package com.example.viatrack.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.viatrack.MainActivity
import com.example.viatrack.R
import com.example.viatrack.database.TrackPoint
import com.example.viatrack.repository.TrackerRepository
import com.example.viatrack.utils.Constants
import com.example.viatrack.utils.TrackingUtils
import com.google.android.gms.location.*
import kotlinx.coroutines.launch

class TrackingService : LifecycleService(), SensorEventListener {

    private var isFirstRun = true
    private var isServiceRunning = false

    private var currentTripId: Long? = null
    private var currentEventId: Long? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        Constants.LOCATION_UPDATE_INTERVAL
    ).setMinUpdateIntervalMillis(Constants.FASTEST_LOCATION_INTERVAL).build()

    private val pathPoints = mutableListOf<Location>()

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var totalSteps = 0L
    private var previousTotalSteps = 0L

    private lateinit var repository: TrackerRepository

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val liveDistance = MutableLiveData<Float>()
        val liveSteps = MutableLiveData<Long>()
        val livePace = MutableLiveData<Float>()
        val liveAltitude = MutableLiveData<Double>()
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(Constants.NOTIFICATION_ID, createNotification())
        repository = TrackerRepository(application)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) Log.e("TrackingService", "Шагомер не найден")

        postInitialValues()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        liveDistance.postValue(0f)
        liveSteps.postValue(0L)
        livePace.postValue(0f)
        liveAltitude.postValue(0.0)
        pathPoints.clear()
        totalSteps = 0
        previousTotalSteps = 0
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {

            val tripIdFromIntent = it.getLongExtra(Constants.EXTRA_TRIP_ID, -1L)
            if (tripIdFromIntent != -1L) {
                currentTripId = tripIdFromIntent
                Log.d("TrackingService", "Принят tripId = $currentTripId")
            }

            val newEventId = it.getLongExtra(Constants.EXTRA_EVENT_ID, -2L)
            if (newEventId != -2L) {
                currentEventId = if (newEventId == -1L) null else newEventId
                Log.d("TrackingService", "Принят eventId = $currentEventId")
            }

            when (it.action) {

                Constants.ACTION_START_TRACKING -> {
                    if (isTracking.value == false) {
                        Log.d("TrackingService", "Старт трекинга")
                        startTracking()
                    }
                }

                Constants.ACTION_PAUSE_TRACKING -> {
                    if (isTracking.value == true) {
                        Log.d("TrackingService", "Пауза трекинга")
                        pauseTracking()
                    }
                }

                Constants.ACTION_STOP_TRACKING -> {
                    Log.d("TrackingService", "Стоп трекинга")
                    stopTracking()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTracking() {

        if (currentTripId == null || currentTripId!! <= 0L) {
            Log.e("TrackingService", "ОШИБКА: старт трекинга без корректного tripId")
            return
        }

        isTracking.postValue(true)

        if (!isServiceRunning) {
            startForegroundService()
            isServiceRunning = true
        }

        requestLocationUpdates()

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun pauseTracking() {
        isTracking.postValue(false)
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)
    }

    private fun stopTracking() {
        pauseTracking()
        postInitialValues()
        stopSelf()

        isServiceRunning = false
        isFirstRun = true
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (isTracking.value == true) {
                result.lastLocation?.let { addPathPoint(it) }
            }
        }
    }

    private fun addPathPoint(location: Location) {

        if (currentTripId == null) {
            Log.e("TrackingService", "Поступила точка, но tripId = null — игнор")
            return
        }

        pathPoints.add(location)

        liveAltitude.postValue(location.altitude)
        livePace.postValue(location.speed * 3.6f)
        liveDistance.postValue(TrackingUtils.calculatePolylineDistance(pathPoints))

        lifecycleScope.launch {

            val point = TrackPoint(
                tripId = currentTripId!!,
                eventId = currentEventId,
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                speed = location.speed,
                timestamp = location.time
            )

            repository.saveTrackPoint(point)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return

        val currentSteps = event.values[0].toLong()

        if (previousTotalSteps == 0L) {
            previousTotalSteps = currentSteps
        }

        totalSteps = currentSteps - previousTotalSteps

        if (isTracking.value == true) {
            liveSteps.postValue(totalSteps)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit


    fun resetMetricsForNewEvent() {
        previousTotalSteps = 0L
        totalSteps = 0L
        pathPoints.clear()

        liveDistance.postValue(0f)
        liveSteps.postValue(0L)
        livePace.postValue(0f)
    }

    private fun startForegroundService() {
        startForeground(Constants.NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.tracker)
            .setContentTitle("ViaTrack")
            .setContentText("Идёт запись путешествия…")
            .setContentIntent(pendingIntent)
            .build()
    }
}
