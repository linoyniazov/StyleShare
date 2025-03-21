package com.example.styleshare.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.styleshare.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.styleshare.R

class LoginFragment : Fragment() {


    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

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
//
//            auth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
//                        // Navigate to HomeFragment after successful login
//                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
//                    } else {
//                        Toast.makeText(requireContext(), "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
//                    }
//                }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (isAdded) { // ✅ בודק אם ה-Fragment עדיין מחובר
                            Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        }
                    } else {
                        if (isAdded) { // ✅ בודק שוב לפני הצגת הודעת שגיאה
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