package com.example.styleshare.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.styleshare.databinding.FragmentRegisterEmailBinding
import com.example.styleshare.ui.BaseFragment

class RegisterEmailFragment : BaseFragment() {

    override val showToolbar: Boolean = true
    override val showBottomNav: Boolean = false
    override val showBackButton: Boolean = true

    private var _binding: FragmentRegisterEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueButton.setOnClickListener {
            val email = binding.emailEditText.text?.toString()?.trim()

            if (email.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔹 ניווט למסך הסיסמה עם האימייל שהוזן
            val action = RegisterEmailFragmentDirections.actionRegisterEmailFragmentToRegisterPasswordFragment(email)
            findNavController().navigate(action)
        }

        // 🔹 הוספת ניווט לעמוד Log In
        binding.loginLink.setOnClickListener {
            findNavController().navigate(RegisterEmailFragmentDirections.actionRegisterEmailFragmentToLoginFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
