package com.example.viatrack

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.viatrack.ui.gallery.GalleryFragment

enum class NavItem(
    val viewId: Int,
    val iconResId: Int,
    val title: String,
    val fragmentCreator: () -> Fragment
) {
    TRACKER(R.id.nav_tracker, R.drawable.tracker, "Трекер", { TrackerFragment() }),
    HISTORY(R.id.nav_history, R.drawable.history, "История", { HistoryFragment() }),
    GALLERY(R.id.nav_gallery, R.drawable.gallery, "Галерея", { GalleryFragment() }),
    PROFILE(R.id.nav_profile, R.drawable.profile, "Профиль", { ProfileFragment() })
}

class MainActivity : AppCompatActivity() {
    private val navButtons = mutableMapOf<Int, View>()

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->

            val fineGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

            if (fineGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestBackgroundLocation()
            }
        }

    private val backgroundLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            checkAndEnableGPS()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupCustomBottomNav()

        if (savedInstanceState == null) {
            val initialItem = NavItem.HISTORY
            navButtons[initialItem.viewId]?.isActivated = true
            loadFragment(initialItem.name, initialItem.fragmentCreator)
        }

        requestAllNeededPermissions()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 999) {
            if (resultCode == Activity.RESULT_OK) {
            } else {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndEnableGPS()
    }


    private fun requestAllNeededPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        fun need(perm: String) =
            ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED

        if (need(Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if (need(Manifest.permission.ACTIVITY_RECOGNITION))
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            need(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestBackgroundLocation()
            } else {
                checkAndEnableGPS()
            }
        }
    }

    private fun requestBackgroundLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    private fun checkAndEnableGPS() {
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            1000
        ).build()

        val builder = com.google.android.gms.location.LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client = com.google.android.gms.location.LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
        }

        task.addOnFailureListener { exception ->
            if (exception is com.google.android.gms.common.api.ResolvableApiException) {
                exception.startResolutionForResult(this, 999)
            }
        }
    }


    private fun setupCustomBottomNav() {
        NavItem.entries.forEach { item ->
            val buttonView = findViewById<View>(item.viewId)
            navButtons[item.viewId] = buttonView
            val iconView = buttonView.findViewById<ImageView>(R.id.nav_icon)
            val titleView = buttonView.findViewById<TextView>(R.id.nav_title)
            iconView.setImageResource(item.iconResId)
            titleView.text = item.title
            buttonView.setOnClickListener {
                handleNavigationClick(item)
            }
        }
    }

    private fun handleNavigationClick(selectedItem: NavItem) {
        loadFragment(selectedItem.name, selectedItem.fragmentCreator)
        navButtons.values.forEach { it.isActivated = false }
        navButtons[selectedItem.viewId]?.isActivated = true
    }

    private fun loadFragment(tag: String, fragmentCreator: () -> Fragment) {
        val existing = supportFragmentManager.findFragmentByTag(tag)
        val fragmentToShow = existing ?: fragmentCreator()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragmentToShow, tag)
            .commit()
    }
}