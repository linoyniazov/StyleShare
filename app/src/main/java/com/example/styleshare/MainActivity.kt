package com.example.styleshare

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.styleshare.databinding.ActivityMainBinding
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.repository.HomeRepository
import com.example.styleshare.viewmodel.HomeViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { HomeRepository(database.postDao()) }
    private val factory by lazy { HomeViewModel.HomeViewModelFactory(repository) }
    private val homeViewModel: HomeViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // ✅ מאזינים לשינויים בנווט ומעדכנים את ה-Bottom Navigation בהתאם
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateNavigationVisibility(destination.id)
        }

        // ✅ חיבור ה-Bottom Navigation לניווט
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val currentDestination = navController.currentDestination?.id
            when (item.itemId) {
                R.id.homeFragment -> {
                    if (currentDestination != R.id.homeFragment) {
                        navController.navigate(R.id.homeFragment)
                    }
                    true
                }
                R.id.addPostFragment -> {
                    if (currentDestination != R.id.uploadPostFragment) {
                        navController.navigate(R.id.uploadPostFragment)
                    }
                    true
                }
                R.id.profileFragment -> {
                    if (currentDestination != R.id.profileFragment) {
                        navController.navigate(R.id.profileFragment)
                    }
                    true
                }
                else -> false
            }
        }
    }

    /**
     * ✅ פונקציה לעדכון הניווט בהתאם למסך הפעיל
     */
    fun updateNavigationVisibility(destinationId: Int) {
        val hideBottomNavScreens = setOf(
            R.id.uploadPostFragment,
            R.id.welcomeFragment,
            R.id.loginFragment,
            R.id.registerEmailFragment,
            R.id.registerPasswordFragment
        )

        binding.bottomNavigation.visibility = if (destinationId in hideBottomNavScreens) View.GONE else View.VISIBLE
    }
}
