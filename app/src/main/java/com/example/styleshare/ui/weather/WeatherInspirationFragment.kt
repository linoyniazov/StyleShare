package com.example.styleshare.ui.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.styleshare.adapters.OutfitAdapter
import com.example.styleshare.api.GeocodingApi
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
    private lateinit var weatherApi: WeatherApi
    private lateinit var geocodingApi: GeocodingApi

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

        setupRetrofit()
        setupLocationClient()
        setupRecyclerView()
        setupSearchInput()
        setupCurrentLocationButton()
        checkLocationPermission()
    }

    private fun setupRetrofit() {
        // Create separate Retrofit instances for weather and geocoding APIs
        val weatherRetrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val geocodingRetrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherApi = weatherRetrofit.create(WeatherApi::class.java)
        geocodingApi = geocodingRetrofit.create(GeocodingApi::class.java)
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun setupSearchInput() {
        binding.locationSearchInput.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val location = textView.text.toString()
                if (location.isNotEmpty()) {
                    searchLocation(location)
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupCurrentLocationButton() {
        binding.currentLocationButton.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun searchLocation(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        coroutineScope.launch {
            try {
                val locations = geocodingApi.getCoordinates(query, apiKey = WEATHER_API_KEY)
                if (locations.isNotEmpty()) {
                    val location = locations.first()
                    fetchWeather(location.lat, location.lon)
                } else {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error searching location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
        binding.progressBar.visibility = View.VISIBLE
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
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error getting location", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        coroutineScope.launch {
            try {
                val response = weatherApi.getWeather(lat, lon, apiKey = WEATHER_API_KEY)

                binding.apply {
                    locationText.text = response.name
                    temperatureText.text = "${response.main.temp.roundToInt()}°C"

                    val weather = response.weather.firstOrNull()
                    val weatherMain = weather?.main?.lowercase() ?: ""

                    val timeOfDay = if (weather?.icon?.endsWith("d") == true) "day" else "night"
                    weatherDescription.text = getEnhancedWeatherDescription(weatherMain, timeOfDay)

                    // Use weather condition icon from OpenWeatherMap directly
                    val iconCode = weather?.icon ?: "01d"
                    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
                    Glide.with(requireContext())
                        .load(iconUrl)
                        .into(weatherIcon)

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

                    loadOutfitsForWeather(response.main.temp, weatherMain)
                }
                binding.progressBar.visibility = View.GONE
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error fetching weather data", Toast.LENGTH_SHORT).show()
                }
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
            else -> weatherMain.capitalize()
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