package com.example.styleshare.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.styleshare.databinding.FragmentRegisterEmailBinding
import com.example.styleshare.model.CloudinaryModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RegisterEmailFragment : Fragment() {

    private var _binding: FragmentRegisterEmailBinding? = null
    private val binding get() = _binding!!

    private var profileImageUrl: String = ""
    private lateinit var currentPhotoPath: String

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.profileImageView)
                uploadImageToCloudinary(it)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    File(currentPhotoPath)
                )
                Glide.with(this)
                    .load(photoUri)
                    .circleCrop()
                    .into(binding.profileImageView)
                uploadImageToCloudinary(photoUri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectImageButton.setOnClickListener {
            showImageSourceDialog()
        }

        binding.continueButton.setOnClickListener {
            val email = binding.emailEditText.text?.toString()?.trim()
            val username = binding.usernameEditText.text?.toString()?.trim()

            when {
                email.isNullOrEmpty() -> {
                    Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(requireContext(), "Enter a valid email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                username.isNullOrEmpty() -> {
                    binding.usernameInputLayout.error = "Please enter a username"
                    return@setOnClickListener
                }
                profileImageUrl.isEmpty() -> {
                    Toast.makeText(requireContext(), "You haven't selected a profile image. You can add it later.", Toast.LENGTH_SHORT).show()
                }
            }

            val action = RegisterEmailFragmentDirections
                .actionRegisterEmailFragmentToRegisterPasswordFragment(
                    email,
                    username,
                    "", // Empty string for fullName since we removed it
                    profileImageUrl
                )
            findNavController().navigate(action)
        }

        binding.loginLink.setOnClickListener {
            findNavController().navigate(
                RegisterEmailFragmentDirections.actionRegisterEmailFragmentToLoginFragment()
            )
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("בחר מהגלריה", "צלם תמונה")
        AlertDialog.Builder(requireContext())
            .setTitle("בחר תמונה")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> galleryLauncher.launch("image/*")
                    1 -> checkCameraPermission()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showCameraPermissionRationale()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showCameraPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Camera Permission Required")
            .setMessage("We need camera permission to take profile photos. Would you like to grant permission?")
            .setPositiveButton("Yes") { _, _ ->
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun startCamera() {
        val photoFile = createImageFile()
        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().cacheDir
        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun uploadImageToCloudinary(uri: Uri) {
        lifecycleScope.launch {
            val uploadedUrl = CloudinaryModel.uploadImageFromUri(requireContext(), uri)
            if (uploadedUrl != null) {
                profileImageUrl = uploadedUrl
                Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}