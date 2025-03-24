package com.example.styleshare.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.styleshare.databinding.FragmentProfileBinding
import com.example.styleshare.viewmodel.ProfileViewModel
import com.example.styleshare.adapters.PostsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.example.styleshare.R
import com.google.firebase.firestore.FirebaseFirestore
import android.app.AlertDialog
import android.widget.Toast
import android.content.Context
import androidx.core.content.ContextCompat



class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        setupRecyclerView()
        setupListeners()
        loadUserData()
        loadPostCount()
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            navController.navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    private fun logout() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()

                requireActivity().getSharedPreferences("StyleShare", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()

                Toast.makeText(requireContext(), "You have been logged out", Toast.LENGTH_SHORT).show()

                navController.navigate(R.id.action_profileFragment_to_loginFragment)
                navController.popBackStack(R.id.loginFragment, false)
            }
            .setNegativeButton("Cancel", null)
            .create() // שימי לב: משתמשים ב־create במקום show

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }


    private fun setupRecyclerView() {
        postsAdapter = PostsAdapter()
        binding.categoriesGrid.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = postsAdapter
        }
    }

    private fun loadPostCount() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (isAdded) {
                    binding.postCount.text = documents.size().toString()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error fetching post count", e)
            }
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        profileViewModel.loadUserProfile(userId)
        profileViewModel.loadUserPosts(userId)

        viewLifecycleOwnerLiveData.observe(viewLifecycleOwner) { lifecycleOwner ->
            profileViewModel.username.observe(lifecycleOwner) { username ->
                binding.username.text = "@$username"
            }

            profileViewModel.bio.observe(lifecycleOwner) { bio ->
                binding.userBio.text = bio
            }

            profileViewModel.profileImage.observe(lifecycleOwner) { imageUrl ->
                if (!imageUrl.isNullOrEmpty() && isAdded) {
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .into(binding.profileImage)
                }
            }

            profileViewModel.userPosts.observe(lifecycleOwner) { posts ->
                postsAdapter.submitList(posts)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}