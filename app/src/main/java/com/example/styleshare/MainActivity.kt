package com.example.styleshare

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.styleshare.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // אם יש משתמש מחובר, תנווט לעמוד הבית
            navController.navigate(R.id.homeFragment)
        } else {
            // אם אין משתמש מחובר, תנווט לעמוד login
            navController.navigate(R.id.welcomeFragment)
        }

        binding.bottomNavigation.setupWithNavController(navController)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .build()

            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment, null, options)
                    true
                }
                R.id.uploadPostFragment -> {
                    navController.navigate(R.id.uploadPostFragment, null, options)
                    true
                }
                R.id.profileFragment -> {
                    navController.navigate(R.id.profileFragment, null, options)
                    true
                }
                R.id.navigation_weather -> {
                    navController.navigate(R.id.navigation_weather, null, options)
                    true
                }

                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hiddenScreens = setOf(
                R.id.welcomeFragment,
                R.id.loginFragment,
                R.id.registerEmailFragment,
                R.id.registerPasswordFragment
            )
            binding.bottomNavigation.visibility =
                if (destination.id in hiddenScreens) View.GONE else View.VISIBLE
        }
    }

    // פונקציה לביצוע logout
    private fun logoutUser() {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()  // יציאה מהמערכת
        navController.navigate(R.id.welcomeFragment)  // ניווט לעמוד Welcome
    }
}
