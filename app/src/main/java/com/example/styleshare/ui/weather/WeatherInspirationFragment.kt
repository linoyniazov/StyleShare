package com.example.styleshare.ui.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.styleshare.adapters.OutfitAdapter
import com.example.styleshare.api.WeatherApi
import com.example.styleshare.databinding.FragmentWeatherInspirationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

class WeatherInspirationFragment : Fragment() {
    private var _binding: FragmentWeatherInspirationBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var adapter: OutfitAdapter

    private val WEATHER_API_KEY = "753dd7ad9d611426a2449815592357f7"
    private val LOCATION_PERMISSION_REQUEST = 1000

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherInspirationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.setOnRefreshListener {
            getCurrentLocation()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupRecyclerView()
        checkLocationPermission()
    }

    private fun setupRecyclerView() {
        adapter = OutfitAdapter()
        binding.outfitsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@WeatherInspirationFragment.adapter
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(
                        context,
                        "Location permission is required for weather-based suggestions",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        fetchWeather(it.latitude, it.longitude)
                    }
                }
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApi = retrofit.create(WeatherApi::class.java)

        coroutineScope.launch {
            try {
                val response = weatherApi.getWeather(lat, lon, apiKey = WEATHER_API_KEY)

                // Update UI with weather info
                binding.apply {
                    locationText.text = response.name
                    temperatureText.text = "${response.main.temp.roundToInt()}°C"

                    val weather = response.weather.firstOrNull()
                    val weatherMain = weather?.main?.lowercase() ?: ""
                    val weatherDesc = weather?.description?.capitalize() ?: ""

                    // Enhanced weather description
                    val timeOfDay = if (weather?.icon?.endsWith("d") == true) "day" else "night"
                    weatherDescription.text = getEnhancedWeatherDescription(weatherMain, timeOfDay)

                    // Custom weather icon URL based on condition
                    val iconUrl = getCustomWeatherIconUrl(weatherMain, timeOfDay)
                    Glide.with(requireContext())
                        .load(iconUrl)
                        .into(weatherIcon)

                    // Enhanced outfit suggestions based on weather and temperature
                    outfitSuggestionText.text = when {
                        weatherMain == "rain" -> "Time for Rain-Ready Outfits ☔"
                        weatherMain == "snow" -> "Bundle Up in Winter Warmth ❄️"
                        weatherMain == "clear" && response.main.temp >= 25 -> "Perfect for Summer Vibes ☀️"
                        weatherMain == "clear" && response.main.temp >= 20 -> "Ideal for Light & Breezy Looks 🌞"
                        weatherMain == "clouds" && response.main.temp >= 20 -> "Stylish Layers for a Cloudy Day ⛅"
                        response.main.temp >= 15 -> "Light Jacket Weather 🧥"
                        response.main.temp >= 10 -> "Time to Layer Up! 👔"
                        else -> "Cozy Winter Fashion ❄️"
                    }

                    // Load appropriate outfits based on temperature and weather
                    loadOutfitsForWeather(response.main.temp, weatherMain)
                }

                // End the swipe refresh animation
                binding.swipeRefreshLayout.isRefreshing = false

            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching weather data", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false // Stop the refresh animation on error
            }
        }
    }


    private fun getEnhancedWeatherDescription(weatherMain: String, timeOfDay: String): String {
        return when (weatherMain) {
            "clear" -> if (timeOfDay == "day") "Sunny and Clear ☀️" else "Clear Night 🌙"
            "clouds" -> "Cloudy Skies ⛅"
            "rain" -> "Rainy Weather ☔"
            "snow" -> "Snowing ❄️"
            "thunderstorm" -> "Thunderstorms ⛈️"
            "drizzle" -> "Light Rain 🌧️"
            "mist", "fog" -> "Misty Conditions 🌫️"
            else -> "Weather Unknown"
        }
    }

    private fun getCustomWeatherIconUrl(weatherMain: String, timeOfDay: String): String {
        val baseUrl = "https://cdn.weatherapi.com/weather/128x128"
        return when (weatherMain) {
            "clear" -> if (timeOfDay == "day") "$baseUrl/day/113.png" else "$baseUrl/night/113.png"
            "clouds" -> if (timeOfDay == "day") "$baseUrl/day/116.png" else "$baseUrl/night/116.png"
            "rain" -> "$baseUrl/day/308.png"
            "snow" -> "$baseUrl/day/332.png"
            "thunderstorm" -> "$baseUrl/day/389.png"
            "drizzle" -> "$baseUrl/day/266.png"
            "mist", "fog" -> "$baseUrl/day/248.png"
            else -> "$baseUrl/day/113.png"
        }
    }

    private fun loadOutfitsForWeather(temperature: Double, weatherCondition: String) {
        val cloudName = "drwkwfox9"
        val baseFolder = "weather outfit"

        val outfitFolder = when {
            weatherCondition == "rain" -> "rain_outfits"
            weatherCondition == "snow" -> "winter_outfits"
            temperature >= 25 -> "summer_outfits"
            temperature >= 20 -> "spring_outfits"
            temperature >= 15 -> "fall_outfits"
            temperature >= 10 -> "light_winter_outfits"
            else -> "winter_outfits"
        }

        val baseUrl = "https://res.cloudinary.com/$cloudName/image/upload/${baseFolder.replace(" ", "%20")}/$outfitFolder"

        val outfits = mutableListOf<String>()

        for (i in 1..6) {
            outfits.add("$baseUrl/look$i.jpg")
        }

        adapter.submitList(outfits)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
        _binding = null
    }
}