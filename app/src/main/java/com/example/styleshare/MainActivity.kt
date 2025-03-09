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
import com.example.styleshare.viewmodel.HomeViewModel.HomeViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private val homeViewModel: HomeViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        HomeViewModelFactory(HomeRepository(database.postDao(), database.followDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Connect bottom navigation with NavController
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val currentDestination = navController.currentDestination?.id
            when (item.itemId) {
                R.id.homeFragment -> {
                    if (currentDestination != R.id.homeFragment) {
                        navController.navigate(R.id.homeFragment)
                    }
                    true
                }
                R.id.searchFragment -> {
                    if (currentDestination != R.id.searchFragment) {
                        navController.navigate(R.id.searchFragment)
                    }
                    true
                }
                R.id.addPostFragment -> {
                    if (currentDestination != R.id.addPostFragment) {
                        navController.navigate(R.id.addPostFragment)
                    }
                    true
                }
                R.id.likesFragment -> {
                    if (currentDestination != R.id.likesFragment) {
                        navController.navigate(R.id.likesFragment)
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

    fun updateNavigationVisibility(showToolbar: Boolean, showBottomNav: Boolean) {
        binding.toolbar.root.visibility = if (showToolbar) View.VISIBLE else View.GONE
        binding.bottomNavigation.visibility = if (showBottomNav) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        updateNavigationVisibility(true, true) // Default: Show both toolbar and bottom nav
    }
}