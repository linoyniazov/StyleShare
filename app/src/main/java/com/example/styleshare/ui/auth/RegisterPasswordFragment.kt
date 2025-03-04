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
import com.google.firebase.auth.FirebaseAuth

class RegisterPasswordFragment : Fragment() {

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

//                    // 🔹 מעבר למסך התחברות לאחר ההרשמה
//                    findNavController().navigate(R.id.action_registerPasswordFragment_to_loginFragment)
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
