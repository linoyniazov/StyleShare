package com.example.styleshare.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.styleshare.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.styleshare.R
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.repository.UserRepository
import com.example.styleshare.viewmodel.UserViewModel
import androidx.fragment.app.viewModels
import com.google.firebase.firestore.FirebaseFirestore
import com.example.styleshare.model.entities.User
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    // ✅ הוספת ViewModel
    private val userViewModel: UserViewModel by viewModels {
        UserViewModel.UserViewModelFactory(
            UserRepository(AppDatabase.getDatabase(requireContext()).userDao())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text?.toString()?.trim()
            val password = binding.passwordEditText.text?.toString()?.trim()

            if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        lifecycleScope.launch {
                            val userExists = userViewModel.doesUserExist(userId)

                            if (!userExists) {
                                FirebaseFirestore.getInstance().collection("users").document(userId).get()
                                    .addOnSuccessListener { document ->
                                        val username = document.getString("username") ?: ""
                                        val email = document.getString("email") ?: ""
                                        val profileImageUrl = document.getString("profileImageUrl") ?: ""
                                        val bio = document.getString("bio") ?: ""

                                        val user = User(
                                            userId = userId,
                                            username = username,
                                            email = email,
                                            profileImageUrl = profileImageUrl,
                                            bio = bio
                                        )
                                        userViewModel.insertUser(user)
                                    }
                            }
                        }

                        if (isAdded) {
                            Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        }
                    } else {
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }

        binding.registerLink.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterEmailFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
