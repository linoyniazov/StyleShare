package com.example.styleshare

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.styleshare.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // חיבור ל־View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // הגדרת ה־NavController מתוך ה־NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // חיבור ה־BottomNavigation ל־NavController
        binding.bottomNavigation.setupWithNavController(navController)

        // ניווט מותאם אישית עם popUpTo ומניעת חזרתיות
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

        // הסתרת ה־BottomNavigation במסכים מסוימים (למשל Login/Register)
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
}
