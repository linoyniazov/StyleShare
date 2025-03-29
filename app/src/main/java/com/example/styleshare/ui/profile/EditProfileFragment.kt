package com.example.styleshare.ui.profile

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.styleshare.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.styleshare.model.CloudinaryModel
import kotlinx.coroutines.launch
import com.example.styleshare.R
import androidx.fragment.app.viewModels
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.model.entities.User
import com.example.styleshare.repository.UserRepository
import com.example.styleshare.viewmodel.UserViewModel

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var selectedImageUri: Uri? = null

    private val userViewModel: UserViewModel by viewModels {
        UserViewModel.UserViewModelFactory(
            UserRepository(AppDatabase.getDatabase(requireContext()).userDao())
        )
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.profileImage.setImageURI(it)
            uploadProfileImageToCloudinary(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = auth.currentUser?.uid
        userId?.let { loadUserData(it) }

        binding.btnBack.setOnClickListener {
            try {
                if (isAdded) {
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                Log.e("EditProfileFragment", "Navigation error on back press", e)
            }
        }

        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }

        binding.btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun loadUserData(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: ""
                    val bio = document.getString("bio") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl")

                    binding.usernameInput.setText(username)
                    binding.bioInput.setText(bio)

                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(profileImageUrl).into(binding.profileImage)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserProfile() {
        val username = binding.usernameInput.text.toString().trim()
        val bio = binding.bioInput.text.toString().trim()

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(requireContext(), "username is required!", Toast.LENGTH_SHORT).show()
            return
        }

        val userUpdates = hashMapOf(
            "username" to username,
            "bio" to bio
        )

        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).update(userUpdates as Map<String, Any>)
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val updatedUser = User(
                                    userId = userId,
                                    username = document.getString("username") ?: "",
                                    email = document.getString("email") ?: "",
                                    profileImageUrl = document.getString("profileImageUrl") ?: "",
                                    bio = document.getString("bio") ?: ""
                                )
                                userViewModel.insertUser(updatedUser)
                            }
                        }

                    view?.postDelayed({
                        try {
                            findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
                        } catch (e: Exception) {
                            Log.e("EditProfileFragment", "Navigation error: ${e.message}")
                        }
                    }, 100)
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditProfileFragment", "Firestore update failed", e)
                }
            }
    }

    private fun uploadProfileImageToCloudinary(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val imageUrl = CloudinaryModel.uploadImageFromUri(requireContext(), imageUri, "profile_pictures")
                if (!imageUrl.isNullOrEmpty()) {
                    saveProfileImageUrlToDatabase(imageUrl)
                } else {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Log.e("EditProfileFragment", "Error uploading image", e)
                    Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveProfileImageUrlToDatabase(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        userRef.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile image updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save profile image", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
