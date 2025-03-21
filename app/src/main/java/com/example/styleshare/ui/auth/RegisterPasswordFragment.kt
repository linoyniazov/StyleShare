package com.example.styleshare.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentRegisterPasswordBinding
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.model.entities.User
import com.example.styleshare.repository.UserRepository
import com.example.styleshare.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.fragment.app.Fragment


class RegisterPasswordFragment : Fragment() {

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
        binding.emailTextView.text = args.email

        binding.registerButton.setOnClickListener {
            val password = binding.passwordEditText.text?.toString()?.trim()
            val confirmPassword = binding.passwordConfirmEditText.text?.toString()?.trim()

            when {
                password.isNullOrEmpty() || confirmPassword.isNullOrEmpty() -> {
                    Toast.makeText(requireContext(), "Password fields cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            auth.fetchSignInMethodsForEmail(args.email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods
                        if (!signInMethods.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), "This email is already registered. Try logging in.", Toast.LENGTH_LONG).show()
                        } else {
                            registerUserWithEmailAndSave(password!!)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error checking email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun registerUserWithEmailAndSave(password: String) {
        val email = args.email
        val username = args.username
        val fullName = args.fullName
        val profileImageUrl = args.profileImageUrl ?: ""

        if (email.isNullOrEmpty() || username.isNullOrEmpty() || fullName.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Missing registration data", Toast.LENGTH_LONG).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid ?: return@addOnCompleteListener

                    val newUser = User(
                        userId = userId,
                        username = username,
                        email = email,
                        fullName = fullName,
                        profileImageUrl = profileImageUrl,
                        bio = ""
                    )

                    userViewModel.insertUser(newUser)

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .set(newUser)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Welcome!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registerPasswordFragment_to_homeFragment)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to save user: ${it.message}", Toast.LENGTH_LONG).show()
                        }

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
