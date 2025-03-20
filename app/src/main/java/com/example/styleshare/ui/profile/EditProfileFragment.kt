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

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = auth.currentUser?.uid
        userId?.let { loadUserData(it) }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }

        binding.btnChangePhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Profile photo update not implemented", Toast.LENGTH_SHORT).show()
            // TODO: Implement photo upload functionality
        }
    }

    private fun loadUserData(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val username = document.getString("username") ?: ""
                    val bio = document.getString("bio") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl")

                    binding.nameInput.setText(name)
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
        val name = binding.nameInput.text.toString().trim()
        val username = binding.usernameInput.text.toString().trim()
        val bio = binding.bioInput.text.toString().trim()

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username)) {
            Toast.makeText(requireContext(), "Name and username are required!", Toast.LENGTH_SHORT).show()
            return
        }

        val userUpdates = hashMapOf(
            "name" to name,
            "username" to username,
            "bio" to bio
        )

        userId?.let {
            firestore.collection("users").document(it).update(userUpdates as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp() // חזרה למסך הקודם
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
