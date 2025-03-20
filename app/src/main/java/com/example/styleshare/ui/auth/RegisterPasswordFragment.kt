package com.example.styleshare.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.styleshare.databinding.FragmentRegisterPasswordBinding
import com.example.styleshare.ui.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.viewModels
import com.example.styleshare.R
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.repository.UserRepository
import com.example.styleshare.viewmodel.UserViewModel
import com.example.styleshare.model.entities.User


class RegisterPasswordFragment : BaseFragment() {

    override val showToolbar: Boolean = true
    override val showBottomNav: Boolean = false
    override val showBackButton: Boolean = true

    private val userViewModel: UserViewModel by viewModels {
        UserViewModel.UserViewModelFactory(
            UserRepository(AppDatabase.getDatabase(requireContext()).userDao())
        )
    }

    private var _binding: FragmentRegisterPasswordBinding? = null
    private val binding get() = _binding!!
    private val args: RegisterPasswordFragmentArgs by navArgs()

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val email = args.email
        binding.emailTextView.text = email

        binding.registerButton.setOnClickListener {
            val password = binding.passwordEditText.text?.toString()?.trim()
            val confirmPassword = binding.passwordConfirmEditText.text?.toString()?.trim()

            if (password.isNullOrEmpty() || confirmPassword.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Password fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔹 בדיקה אם המשתמש כבר קיים
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods
                        if (!signInMethods.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), "This email is already registered. Try logging in.", Toast.LENGTH_LONG).show()
                        } else {
                            registerUserWithEmail(email, password)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error checking email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun registerUserWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Registration Successful!", Toast.LENGTH_SHORT).show()
                    val firebaseUser = auth.currentUser
                    val newUser = User(
                        userId = firebaseUser?.uid ?: "",
                        username = "", // אפשר להשלים בהמשך
                        email = firebaseUser?.email ?: "",
                        fullName = "",
                        profileImageUrl = "",
                        bio = ""
                    )
                    userViewModel.insertUser(newUser)
                    findNavController().navigate(R.id.action_registerPasswordFragment_to_homeFragment)

                } else {
                    Toast.makeText(requireContext(), "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
