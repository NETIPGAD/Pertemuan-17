package com.example.myfriend

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController

class MainActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false
    private lateinit var navController: NavController

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when (navController.currentDestination?.id) {
                R.id.homeFragment -> {
                    if (doubleBackToExitPressedOnce) {
                        finish()
                        return
                    }
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(this@MainActivity, "Tekan back lagi untuk keluar", Toast.LENGTH_SHORT).show()
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                }
                R.id.detailFragment, R.id.editFriendFragment, R.id.addFriendFragment -> {
                    // Navigasi kembali ke HomeFragment
                    navController.popBackStack(R.id.homeFragment, false)
                    Log.d("MainActivity", "Back pressed from ${navController.currentDestination?.label}, navigating to HomeFragment")
                }
                else -> {
                    // Biarkan NavController menangani back untuk fragment lain
                    if (!navController.popBackStack()) {
                        finish() // Keluar jika tidak ada fragment di back stack
                    }
                    Log.d("MainActivity", "Back pressed from ${navController.currentDestination?.label}, default popBackStack")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Atur latar belakang putih
        window.setBackgroundDrawableResource(android.R.color.white)

        // Inisialisasi toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (toolbar == null) {
            Log.e("MainActivity", "Toolbar not found in layout!")
            return
        }
        setSupportActionBar(toolbar)
        Log.d("MainActivity", "ActionBar set successfully")

        // Inisialisasi NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        if (navHostFragment == null) {
            Log.e("MainActivity", "NavHostFragment not found!")
            return
        }
        navController = navHostFragment.navController
        Log.d("MainActivity", "Start destination ID: ${navController.graph.startDestinationId}")

        // Konfigurasi AppBar dengan semua fragment
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.welcomeFragment, R.id.homeFragment, R.id.detailFragment, R.id.addFriendFragment, R.id.editFriendFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Tambahkan callback untuk tombol back
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Listener untuk update UI dan debug
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("MainActivity", "Destination changed to: ${destination.label} (ID: ${destination.id})")
            supportActionBar?.let { actionBar ->
                when (destination.id) {
                    R.id.welcomeFragment -> actionBar.hide()
                    else -> {
                        actionBar.show()
                        actionBar.title = destination.label
                    }
                }
            }
        }

        // Debug setelah setup
        Log.d("MainActivity", "Current destination after setup: ${navController.currentDestination?.id}")
        if (navController.currentDestination == null) {
            Log.e("MainActivity", "Start destination still null! Check nav_graph.xml or layout.")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}