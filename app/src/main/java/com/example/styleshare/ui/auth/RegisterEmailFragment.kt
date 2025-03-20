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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.styleshare.databinding.FragmentRegisterEmailBinding
import com.example.styleshare.model.CloudinaryModel
import com.example.styleshare.ui.BaseFragment
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RegisterEmailFragment : BaseFragment() {

    override val showToolbar: Boolean = true
    override val showBottomNav: Boolean = false
    override val showBackButton: Boolean = true

    private var _binding: FragmentRegisterEmailBinding? = null
    private val binding get() = _binding!!

    private var profileImageUrl: String = ""
    private lateinit var currentPhotoPath: String

    // בחירה מהגלריה
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uploadImageToCloudinary(it) }
        }

    // צילום מהמצלמה
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    File(currentPhotoPath)
                )
                uploadImageToCloudinary(photoUri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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
            val fullName = binding.fullNameEditText.text?.toString()?.trim()

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
                fullName.isNullOrEmpty() -> {
                    binding.fullNameInputLayout.error = "Please enter your full name"
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
                    fullName,
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
                    1 -> openCamera()
                }
            }
            .show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
            return
        }

        val photoFile = createImageFile()
        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
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
